package tp1.clients.factories;

import tp1.api.service.util.Directory;
import tp1.clients.directory.RestDirectoryClient;
import tp1.server.RESTDirServer;
import tp1.server.discovery.Discovery;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.logging.Logger;

public class DirClientFactory {

    private static Logger Log = Logger.getLogger(RESTDirServer.class.getName());

    //TODO complete class 57956
    public static final int PORT = 8080;
    public static final String SERVICE = "directory";

    private static Discovery discovery;

    public DirClientFactory(){
        try {
            discovery = new Discovery(new InetSocketAddress(InetAddress.getLocalHost().getHostName(), PORT), SERVICE, "");
            discovery.listener();
        } catch( Exception e) {
            Log.severe(e.getMessage());
        }
    }

    public static Directory getClient() {
        var serverURI = discovery.getOptimalURI(SERVICE);
        if( serverURI.endsWith("rest"))
            return new RestDirectoryClient(URI.create(serverURI) );
        else
            return null;
    }
}
