package tp1.server.resources;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.rest.RestUsers;
import tp1.clients.RestClient;
import tp1.server.RESTDirServer;
import tp1.server.RESTFilesServer;
import tp1.server.RESTUsersServer;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Logger;

@Singleton
public class DirectoryResources extends RestClient implements RestDirectory {

    private static Logger Log = Logger.getLogger(DirectoryResources.class.getName());

    //private HashMap<String, FileInfo> files;
    //private HashMap<URI, List<FileInfo>> filesByURI;
    private Map<String, Set<FileInfo>> usersFiles;

    public DirectoryResources()
    {
        //files = new HashMap<>();
        usersFiles = new HashMap<>();
    }

    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password){
        return super.reTry( () -> {
            return clt_writeFile(filename, data, userId, password);
        });
    }

    @Override
    public void deleteFile(String filename, String userId, String password) {

    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {

    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {

    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {
        return new byte[0];
    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) {
        return null;
    }

    private FileInfo clt_writeFile(String filename, byte[] data, String userId, String password) {
        Log.info("writeFile : " + data.toString());
        System.out.println("Entrei no write file");

        //Check if user is correct
        String serverURI = getOptimalURI(RESTUsersServer.SERVICE);
        System.out.println("RestUserServer URI: " + serverURI);
        if(serverURI == null){
            System.out.println("User server URI not found");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }

        WebTarget target = client.target( serverURI ).path( RestUsers.PATH );
        Response r = target.path(userId)
                .queryParam(RestUsers.PASSWORD, password).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        if(r.getStatus() != Response.Status.OK.getStatusCode()){
            System.out.println("User invalid. Status: " + r.getStatus());
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }
        if(!r.readEntity(User.class).getPassword().equals(password)){
            Log.info("Password is incorrect.");
            throw new WebApplicationException( Response.Status.FORBIDDEN );
        }

        //Post Data on file server
        serverURI = getOptimalURI(RESTFilesServer.SERVICE);
        System.out.println("RestFilesServer URI: " + serverURI);
        if(serverURI == null){
            Log.info("File server URI not found");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }
        //Post Data on file server
        target = client.target( serverURI ).path(RestFiles.PATH);
        r = target.path(filename).request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        if(r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity()){
            FileInfo file = new FileInfo(userId, filename, target.path(filename).getUri().toString(), new HashSet<>());
            Set<FileInfo> files = new HashSet<>();
            files.add(file);
            usersFiles.put(userId, files);
            System.out.println("I did it");
            return r.readEntity(FileInfo.class);
        }
        else {
            System.out.println("Error, HTTP error status: " + r.getStatus());
        }

        return null;
    }

    private String getOptimalURI(String serviceName){

        Map<String, LocalTime> urisFiles = RESTDirServer.discovery.knownUrisOf(serviceName);

        var bestURIWrapper = new Object(){String bestURI; int bestTime = -1;};
        final int currentTime = LocalTime.now().toSecondOfDay();

        urisFiles.forEach((k, v) ->{
            if(bestURIWrapper.bestURI == null){
                bestURIWrapper.bestURI = k;
            }
            else {
                if(bestURIWrapper.bestTime == -1){
                    bestURIWrapper.bestTime = v.toSecondOfDay();
                }
                else {
                    int timeDif = currentTime - v.toSecondOfDay();
                    if(timeDif < bestURIWrapper.bestTime){
                        bestURIWrapper.bestTime = v.toSecondOfDay();
                        bestURIWrapper.bestURI = k;
                    }
                }
            }
        });

        return bestURIWrapper.bestURI;
        /*
        var emptiestWrapper = new Object(){  URI emptiest; };
        var minWrapper = new Object(){ int min = 0; };
        filesByURI.forEach((k, v) -> {
            if(minWrapper.min < 0) minWrapper.min = v.size();

            if(v.size() < minWrapper.min)
                emptiestWrapper.emptiest = k;
                });
        return emptiestWrapper.emptiest;

         */
    }
}






















