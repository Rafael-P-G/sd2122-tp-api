package tp1.server.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;

import java.io.File;
import java.net.URI;
import java.util.*;

public class JavaDirectory implements Directory {

    private Map<String, Map<String, FileInfo>> usersFiles;
    private Map<String, List<FileInfo>> filesUserAccess;

    @Override
    public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {

        if(!usersFiles.containsKey(userId))
            return Result.error(Result.ErrorCode.NOT_FOUND);

        if(password == null)
            return Result.error(Result.ErrorCode.FORBIDDEN);

        if(filename == null){
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        //TODO how to get file URL
        FileInfo file = new FileInfo(userId, filename, userId + "/" + filename, new HashSet<>());
        Map<String, FileInfo> files = usersFiles.get(userId);
        if(files == null){
            files = new HashMap<>();
            usersFiles.put(userId, files);
        }
        files.put(filename, file);
        return Result.ok(file);
    }

    @Override
    public Result<Void> deleteFile(String filename, String userId, String password) {

        Map<String, FileInfo> files = usersFiles.get(userId);
        if(files == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        if(files.containsKey(filename))
            return Result.error(Result.ErrorCode.NOT_FOUND);

        if(password == null)
            return Result.error(Result.ErrorCode.FORBIDDEN);    //TODO is this correct how to check for password here

        files.remove(filename);
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

        if(password == null)
            return Result.error(Result.ErrorCode.FORBIDDEN);    //TODO is this correct how to check for password here

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
        Map<String, FileInfo> files = usersFiles.get(userId);
        if(files == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        FileInfo file = files.get(filename);
        if(file == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        if(!file.getSharedWith().contains(accUserId))
            return Result.error(Result.ErrorCode.FORBIDDEN);

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
