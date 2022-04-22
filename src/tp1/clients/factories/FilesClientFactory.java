package tp1.clients.factories;

import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.clients.directory.RestDirectoryClient;
import tp1.clients.files.RestFilesClient;
import tp1.server.RESTDirServer;
import tp1.server.RESTFilesServer;
import tp1.server.discovery.Discovery;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.logging.Logger;

public class FilesClientFactory {
    private static Logger Log = Logger.getLogger(RESTFilesServer.class.getName());

    public static final int PORT = 8080;
    public static final String SERVICE = "factory";
    public static final String SERVICE_TO_RETURN = "files";

    private static Discovery discovery;

    public FilesClientFactory(){
        try {
            discovery = new Discovery(new InetSocketAddress(InetAddress.getLocalHost().getHostName(), PORT), SERVICE, "");
            discovery.listener();
        } catch( Exception e) {
            Log.severe(e.getMessage());
        }
    }

    public static Files getClient() {
        var serverURI = discovery.getOptimalURI(SERVICE_TO_RETURN);
        if( serverURI.endsWith("rest")){
            return new RestFilesClient(URI.create(serverURI) );
        }
        else
            return null;
    }

    public static Files getClientFromUri(String serverURI) {
        if( serverURI.endsWith("rest")){
            return new RestFilesClient(URI.create(serverURI) );
        }
        else
            return null;
    }
}
