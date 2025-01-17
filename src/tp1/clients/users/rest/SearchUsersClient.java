package tp1.clients.users.rest;

import util.Debug;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchUsersClient {

    private static Logger Log = Logger.getLogger(SearchUsersClient.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static void main(String[] args) throws IOException {

        Debug.setLogLevel(Level.FINE, Debug.SD2122);

        if (args.length != 2) {
            System.err.println("Use: java sd2122.tp1.clients.SearchUsersClient url pattern ");
            return;
        }

        String serverUrl = args[0];
        String pattern = args[1];


        Log.info("Sending request to server.");

        var result = new RestUsersClient(URI.create(serverUrl)).searchUsers(pattern);
        System.out.println("Result: " + result);

    }

}
