package tp1.server.resources.rest;


import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Users;
import tp1.clients.factories.DirClientFactory;
import tp1.clients.factories.FilesClientFactory;
import tp1.clients.factories.UsersClientFactory;
import tp1.server.RESTDirServer;
import tp1.server.RESTUsersServer;
import util.ErrorManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Singleton
public class UsersResources implements RestUsers {

    private final Map<String,User> users = new HashMap<>();

    private static Logger Log = Logger.getLogger(UsersResources.class.getName());

    final Users impl = new JavaUsers();
    private DirClientFactory dirFactory;

    public UsersResources() {
        dirFactory = RESTUsersServer.dirFactory;
    }

    @Override
    public String createUser(User user) {
        Log.info("createUser : " + user);

        var result = impl.createUser( user );
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(ErrorManager.translateResultError(result));
    }


    @Override
    public User getUser(String userId, String password) {
        Log.info("getUser : user = " + userId + "; pwd = " + password);
        var result = impl.getUser(userId, password);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(ErrorManager.translateResultError(result));
    }


    @Override
    public User updateUser(String userId, String password, User user) {
        Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);
        var result = impl.updateUser(userId, password, user);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(ErrorManager.translateResultError(result));
    }


    @Override
    public User deleteUser(String userId, String password) {
        Log.info("deleteUser : user = " + userId + "; pwd = " + password);

        var result = impl.deleteUser(userId, password);
        if( !result.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(result));

        var dirClient = dirFactory.getClient();
        if(dirClient != null) {
            dirClient.deleteAllUserFiles(userId);
        }

        return result.value();
    }


    @Override
    public List<User> searchUsers(String pattern) {
        Log.info("searchUsers : pattern = " + pattern);

        var result = impl.searchUsers(pattern);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(ErrorManager.translateResultError(result));
    }

    @Override
    public void checkUser(String userId) {
        Log.info("getUser : user = " + userId);

        var result = impl.checkUser(userId);
        if( !result.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(result));
    }

}

