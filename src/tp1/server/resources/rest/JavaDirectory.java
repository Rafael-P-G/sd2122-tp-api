package tp1.server.resources.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.server.RESTDirServer;
import tp1.server.RESTFilesServer;
import util.UrlParser;

import java.net.URI;
import java.util.*;

public class JavaDirectory implements Directory {

    private final Map<String, Map<String, FileInfo>> usersFiles = new HashMap<>();
    private final Map<String, List<FileInfo>> filesUserAccess = new HashMap<>();

    @Override
    public synchronized Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {

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

            file = createNewFileInfo(filename, userId);
        }
        else {
            file = files.get(filename);
            if(file == null){
                file = createNewFileInfo(filename, userId);
            }
            else {

                file.setFilename(filename);
                file.setOwner(userId);
                file.setSharedWith(file.getSharedWith());
                file.setFileURL(file.getFileURL());
            }
        }
        files.put(filename, file);

        //incrementFileNmr(file);

        return Result.ok(file);
    }

    private FileInfo createNewFileInfo(String filename, String userId) {
        String fileURI = getEmptiestFileURI();
        /*
        var wrapper = new Object(){String uri; int min = Collections.min(nmrFilesInUris.values());;};
        nmrFilesInUris.forEach((k, v) ->{
            if(v == wrapper.min) {
                wrapper.uri = k;
            }
        });
        String fileURI = wrapper.uri;
         */
        return new FileInfo(userId,
                filename,
                fileURI + "/" + RESTFilesServer.SERVICE + "/" + filename + "_" + userId,
                new HashSet<>());
    }

    private String getEmptiestFileURI() {
        Map<String, Integer> nmrFilesInUris = new HashMap<>();
        Set<String> knownUris = RESTDirServer.discovery.knownUrisOf(RESTFilesServer.SERVICE).keySet();
        for (String uri: knownUris) {
            nmrFilesInUris.put(uri, 0);
        }

        usersFiles.values().forEach(filesMaps -> {
            filesMaps.values().forEach(file ->{
                String uri = UrlParser.extractFileURIFromURL(file.getFileURL());
                if(knownUris.contains(uri)){
                    nmrFilesInUris.put(uri, nmrFilesInUris.get(uri) + 1);
                }
            });
        });

        Iterator<String> it = knownUris.iterator();
        String emptiestUri;
        if(it.hasNext())
            emptiestUri = it.next();
        else
            return null;

        System.out.println("Empties Uri: " + emptiestUri);
        while (it.hasNext()){
            String next = it.next();
            if(nmrFilesInUris.get(next) < nmrFilesInUris.get(emptiestUri))
                emptiestUri = next;
        }

        return emptiestUri;
    }

    @Override
    public synchronized Result<Void> deleteFile(String filename, String userId, String password) {
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
        fileToRemove.getSharedWith().forEach(user ->{
            filesUserAccess.get(user).remove(fileToRemove);
        });
    }

    @Override
    public synchronized Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
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

        if(!filesSharedWithUser.contains(file)){ //filesSharedWithUser.indexOf(file) < 0
            filesSharedWithUser.add(file);
        }
        return Result.ok();
    }

    @Override
    public synchronized Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
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
    public synchronized Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
        Map<String, FileInfo> files = usersFiles.get(userId);
        if(files == null) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        FileInfo file = files.get(filename);
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
    public synchronized Result<List<FileInfo>> lsFile(String userId, String password) {
        return Result.ok(hasAccessTo(userId));
    }

    @Override
    public synchronized Result<List<FileInfo>> deleteAllUserFiles(String userId) {
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


    /**
     *Returns a List of the files which the User has access to, meaning the owned files and
     * files that have been shared with the user
     *
     * @param userId
     * @return List</FileInfo> list of all files the user has access to
     */
    private List<FileInfo> hasAccessTo(String userId){
        List<FileInfo> result = new ArrayList<>();
        Map<String, FileInfo> uf = usersFiles.get(userId);

        if(uf != null){
            List<FileInfo> files = new ArrayList<>(uf.values());
            result.addAll(files);
        }
        List<FileInfo> accesses = filesUserAccess.get(userId);
        if(accesses != null){
            result.addAll(filesUserAccess.get(userId));
        }
        return result;
    }

}
