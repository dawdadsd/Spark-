package sparkanalysis.interfaces.rest.controller.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sparkanalysis.service.ParquetToMysqlService;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/data-import")
public class ParquetToMysqlController {

    @Autowired
    private ParquetToMysqlService parquetToMysqlService;

    @PostMapping("/mysql")
    public ResponseEntity<?> importToMysql(
            @RequestParam String parquetPath,
            @RequestParam String tableName,
            @RequestParam(defaultValue = "overwrite") String saveMode) {
        try {
            parquetToMysqlService.importToMysql(parquetPath, tableName, saveMode);
            return ResponseEntity.ok(createSuccessResponse(tableName));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, Object> createSuccessResponse(String tableName) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "数据已成功导入到MySQL表: " + tableName);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
} 