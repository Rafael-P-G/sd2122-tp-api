package tp1.server.resources.rest;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.factories.FilesClientFactory;
import tp1.clients.factories.UsersClientFactory;
import tp1.server.RESTDirServer;
import tp1.server.RESTFilesServer;
import util.ErrorManager;
import util.UrlParser;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Singleton
public class DirectoryResources implements RestDirectory {

    private static Logger Log = Logger.getLogger(DirectoryResources.class.getName());

    private final Directory impl = new JavaDirectory();
    private UsersClientFactory usersFactory;
    private FilesClientFactory filesFactory;


    public DirectoryResources() {
        usersFactory = RESTDirServer.usersFactory;
        filesFactory = RESTDirServer.filesFactory;
    }

    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password){
        var usersClient = usersFactory.getClient();
        var checkUserResult = usersClient.getUser(userId, password);
        if( !checkUserResult.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(checkUserResult));

        var result = impl.writeFile(filename, data, userId, password);
        if( !result.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(result));

        String fileId = filename + "_" + userId;
        String fileUri = UrlParser.extractFileURIFromURL(result.value().getFileURL());
        var filesClient = filesFactory.getClientFromUri(fileUri);
        var writeFileResult = filesClient.writeFile(fileId, data, "");

        if(writeFileResult == null || !writeFileResult.isOK()) {
            if(RESTDirServer.discovery.knownUrisOf(RESTFilesServer.SERVICE).size() < 2){
                impl.deleteFile(filename, userId, password);
                throw new WebApplicationException(ErrorManager.translateResultError(writeFileResult));
            }else{
                result = impl.writeFile(filename, data, userId, password);
                fileUri = UrlParser.extractFileURIFromURL(result.value().getFileURL());
                filesClient = filesFactory.getClientFromUri(fileUri);
                writeFileResult = filesClient.writeFile(fileId, data, "");
                if(writeFileResult == null || !writeFileResult.isOK()){
                    impl.deleteFile(filename, userId, password);
                    throw new WebApplicationException(ErrorManager.translateResultError(writeFileResult));
                }
            }
        }

        return result.value();
    }



    @Override
    public void deleteFile(String filename, String userId, String password) {
        var usersClient = usersFactory.getClient();

        var checkUserResult = usersClient.getUser(userId, password);
        if( !checkUserResult.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(checkUserResult));

        String fileId = filename + "_" + userId;
        var filesClient = filesFactory.getClient();
        var writeFileResult = filesClient.deleteFile(fileId, "");
        if( !writeFileResult.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(writeFileResult));

        var result = impl.deleteFile(filename, userId, password);
        if( !result.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(result));

    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {
        var usersClient = usersFactory.getClient();
        var checkUserResult = usersClient.getUser(userId, password);
        if( !checkUserResult.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(checkUserResult));

        var checkUserShareResult = usersClient.checkUser(userIdShare);
        if( !checkUserShareResult.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(checkUserShareResult));

        var result = impl.shareFile(filename, userId, userIdShare, password);
        if( !result.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(result));
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {
        var usersClient = usersFactory.getClient();
        var checkUserResult = usersClient.getUser(userId, password);
        if( !checkUserResult.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(checkUserResult));

        var checkUserShareResult = usersClient.checkUser(userIdShare);
        if( !checkUserShareResult.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(checkUserResult));

        var result = impl.unshareFile(filename, userId, userIdShare, password);
        if( !result.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(result));
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {
        var usersClient = usersFactory.getClient();
        var checkUserResult = usersClient.checkUser(userId);
        if( !checkUserResult.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(checkUserResult));

        var checkUserShareResult = usersClient.getUser(accUserId, password);
        if( !checkUserShareResult.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(checkUserShareResult));

        var result = impl.getFile(filename, userId, accUserId, password);
        if( result.isOK() ) {
            return result.value();
        }else
            throw new WebApplicationException(ErrorManager.translateResultError(result));
    }


    @Override
    public List<FileInfo> lsFile(String userId, String password) {
        var usersClient = usersFactory.getClient();
        var checkUserResult = usersClient.getUser(userId, password);

        if( !checkUserResult.isOK() )
            throw new WebApplicationException(ErrorManager.translateResultError(checkUserResult));

        var result = impl.lsFile(userId, password);
        if(result.isOK())
            return result.value();
        else
            throw new WebApplicationException(ErrorManager.translateResultError(result));
    }

    @Override
    public List<FileInfo> deleteAllUserFiles(String userId) {
        var result = impl.deleteAllUserFiles(userId);

        if(result.value().isEmpty())
            return result.value();

        //In order to not flood the system with clients, this map was created, where key = serverURI and value = Client,
        // if a FileInfo has a URI that is already being used by a client, the operation will be executed on that client.
        Map<String, Files> clientMap = new ConcurrentHashMap<>();

        for (FileInfo file: result.value()) { //Each file belonging to the user will be deleted, one by one
            String serverURI = UrlParser.extractFileURIFromURL(file.getFileURL()); //extractFileURI(file);
            String fileId = file.getFilename() + "_" + file.getOwner();
            Files client = clientMap.get(serverURI);

            if(client == null){
                client = FilesClientFactory.getClientFromUri(serverURI);
                if(client == null) throw new WebApplicationException(Result.ErrorCode.INTERNAL_ERROR.name());
                clientMap.put(serverURI, client);
            }

            client.deleteFile(fileId, "");
        }

        return result.value();
    }



}






















