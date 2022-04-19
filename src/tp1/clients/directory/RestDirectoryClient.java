package tp1.clients.directory;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.clients.RestClient;
import util.ErrorManager;

import java.net.URI;
import java.util.List;

public class RestDirectoryClient extends RestClient implements Directory {

    private static final String SHARE_DIR = "share";

    final WebTarget target;

    public RestDirectoryClient(URI serverURI){
        super(serverURI);
        target = client.target( serverURI ).path( RestDirectory.PATH );
    }

    @Override
    public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
        return super.reTry( () -> {
            return clt_writeFile(filename, data, userId, password);
        });
    }

    @Override
    public Result<Void> deleteFile(String filename, String userId, String password) {
        return super.reTry( () -> {
            return clt_deleteFile(filename, userId, password);
        });
    }

    @Override
    public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
        return super.reTry( () -> {
            return clt_shareFile(filename, userId, userIdShare,password);
        });
    }

    @Override
    public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
        return super.reTry( () -> {
            return clt_unshareFile(filename, userId, userIdShare,password);
        });
    }

    @Override
    public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
        return super.reTry( () -> {
            return clt_getFile(filename, userId, accUserId,password);
        });
    }

    @Override
    public Result<List<FileInfo>> lsFile(String userId, String password) {
        return super.reTry( () -> {
            return clt_lsFile(userId, password);
        });
    }

    private Result<FileInfo> clt_writeFile(String filename, byte[] data, String userId, String password) {

        Response r = target.path(userId).path(filename)
                .queryParam(RestUsers.PASSWORD, password)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        if( ErrorManager.translateResponseStatus(r.getStatus()) == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return Result.ok(r.readEntity(FileInfo.class));
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return Result.error(ErrorManager.responseErrorToResult(r));
    }

    private Result<Void> clt_deleteFile(String filename, String userId, String password) {
        Response r = target.path(userId).path(filename)
                .queryParam(RestUsers.PASSWORD, password)
                .request()
                .delete();

        if( ErrorManager.translateResponseStatus(r.getStatus()) == Response.Status.OK.getStatusCode())
            return Result.ok();
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

         return Result.error(ErrorManager.responseErrorToResult(r));
    }

    private Result<Void> clt_shareFile(String filename, String userId, String userIdShare, String password) {
        Response r = target.path(userId).path(filename).path(SHARE_DIR).path(userIdShare)
                .queryParam(RestUsers.PASSWORD, password)
                .request()
                .post(Entity.json(null));

        if( ErrorManager.translateResponseStatus(r.getStatus()) == Response.Status.OK.getStatusCode())
            return Result.ok();
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return Result.error(ErrorManager.responseErrorToResult(r));
    }

    private Result<Void> clt_unshareFile(String filename, String userId, String userIdShare, String password) {
        Response r = target.path(userId).path(filename).path(SHARE_DIR).path(userIdShare)
                .queryParam(RestUsers.PASSWORD, password)
                .request()
                .delete();

        if( ErrorManager.translateResponseStatus(r.getStatus()) == Response.Status.OK.getStatusCode() )
            return Result.ok();
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return Result.error(ErrorManager.responseErrorToResult(r));
    }

    private Result<byte[]> clt_getFile(String filename, String userId, String accUserId, String password) {
        Response r = target.path(userId).path(filename)
                .queryParam(RestUsers.PASSWORD, password)
                .queryParam(RestUsers.ACCUSER_ID, accUserId) //TODO ask if is QUERY or USERID
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        if( ErrorManager.translateResponseStatus(r.getStatus()) == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return Result.ok(r.readEntity(new GenericType<byte[]>() {}));
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return Result.error(ErrorManager.responseErrorToResult(r));
    }

    private Result<List<FileInfo>> clt_lsFile(String userId, String password) {
        return null;
    }
}

































