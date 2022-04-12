package tp1.server.resources;


import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Users;

import java.util.ArrayList;
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
        throw new WebApplicationException(result.error().name());

        /*
        // Check if user data is valid
        if(user.getUserId() == null || user.getPassword() == null || user.getFullName() == null ||
                user.getEmail() == null) {
            Log.info("User object invalid.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST ); //400
        }

        // Check if userId already exists
        if( users.containsKey(user.getUserId())) {
            Log.info("User already exists.");
            throw new WebApplicationException( Response.Status.CONFLICT ); //409
        }

        //Add the user to the map of users
        users.put(user.getUserId(), user);
        return user.getUserId();
         */
    }


    @Override
    public User getUser(String userId, String password) {
        Log.info("getUser : user = " + userId + "; pwd = " + password);

        var result = impl.getUser(userId, password);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(result.error().name());
        /*
        User user = users.get(userId);

        // Check if user exists
        if( user == null ) {
            Log.info("User does not exist.");
            throw new WebApplicationException( Response.Status.NOT_FOUND );
        }

        //Check if the password is correct
        if( !user.getPassword().equals( password)) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException( Response.Status.FORBIDDEN );
        }

        return user;

         */
    }


    @Override
    public User updateUser(String userId, String password, User user) {
        Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);
        var result = impl.updateUser(userId, password, user);
        if( result.isOK() )
            return result.value();
        else
        throw new WebApplicationException(result.error().name());
        /*
        // Check if user is valid
        if(userId == null || password == null) {
            Log.info("UserId or password null.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }

        User oldUser = users.get(userId);

        // Check if old user exists
        if( oldUser == null ) {
            Log.info("User does not exist.");
            throw new WebApplicationException( Response.Status.NOT_FOUND );
        }

        //Check if the password is correct
        if( !oldUser.getPassword().equals( password)) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException( Response.Status.FORBIDDEN );
        }

        String newEmail = user.getEmail();
        String email = newEmail != null ? newEmail : oldUser.getEmail();

        String newFullName = user.getFullName();
        String fullName = newFullName != null ? newFullName : oldUser.getFullName();

        String newPassword = user.getPassword();
        String _password = newPassword != null ? newPassword : password;

        User newUser = new User(userId, fullName, email, _password);

        users.replace(userId, newUser);

        return users.get(userId);

         */
    }


    @Override
    public User deleteUser(String userId, String password) {
        Log.info("deleteUser : user = " + userId + "; pwd = " + password);

        var result = impl.deleteUser(userId, password);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(result.error().name());

        /*
        User user = users.get(userId);

        // Check if user exists
        if( user == null ) {
            Log.info("User does not exist.");
            throw new WebApplicationException( Response.Status.NOT_FOUND );
        }

        //Check if the password is correct
        if( !user.getPassword().equals( password)) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException( Response.Status.FORBIDDEN );
        }

        return users.remove(userId);

         */
    }


    @Override
    public List<User> searchUsers(String pattern) {
        Log.info("searchUsers : pattern = " + pattern);
        // Check if user is valid

        var result = impl.searchUsers(pattern);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(result.error().name());

        /*
        if(pattern == null) {
            Log.info("pattern is null.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }

        List<User> userL = new ArrayList<>();

        users.values().forEach(v -> {
            if(v.getFullName().toUpperCase().contains(pattern.toUpperCase())){
                userL.add(new User(v.getUserId(), v.getFullName(), v.getEmail(), ""));
            }
        });

        return  userL;

         */
    }


}

