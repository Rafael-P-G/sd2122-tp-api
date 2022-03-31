package tp1.clients.directory;

import tp1.api.FileInfo;
import tp1.clients.users.RestUsersClient;
import util.Debug;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
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
            System.err.println("Use: java tp1.clients.directory.WriteFileClient url userId fullName email password");
            return;
        }

        //TODO complete class 57956
        //arg variables

        Log.info("Sending request to server.");

        //var result = new RestDirectoryClient(URI.create(serverUrl)).writeFile(???);
        //System.out.println("Result: " + result);
    }
}
