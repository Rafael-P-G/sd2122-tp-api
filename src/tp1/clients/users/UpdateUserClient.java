package tp1.clients.users;

import tp1.api.User;

import java.io.IOException;
import java.net.URI;

public class UpdateUserClient {


    public static void main(String[] args) throws IOException {

        if( args.length != 6) {
            System.err.println( "Use: java sd2122.tp1.clients.UpdateUserClient url userId oldPassword newFullName newEmail newPassword");
            return;
        }

        String serverUrl = args[0];
        String userId = args[1];
        String oldPassword = args[2];
        String newFullName = args[3];
        String newEmail = args[4];
        String newPassword = args[5];

        User u = new User( userId, newFullName, newEmail, newPassword);

        System.out.println("Sending request to tp1.server.");

        var result = new  RestUsersClient(URI.create(serverUrl)).updateUser(userId, oldPassword, u);
        System.out.println("Result: " + result);

    }
}

