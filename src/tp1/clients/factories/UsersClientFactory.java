package tp1.clients.factories;

import tp1.api.service.util.Users;
import tp1.clients.users.rest.RestUsersClient;
import tp1.server.RESTUsersServer;
import tp1.server.discovery.Discovery;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.logging.Logger;

public class UsersClientFactory {
    private static Logger Log = Logger.getLogger(RESTUsersServer.class.getName());

    public static final int PORT = 8080;
    public static final String SERVICE = "users";

    private static Discovery discovery;

    public UsersClientFactory(){
        try {
            discovery = new Discovery(new InetSocketAddress(InetAddress.getLocalHost().getHostName(), PORT), SERVICE, "");
            discovery.listener();
        } catch( Exception e) {
            Log.severe(e.getMessage());
        }
    }

    public static Users getClient() {
        var serverURI = discovery.getOptimalURI(SERVICE); // use discovery to find a uri of the Users service;
        if( serverURI.endsWith("rest"))
            return new RestUsersClient(URI.create(serverURI) );
       else
        return null; //new SoapUsersClient( serverURI );
    }
}
