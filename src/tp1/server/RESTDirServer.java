package tp1.server;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import tp1.clients.factories.FilesClientFactory;
import tp1.clients.factories.UsersClientFactory;
import tp1.server.discovery.Discovery;
import tp1.server.resources.rest.DirectoryResources;
import tp1.server.util.GenericExceptionMapper;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.logging.Logger;

public class RESTDirServer {

    private static Logger Log = Logger.getLogger(RESTDirServer.class.getName());

    //TODO complete class 57956
    public static final int PORT = 8080;
    public static final String SERVICE = "directory";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static Discovery discovery;
    public static UsersClientFactory usersFactory;
    public static FilesClientFactory filesFactory;


    public static void main(String[] args) {
        try {
            //Debug.setLogLevel( Level.INFO, Debug.SD2122 );

            ResourceConfig config = new ResourceConfig();
            config.register(DirectoryResources.class);
            //config.register(CustomLoggingFilter.class);
            config.register(GenericExceptionMapper.class);

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config);

            discovery = new Discovery(new InetSocketAddress(InetAddress.getLocalHost().getHostName(), PORT), SERVICE, serverURI);
            discovery.start();
            usersFactory = new UsersClientFactory();
            filesFactory = new FilesClientFactory();
            Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));
            //More code can be executed here...
        } catch( Exception e) {
            Log.severe(e.getMessage());
        }
    }

}
