package tp1.server.resources.rest;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Result;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Singleton
public class FilesResources implements RestFiles {


    private static Logger Log = Logger.getLogger(FilesResources.class.getName());

    private List<String> files;

    private final tp1.api.service.util.Files impl = new JavaFiles();

    public FilesResources(){
        files = new ArrayList<>();
    }

    @Override
    public void writeFile(String fileId, byte[] data, String token) {
        Log.info("writeFile : " + fileId);

        var result = impl.writeFile(fileId, data, token);
        if( !result.isOK() )
            throw new WebApplicationException(translateError(result));
    }

    @Override
    public void deleteFile(String fileId, String token) {
        Log.info("deleteFile : file = " + fileId);

        var result = impl.deleteFile(fileId, token);
        if( !result.isOK() )
            throw new WebApplicationException(translateError(result));
    }

    @Override
    public byte[] getFile(String fileId, String token) {
        Log.info("getFile : file = " + fileId);

        var result = impl.getFile(fileId, token);
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(translateError(result));
    }

    private Response.Status translateError(Result<?> result){
        switch (result.error()){
            case FORBIDDEN: return  Response.Status.FORBIDDEN;
            case NOT_FOUND: return Response.Status.NOT_FOUND;
            case CONFLICT: return Response.Status.CONFLICT;
            case BAD_REQUEST: return Response.Status.BAD_REQUEST;
            default: return Response.Status.INTERNAL_SERVER_ERROR;
        }
    }
}

