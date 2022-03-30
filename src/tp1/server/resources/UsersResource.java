package tp1.server.resources;


import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Singleton
public class UsersResource implements RestUsers {

    private final Map<String,User> users = new HashMap<>();

    private static Logger Log = Logger.getLogger(UsersResource.class.getName());

    public UsersResource() {
    }

    @Override
    public String createUser(User user) {
        Log.info("createUser : " + user);

        // Check if user data is valid
        if(user.getUserId() == null || user.getPassword() == null || user.getFullName() == null ||
                user.getEmail() == null) {
            Log.info("User object invalid.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }

        // Check if userId already exists
        if( users.containsKey(user.getUserId())) {
            Log.info("User already exists.");
            throw new WebApplicationException( Response.Status.CONFLICT );
        }

        //Add the user to the map of users
        users.put(user.getUserId(), user);
        return user.getUserId();
    }


    @Override
    public User getUser(String userId, String password) {
        Log.info("getUser : user = " + userId + "; pwd = " + password);

        // Check if user is valid
        if(userId == null || password == null) {
            Log.info("UserId or passwrod null.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }

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
    }


    @Override
    public User updateUser(String userId, String password, User user) {
        Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);
        // TODO Complete method
        throw new WebApplicationException( Response.Status.NOT_IMPLEMENTED );
    }


    @Override
    public User deleteUser(String userId, String password) {
        Log.info("deleteUser : user = " + userId + "; pwd = " + password);
        // TODO Complete method
        throw new WebApplicationException( Response.Status.NOT_IMPLEMENTED );
    }


    @Override
    public List<User> searchUsers(String pattern) {
        Log.info("searchUsers : pattern = " + pattern);
        // TODO Complete method
        // Check if user is valid
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
        System.out.println(userL);

        return  userL;
    }

}

