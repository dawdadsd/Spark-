package sparkanalysis.util;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public class ResponseUtils {
    public static ResponseEntity<Map<String,Object>> success(String message,Object data)
    {
        return ResponseEntity.ok(Map.of(
                "success",true,
                "message",message,
                "data",data
        ));
    }
    public static ResponseEntity<Map<String,Object>> error(int status,String error)
    {
        return ResponseEntity.status(status).body(Map.of(
                "success",false,
                "error",error
        ));
    }

}
