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

        //TODO how to get file URL
        FileInfo file;
        String fileURL;
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

        /*
        if(files == null){
            files = new HashMap<>();
            usersFiles.put(userId, files);
            fileURL = RESTDirServer.discovery.getEmptiestFileURI();
        }
        else {
            file = files.get(filename);
            if(file == null){
                fileURL = RESTDirServer.discovery.getEmptiestFileURI();
            }
            else {
                fileURL = file.getFileURL();
            }
        }
        file = new FileInfo(userId,
                filename,
                fileURL + "/" + RESTFilesServer.SERVICE + "/" + filename+"_"+userId,
                new HashSet<>());
        files.put(filename, file);
        */

        return Result.ok(file);
    }

    private FileInfo createNewFile(String filename, String userId) {
        String fileURL = RESTDirServer.discovery.getEmptiestFileURI();;
        FileInfo file = new FileInfo(userId,
                filename,
                fileURL + "/" + RESTFilesServer.SERVICE + "/" + filename + "_" + userId,
                new HashSet<>());
        return file;
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

         if(files.remove(filename) == null){
             return Result.error(Result.ErrorCode.BAD_REQUEST);
         }
        return Result.ok();
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
        filesSharedWithUser.add(file);

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
        System.out.println("JavaDir -> Entered getFile");
        Map<String, FileInfo> files = usersFiles.get(userId);
        if(files == null) {
            System.out.println("This is JavaDir: fileS is null");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        FileInfo file = files.get(filename);
        if(file == null) {
            System.out.println("This is JavaDir: file is null");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        if( accUserId != userId && !file.getSharedWith().contains(accUserId)) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        System.out.println("JavaDir -> redirecting getFile");
        //Redirect TODO check if is right
        throw new WebApplicationException(
                Response.temporaryRedirect(
                        URI.create(file.getFileURL())).build());
    }

    @Override
    public Result<List<FileInfo>> lsFile(String userId, String password) {
        Map<String, FileInfo> userFiles = usersFiles.get(userId);
        if(userFiles == null)
            return Result.error(Result.ErrorCode.BAD_REQUEST);

        return Result.ok(hasAccessTo(userId, userFiles));
    }

    private List<FileInfo> hasAccessTo(String userId, Map<String, FileInfo> userFiles){
        List<FileInfo> accesses = new ArrayList<>(userFiles.values());
        accesses.addAll(filesUserAccess.get(userId));
        return accesses;
    }
}
