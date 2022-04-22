package tp1.server;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import tp1.clients.factories.DirClientFactory;
import tp1.clients.factories.UsersClientFactory;
import tp1.server.discovery.Discovery;
import tp1.server.resources.rest.UsersResources;
import tp1.server.util.GenericExceptionMapper;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.logging.Logger;

public class RESTUsersServer {
    private static Logger Log = Logger.getLogger(RESTUsersServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8080;
    public static final String SERVICE = "users";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static DirClientFactory dirFactory;

    public static Discovery discovery;

    public static void main(String[] args) {
        try {
            //Debug.setLogLevel( Level.INFO, Debug.SD2122 );
            ResourceConfig config = new ResourceConfig();
            config.register(UsersResources.class);
            //config.register(CustomLoggingFilter.class);
            config.register(GenericExceptionMapper.class);

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config);

            discovery = new Discovery(new InetSocketAddress(InetAddress.getLocalHost().getHostName(), PORT), SERVICE, serverURI);
            discovery.start();
            dirFactory = new DirClientFactory();
            Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));
        } catch( Exception e) {
            Log.severe(e.getMessage());
        }
    }
}
