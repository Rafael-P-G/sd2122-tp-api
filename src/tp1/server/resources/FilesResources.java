package tp1.server.resources;

import com.sun.xml.ws.api.model.wsdl.WSDLOutput;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestFiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Logger;

@Singleton
public class FilesResources implements RestFiles {

    private static final String STORAGE_PATH = "./fileStorage/";

    private static Logger Log = Logger.getLogger(FilesResources.class.getName());

    private HashMap<String, FileInfo> files;

    public FilesResources(){
    }

    @Override
    public void writeFile(String fileId, byte[] data, String token) {
        Log.info("WriteFile : " + fileId);

        if(fileId == null || data == null){
            Log.info("fileId or data is null.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }

        try {
            String filePath = fileId;
            File file = new File(filePath);
            System.out.println("is directory: " + file.isDirectory());
            System.out.println("is file: " + file.isFile());
            System.out.println("does exist: " + file.exists());
            file.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(filePath);
            outputStream.write(data);
            outputStream.close();
            System.out.println("data written");
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

        String filePath = STORAGE_PATH + fileId;
        File file = new File(filePath);

        if(file.isFile()){
            file.delete();
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
            String filePath = STORAGE_PATH + fileId;
            File file = new File(filePath);
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

