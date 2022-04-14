package tp1.server.resources.rest;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.service.rest.RestFiles;

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
        Log.info("WriteFile : " + fileId);

        if(fileId == null || data == null){
            Log.info("fileId or data is null.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }

        try {
            File file = new File(fileId);
            file.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(fileId);
            outputStream.write(data);
            outputStream.close();

            files.add(fileId);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFile(String fileId, String token) {
        Log.info("deleteFile : file = " + fileId);

        if(fileId == null){
            Log.info("fileId or data is null.");
            throw new WebApplicationException( Response.Status.NOT_FOUND);
        }

        File file = new File(fileId);

        if(file.isFile()){
            file.delete();
            files.remove(fileId);
        }else {
            Log.info("Not a File");
            throw new WebApplicationException( Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public byte[] getFile(String fileId, String token) {
        if(fileId == null){
            Log.info("fileId or data is null.");
            throw new WebApplicationException( Response.Status.NOT_FOUND);
        }

        try {
            File file = new File(fileId);
            byte[] buffer = Files.readAllBytes(file.toPath());

            return  buffer;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.info("Unknown File Related Error");
        throw new WebApplicationException( Response.Status.BAD_REQUEST);
    }
}

