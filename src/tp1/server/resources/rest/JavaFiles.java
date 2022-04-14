package tp1.server.resources.rest;

import tp1.api.service.util.Files;
import tp1.api.service.util.Result;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JavaFiles implements Files {

    private static Logger Log = Logger.getLogger(FilesResources.class.getName());

    private List<String> files;

    public JavaFiles(){
        files = new ArrayList<>();
    }

    @Override
    public Result<Void> writeFile(String fileId, byte[] data, String token) {
        Log.info("deleteFile : file = " + fileId);

        Log.info("WriteFile : " + fileId);

        if(fileId == null || data == null){
            Log.info("fileId or data is null.");
            return Result.error( Result.ErrorCode.BAD_REQUEST );
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
            return Result.error( Result.ErrorCode.INTERNAL_ERROR );
        }

        return Result.ok();
    }

    @Override
    public Result<Void> deleteFile(String fileId, String token) {

        Log.info("deleteFile : file = " + fileId);

        if(fileId == null){
            Log.info("fileId or data is null.");
            return Result.error( Result.ErrorCode.NOT_FOUND );
        }

        File file = new File(fileId);

        if(file.isFile()){
            file.delete();
            files.remove(fileId);
        }else {
            Log.info("Not a File");
            return Result.error( Result.ErrorCode.BAD_REQUEST );
        }

        return Result.ok();
    }

    @Override
    public Result<byte[]> getFile(String fileId, String token) {

        if(fileId == null){
            Log.info("fileId or data is null.");
            return Result.error( Result.ErrorCode.NOT_FOUND );
        }

        try {
            File file = new File(fileId);
            byte[] buffer = java.nio.file.Files.readAllBytes(file.toPath());

            return  Result.ok(buffer);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Result.error( Result.ErrorCode.INTERNAL_ERROR );
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error( Result.ErrorCode.INTERNAL_ERROR );
        }
    }
}
