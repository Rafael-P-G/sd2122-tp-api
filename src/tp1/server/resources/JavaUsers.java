package tp1.server.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.User;
import tp1.api.service.util.Result;
import tp1.api.service.util.Result.ErrorCode;
import tp1.api.service.util.Users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JavaUsers implements Users {
    private static Logger Log = Logger.getLogger(UsersResource.class.getName());

    /*
    esta classe vai ser equivalente ao UsersResources -> vai conter a logica do programa
     */

    private final Map<String,User> users = new HashMap<>();

    public JavaUsers() {}

    @Override
    public Result<String> createUser(User user) {
        Log.info("createUser : " + user);

        // Check if user data is valid
        if(user.getUserId() == null || user.getPassword() == null || user.getFullName() == null ||
                user.getEmail() == null) {
            Log.info("User object invalid.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // Check if userId already exists
        if( users.containsKey(user.getUserId())) {
            Log.info("User already exists.");
            return Result.error(ErrorCode.CONFLICT);
        }

        //Add the user to the map of users
        users.put(user.getUserId(), user);
        return Result.ok(user.getUserId());
    }

    @Override
    public Result<User> getUser(String userId, String password) {
        Log.info("getUser : user = " + userId + "; pwd = " + password);

        User user = users.get(userId);

        // Check if user exists
        if( user == null ) {
            Log.info("User does not exist.");
            System.out.println("User non existant");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        //Check if the password is correct
        if( !user.getPassword().equals( password)) {
            Log.info("Password is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        return Result.ok(user);
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);

        // Check if user is valid
        if(userId == null || password == null) {
            Log.info("UserId or password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        User oldUser = users.get(userId);

        // Check if old user exists
        if( oldUser == null ) {
            Log.info("User does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        //Check if the password is correct
        if( !oldUser.getPassword().equals( password)) {
            Log.info("Password is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        String newEmail = user.getEmail();
        String email = newEmail != null ? newEmail : oldUser.getEmail();

        String newFullName = user.getFullName();
        String fullName = newFullName != null ? newFullName : oldUser.getFullName();

        String newPassword = user.getPassword();
        String _password = newPassword != null ? newPassword : password;

        User newUser = new User(userId, fullName, email, _password);

        users.replace(userId, newUser);

        return Result.ok(users.get(userId));
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        Log.info("deleteUser : user = " + userId + "; pwd = " + password);

        User user = users.get(userId);

        // Check if user exists
        if( user == null ) {
            Log.info("User does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        //Check if the password is correct
        if( !user.getPassword().equals( password)) {
            Log.info("Password is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        users.remove(userId);
        return Result.ok(user);
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        Log.info("searchUsers : pattern = " + pattern);
        // Check if user is valid
        if(pattern == null) {
            Log.info("pattern is null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        List<User> userL = new ArrayList<>();

        users.values().forEach(v -> {
            if(v.getFullName().toUpperCase().contains(pattern.toUpperCase())){
                userL.add(new User(v.getUserId(), v.getFullName(), v.getEmail(), ""));
            }
        });

        return  Result.ok(userL);
    }

    @Override
    public Result<Void> checkUser(String userId) {
        if(!users.containsKey(userId))
            return Result.error(ErrorCode.NOT_FOUND);

        return Result.ok();
    }

}
