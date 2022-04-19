package tp1.clients.users.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.RestClient;
import util.ErrorManager;

import java.net.URI;
import java.util.List;

public class RestUsersClient  extends RestClient implements Users {

    final WebTarget target;

    public RestUsersClient( URI serverURI ) {
        super( serverURI );
        target = client.target( serverURI ).path( RestUsers.PATH );
    }

    @Override
    public Result<String> createUser(User user) {
        return super.reTry( () -> {
            return clt_createUser( user );
        });
    }

    @Override
    public Result<User> getUser(String userId, String password) {
        return super.reTry( () -> {
            return clt_getUser( userId, password );
        });
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        return super.reTry( () -> {
            return clt_updateUser(userId, password, user);
        });
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        return super.reTry( () -> {
            return clt_deleteUser(userId, password);
        });
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        return super.reTry( () -> {
            return clt_searchUsers( pattern );
        });
    }

    @Override
    public Result<Void> checkUser(String userId) {
        return super.reTry( () -> {
            return clt_checkUsers( userId );
        });
    }

    private Result<User> clt_deleteUser(String userId, String password) {
        Response r = target.path(userId)
                .queryParam(RestUsers.PASSWORD, password).request()
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return Result.ok(r.readEntity(User.class));
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return null;
    }


    private Result<String> clt_createUser( User user) {

        Response r = target.request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(user, MediaType.APPLICATION_JSON));

        if( ErrorManager.translateResponseStatus(r.getStatus()) == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return Result.ok(r.readEntity(String.class));
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return null;
    }

    private Result<List<User>> clt_searchUsers(String pattern) {

        Response r = target
                .queryParam(RestUsers.QUERY, pattern)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        if( ErrorManager.translateResponseStatus(r.getStatus()) == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return Result.ok(r.readEntity(new GenericType<List<User>>() {}));
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return null;
    }

    private Result<User> clt_getUser(String userId, String password){
        Response r = target
                .path(userId)
                .queryParam(RestUsers.PASSWORD, password).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        if( ErrorManager.translateResponseStatus(r.getStatus()) == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return Result.ok(r.readEntity(User.class));
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return Result.error(ErrorManager.responseErrorToResult(r));
    }

    private Result<User> clt_updateUser(String userId, String password, User user){
        Response r = target.path( userId )
                .queryParam(RestUsers.PASSWORD, password).request()
                .accept(MediaType.APPLICATION_JSON)
                .put(Entity.entity(user, MediaType.APPLICATION_JSON));

        if( ErrorManager.translateResponseStatus(r.getStatus()) == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return Result.ok(r.readEntity(User.class));
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return null;
    }

    private Result<Void> clt_checkUsers(String userId) {
        Response r = target.path(userId)
                .request()
                .get();

        if( ErrorManager.translateResponseStatus(r.getStatus()) == Response.Status.OK.getStatusCode())
            return Result.ok();
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return null;
    }
}
