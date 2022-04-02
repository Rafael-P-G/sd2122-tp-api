package tp1.server.resources;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.server.internal.process.Endpoint;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestUsers;
import tp1.server.RESTDirServer;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class DirectoryResources implements RestDirectory {

    private static Logger Log = Logger.getLogger(DirectoryResources.class.getName());

    private HashMap<String, FileInfo> files;

    public DirectoryResources() {
        files = new HashMap<>();
    }

    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
        Log.info("writeFile : " + data.toString());

        return null;
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
}

