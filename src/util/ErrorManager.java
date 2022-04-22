package util;


import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.service.util.Result;

public class ErrorManager {

    /*
    This class serves as a general translator for managing differences in errorCodes between services and Results
     */

    public static Status translateResultError(Result<?> result){
        System.out.println("translating: " + result.error());
        switch (result.error()){
            case OK: return Response.Status.OK;
            case FORBIDDEN: return  Response.Status.FORBIDDEN;
            case NOT_FOUND: return Response.Status.NOT_FOUND;
            case CONFLICT: return Response.Status.CONFLICT;
            case BAD_REQUEST: return Response.Status.BAD_REQUEST;
            default: return Response.Status.INTERNAL_SERVER_ERROR;
        }
    }

    public static int translateResponseStatus(int statusCode){
        if(statusCode == Status.NO_CONTENT.getStatusCode())
            return Status.OK.getStatusCode();
        return statusCode;
    }


    public static Result.ErrorCode responseErrorToResult(Response r){
        switch (r.getStatusInfo().toEnum()){
            case FORBIDDEN: return  Result.ErrorCode.FORBIDDEN;
            case NOT_FOUND: return Result.ErrorCode.NOT_FOUND;
            case CONFLICT: return Result.ErrorCode.CONFLICT;
            case BAD_REQUEST: return Result.ErrorCode.BAD_REQUEST;
            default: return Result.ErrorCode.INTERNAL_ERROR;
        }
    }
}
