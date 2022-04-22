package tp1.clients.directory;

import util.Debug;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListFilesClient {

    private static Logger Log = Logger.getLogger(ListFilesClient.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }
    public static void main(String[] args) {
        Debug.setLogLevel( Level.FINE, Debug.SD2122 );

        if (args.length != 3) {
            System.err.println("Use: java tp1.clients.directory.rest.ListFilesClient serverUrl userId password");
            return;
        }

        String serverUrl = args[0];
        String userId = args[1];
        String password = args[2];

        Log.info("Sending request to server.");

        var result = new RestDirectoryClient(URI.create(serverUrl)).lsFile(userId, password);
        System.out.println("Result: " + result);
    }
}
