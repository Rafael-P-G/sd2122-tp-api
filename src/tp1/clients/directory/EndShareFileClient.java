package tp1.clients.directory;

import util.Debug;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EndShareFileClient {

    private static Logger Log = Logger.getLogger(EndShareFileClient.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }
    public static void main(String[] args) {
        //TODO Change class name to "UnshareFileClient"
        Debug.setLogLevel( Level.FINE, Debug.SD2122 );

        if (args.length != 5) {
            System.err.println("Use: java tp1.clients.directory.rest.UnshareFileClient serverUrl filename userId userIdShare password");
            return;
        }

        String serverUrl = args[0];
        String filename = args[1];
        String userId = args[2];
        String userIdShare = args[3];
        String password = args[4];

        Log.info("Sending request to server.");

        var result = new RestDirectoryClient(URI.create(serverUrl)).unshareFile(filename, userId, userIdShare, password);
        System.out.println("Result: " + result);
    }
}
