package tp1.clients.directory;

import util.Debug;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WriteFileClient {

    private static Logger Log = Logger.getLogger(WriteFileClient.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static void main(String[] args) {
        Debug.setLogLevel( Level.FINE, Debug.SD2122 );

        if (args.length != 5) {
            System.err.println("Use: java tp1.clients.directory.WriteFileClient url filename data userId password");
            return;
        }

        //TODO complete class 57956
        //arg variables
        String serverUrl = args[0];
        String userId = args[1];
        String filename = args[2];
        String password = args[3];
        String data = args[4];

        Log.info("Sending request to server.");

        var result = new RestDirectoryClient(URI.create(serverUrl)).writeFile(filename, data.getBytes(), userId, password);
        System.out.println("Result: " + result);
    }
}
