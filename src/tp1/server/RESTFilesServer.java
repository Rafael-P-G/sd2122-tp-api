package tp1.server;

import java.util.logging.Logger;

public class RESTFilesServer {

    private static Logger Log = Logger.getLogger(RESTFilesServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8080;
    public static final String SERVICE = "FilesService";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    //TODO complete class 57956

    public static void main(String[] args) {

    }
}
