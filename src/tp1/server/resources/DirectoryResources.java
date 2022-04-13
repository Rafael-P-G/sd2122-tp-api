package tp1.server.resources;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.RestClient;
import tp1.clients.factories.FilesClientFactory;
import tp1.clients.factories.UsersClientFactory;
import tp1.server.RESTDirServer;

import java.time.LocalTime;
import java.util.*;
import java.util.logging.Logger;

@Singleton
public class DirectoryResources implements RestDirectory {

    private static Logger Log = Logger.getLogger(DirectoryResources.class.getName());

    private final Directory impl = new JavaDirectory();
    private final Users usersClient;
    private final Files filesClient;

    public DirectoryResources()
    {
        usersClient = UsersClientFactory.getClient();
        filesClient = FilesClientFactory.getClient();
    }

    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password){
        var checkUserResult = usersClient.getUser(userId, password);
        if( !checkUserResult.isOK() )
            throw new WebApplicationException(translateError(checkUserResult));

        String fileId = filename + "_" + userId;
        var writeFileResult = filesClient.writeFile(fileId, data, "");
        if( !writeFileResult.isOK() )
            throw new WebApplicationException(translateError(writeFileResult));

        var result = impl.writeFile(filename, data, userId, password);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(translateError(result));

    }

    @Override
    public void deleteFile(String filename, String userId, String password) {
        var checkUserResult = usersClient.getUser(userId, password);
        if( !checkUserResult.isOK() )
            throw new WebApplicationException(translateError(checkUserResult));

        String fileId = filename + "_" + userId;
        var writeFileResult = filesClient.deleteFile(fileId, "");
        if( !writeFileResult.isOK() )
            throw new WebApplicationException(translateError(writeFileResult));

        var result = impl.deleteFile(filename, userId, password);
        if( !result.isOK() )
            throw new WebApplicationException(translateError(result));

    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {
        var checkUserResult = usersClient.getUser(userId, password);
        if( !checkUserResult.isOK() )
            throw new WebApplicationException(translateError(checkUserResult));

        var checkUserShareResult = usersClient.checkUser(userIdShare);
        if( !checkUserShareResult.isOK() )
            throw new WebApplicationException(translateError(checkUserResult));

        var result = impl.shareFile(filename, userId, userIdShare, password);
        if( !result.isOK() )
            throw new WebApplicationException(translateError(result));
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {
        var checkUserResult = usersClient.getUser(userId, password);
        if( !checkUserResult.isOK() )
            throw new WebApplicationException(translateError(checkUserResult));

        var checkUserShareResult = usersClient.checkUser(userIdShare);
        if( !checkUserShareResult.isOK() )
            throw new WebApplicationException(translateError(checkUserResult));

        var result = impl.unshareFile(filename, userId, userIdShare, password);
        if( !result.isOK() )
            throw new WebApplicationException(translateError(result));
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {
        var checkUserResult = usersClient.checkUser(userId);
        if( !checkUserResult.isOK() )
            throw new WebApplicationException(translateError(checkUserResult));

        var checkUserShareResult = usersClient.getUser(accUserId, password);
        if( !checkUserShareResult.isOK() )
            throw new WebApplicationException(translateError(checkUserResult));

        var result = impl.getFile(filename, userId, accUserId, password);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(translateError(result));
    }


    @Override
    public List<FileInfo> lsFile(String userId, String password) {
        var checkUserResult = usersClient.getUser(userId, password);
        if( !checkUserResult.isOK() )
            throw new WebApplicationException(translateError(checkUserResult));

        var result = impl.lsFile(userId, password);
        if(result.isOK())
            return result.value();
        else
            throw new WebApplicationException(translateError(result));
    }

    private Response.Status translateError(Result<?> result){
        switch (result.error()){
            case FORBIDDEN: return  Response.Status.FORBIDDEN;
            case NOT_FOUND: return Response.Status.NOT_FOUND;
            case CONFLICT: return Response.Status.CONFLICT;
            case BAD_REQUEST: return Response.Status.BAD_REQUEST;
            default: return Response.Status.INTERNAL_SERVER_ERROR;
        }
    }
}






















