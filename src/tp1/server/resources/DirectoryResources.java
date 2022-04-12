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
        super.reTry( () -> {
                clt_deleteFile(filename, userId, userId, password);
                return null;
        });
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {
         super.reTry( () -> {
             clt_shareFile(filename, userId, userIdShare, password);
             return null;
        });
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {
        super.reTry( () -> {
            clt_unshareFile(filename, userId, userIdShare, password);
            return null;
        });
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {

        return super.reTry( () -> {
             return clt_getFile(filename, userId, accUserId, password);
        });
    }


    @Override
    public List<FileInfo> lsFile(String userId, String password) {

       return super.reTry( () -> {
            return clt_lsFile(userId, password);
        });
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

        //Post Data on file server
        serverURI = getOptimalURI(RESTFilesServer.SERVICE);
        System.out.println("RestFilesServer URI: " + serverURI);
        if(serverURI == null){
            Log.info("File server URI not found");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }

        target = client.target( serverURI ).path(RestFiles.PATH);
        //String uniqueID = UUID.randomUUID().toString();
        r = target.path( userId + "_" + filename)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        System.out.println(r.toString());
        if(r.getStatus() == Response.Status.NO_CONTENT.getStatusCode()){
            FileInfo file = new FileInfo(userId, filename, target.path( userId + "_" + filename).getUri().toString(), new HashSet<>());
            Set<FileInfo> files = new HashSet<>();
            files.add(file);
            usersFiles.put(userId, files);
            System.out.println("I did it");
            return file;
        }
        else {
            System.out.println("Error, HTTP error status: " + r.getStatus());
        }

        return null;
    }

    private void clt_deleteFile(String filename, String userId, String userId1, String password) {
    }

    private void clt_shareFile(String filename, String userId, String userIdShare, String password) {
    }

    private void clt_unshareFile(String filename, String userId, String userIdShare, String password) {
    }

    private byte[] clt_getFile(String filename, String userId, String accUserId, String password) {
        return null;
    }

    private List<FileInfo> clt_lsFile(String userId, String password) {
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
    }
}






















