package tp1.clients.directory;

import util.Debug;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteFileClient {

    private static Logger Log = Logger.getLogger(DeleteFileClient.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }
    public static void main(String[] args) {
        Debug.setLogLevel( Level.FINE, Debug.SD2122 );

        if (args.length != 4) {
            System.err.println("Use: java tp1.clients.directory.rest.DeleteFileClient serverUrl filename userId password");
            return;
        }

        String serverUrl = args[0];
        String filename = args[1];
        String userId = args[2];
        String password = args[3];

        Log.info("Sending request to server.");

        var result = new RestDirectoryClient(URI.create(serverUrl)).deleteFile(filename, userId, password);
        System.out.println("Result: " + result);
    }
}
