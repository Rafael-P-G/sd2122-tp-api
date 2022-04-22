package tp1.server.resources.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.clients.files.RestFilesClient;
import tp1.server.RESTDirServer;
import tp1.server.RESTFilesServer;

import java.io.File;
import java.net.URI;
import java.util.*;

public class JavaDirectory implements Directory {

    private final Map<String, Map<String, FileInfo>> usersFiles = new HashMap<>();
    private final Map<String, List<FileInfo>> filesUserAccess = new HashMap<>();

    @Override
    public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {

        if(password == null)
            return Result.error(Result.ErrorCode.FORBIDDEN);

        if(filename == null){
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        FileInfo file;
        Map<String, FileInfo> files = usersFiles.get(userId);
        if(files == null){
            files = new HashMap<>();
            usersFiles.put(userId, files);

            file = createNewFile(filename, userId);
        }
        else {
            file = files.get(filename);
            if(file == null){
                file = createNewFile(filename, userId);
            }
            else {

                file.setFilename(filename);
                file.setOwner(userId);
                file.setSharedWith(file.getSharedWith());
                file.setFileURL(file.getFileURL());
            }
        }
        files.put(filename, file);

        return Result.ok(file);
    }

    private FileInfo createNewFile(String filename, String userId) {
        String fileURL = RESTDirServer.discovery.getEmptiestFileURI();
        return new FileInfo(userId,
                filename,
                fileURL + "/" + RESTFilesServer.SERVICE + "/" + filename + "_" + userId,
                new HashSet<>());
    }

    @Override
    public Result<Void> deleteFile(String filename, String userId, String password) {

        Map<String, FileInfo> files = usersFiles.get(userId);
        if(files == null) {
            System.out.println("This is JavaDirectory: files is null");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        if(!files.containsKey(filename)) {
            System.out.println("This is JavaDirectory: files contains file name");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        FileInfo fileToRemove = files.remove(filename);
        if(fileToRemove == null){
            System.out.println("The problem is in JavaDirectory, files.remove(filename) returned: null");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        removeFileFromSharedLists(fileToRemove);
        return Result.ok();
    }

    private void removeFileFromSharedLists(FileInfo fileToRemove) {

        System.out.println(fileToRemove.getSharedWith());
        fileToRemove.getSharedWith().forEach(user ->{
            boolean removed = filesUserAccess.get(user).remove(fileToRemove);
            System.out.println("File " + fileToRemove.getFilename() + " was removed from " + user + "? " + removed);
        });
    }

    @Override
    public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
        Map<String, FileInfo> files = usersFiles.get(userId);
        if(files == null)
            return Result.error(Result.ErrorCode.BAD_REQUEST);

        FileInfo file = files.get(filename);
        if(file == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        Set<String> sharedList = file.getSharedWith();
        sharedList.add(userIdShare);

        List<FileInfo> filesSharedWithUser = filesUserAccess.get(userIdShare);
        if(filesSharedWithUser == null){
            filesSharedWithUser = new ArrayList<>();
            filesUserAccess.put(userIdShare, filesSharedWithUser);
        }

        if(filesSharedWithUser.indexOf(file) < 0){
            filesSharedWithUser.add(file);
        }
        return Result.ok();
    }

    @Override
    public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
        Map<String, FileInfo> files = usersFiles.get(userId);
        if(files == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        FileInfo file = files.get(filename);
        if(file == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        Set<String> sharedList = file.getSharedWith();
        if(!sharedList.contains(userIdShare))
            return Result.error(Result.ErrorCode.BAD_REQUEST);

        sharedList.remove(userIdShare);

        List<FileInfo> filesSharedWithUser = filesUserAccess.get(userIdShare);
        if(filesSharedWithUser == null)
            return Result.error(Result.ErrorCode.BAD_REQUEST);

        filesSharedWithUser.remove(file);
        if(filesSharedWithUser.isEmpty()){
            filesUserAccess.remove(userIdShare);
        }

        return Result.ok();
    }

    @Override
    public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
        Map<String, FileInfo> files = usersFiles.get(userId);
        if(files == null) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        FileInfo file = files.get(filename);
        System.out.println("User " + userId + " files: " + files);
        System.out.println("File to be removed: " + file);
        if(file == null) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        if( !accUserId.equals(userId) && !file.getSharedWith().contains(accUserId)) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        //Redirect
        throw new WebApplicationException(
                Response.temporaryRedirect(
                        URI.create(file.getFileURL())).build());
    }

    @Override
    public Result<List<FileInfo>> lsFile(String userId, String password) {
        return Result.ok(hasAccessTo(userId));
    }

    @Override
    public Result<List<FileInfo>> deleteAllUserFiles(String userId) {
        Map<String, FileInfo> filesToRemove = usersFiles.get(userId);

        if(filesToRemove == null)
            return Result.ok(new ArrayList<FileInfo>());

        filesToRemove.forEach((k, v) ->{
            removeFileFromSharedLists(v);
        });

        var files = usersFiles.get(userId).values();
        usersFiles.remove(userId);
        return Result.ok(new ArrayList<FileInfo>(files));
    }

    private List<FileInfo> hasAccessTo(String userId){
        List<FileInfo> result = new ArrayList<>();
        Map<String, FileInfo> uf = usersFiles.get(userId);

        if(uf != null){
            List<FileInfo> files = new ArrayList<>(uf.values());
            if(files != null){
                result.addAll(files);
            }
        }
        List<FileInfo> accesses = filesUserAccess.get(userId);
        System.out.println("User " + userId + " has accesses to: " + accesses);
        if(accesses != null){
            result.addAll(filesUserAccess.get(userId));
        }
        return result;
    }
}
