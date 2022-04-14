package tp1.server.resources.rest;


import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Singleton
public class UsersResource implements RestUsers {

    private final Map<String,User> users = new HashMap<>();

    private static Logger Log = Logger.getLogger(UsersResource.class.getName());

    final Users impl = new JavaUsers();

    public UsersResource() {

    }

    @Override
    public String createUser(User user) {
        Log.info("createUser : " + user);

        var result = impl.createUser( user );
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(translateError(result));
    }


    @Override
    public User getUser(String userId, String password) {
        Log.info("getUser : user = " + userId + "; pwd = " + password);

        var result = impl.getUser(userId, password);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(translateError(result));
    }


    @Override
    public User updateUser(String userId, String password, User user) {
        Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);
        var result = impl.updateUser(userId, password, user);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(translateError(result));
    }


    @Override
    public User deleteUser(String userId, String password) {
        Log.info("deleteUser : user = " + userId + "; pwd = " + password);

        var result = impl.deleteUser(userId, password);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(translateError(result));
    }


    @Override
    public List<User> searchUsers(String pattern) {
        Log.info("searchUsers : pattern = " + pattern);

        var result = impl.searchUsers(pattern);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(translateError(result));
    }

    @Override
    public void checkUser(String userId) {
        Log.info("getUser : user = " + userId);

        var result = impl.checkUser(userId);
        if( !result.isOK() )
            throw new WebApplicationException(translateError(result));
    }

    private Status translateError(Result<?> result){
        switch (result.error()){
            case FORBIDDEN: return  Status.FORBIDDEN;
            case NOT_FOUND: return Status.NOT_FOUND;
            case CONFLICT: return Status.CONFLICT;
            case BAD_REQUEST: return Status.BAD_REQUEST;
            default: return Status.INTERNAL_SERVER_ERROR;
        }
    }


}

