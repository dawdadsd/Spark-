package sparkanalysis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sparkanalysis.service.FileService;
import sparkanalysis.service.FileStorageService;
import sparkanalysis.util.ResponseUtils;
import sparkanalysis.util.AsyncUtils;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.NumericType;
import java.util.Optional;
import java.nio.file.Path;

/**
 * 文件处理控制器
 * 负责处理所有与文件相关的HTTP请求，包括上传、预览和列表获取等功能
 */
@RestController
@RequestMapping("/api/files")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;
    private final FileStorageService storageService;

    @Autowired
    private SparkSession sparkSession;

    /**
     * 构造函数，通过依赖注入初始化服务
     * @param fileService 文件服务实例
     * @param storageService 存储服务实例
     */
    @Autowired
    public FileController(FileService fileService, FileStorageService storageService) {
        this.fileService = fileService;
        this.storageService = storageService;
    }

    /**
     * 文件上传接口
     * 处理文件上传请求，支持异步处理文件
     *
     * @param caseId 案例ID，用于区分不同的文件组
     * @param file 上传的文件对象
     * @return 上传结果，包含案例ID和文件名
     */
    @PostMapping("/{caseId}/upload")
    public ResponseEntity<Map<String,Object>> uploadFile(
            @PathVariable String caseId,
            @RequestParam("file") MultipartFile file) {
        try {
            storageService.validate(file);
            String filename = storageService.store(caseId, file);
            // 异步处理文件
            AsyncUtils.runAsync(
                    () -> fileService.processFile(caseId, filename),
                    ex -> LogError("文件处理失败在FileController中", ex)
            );
            return ResponseUtils.success("文件上传成功", Map.of(
                    "caseId", caseId,
                    "filename", filename
            ));
        } catch (Exception e) {
            return ResponseUtils.error(400, e.getMessage());
        }
    }

    /**
     * 获取文件列表接口
     * 返回指定案例ID下的所有文件列表
     *
     * @param caseId 案例ID
     * @return 文件列表数据
     */
    @GetMapping("/{caseId}/list")
    public ResponseEntity<Map<String,Object>> listFiles(@PathVariable String caseId) {
        try {
            return ResponseUtils.success("文件列表获取成功",
                    fileService.getFilesList(caseId));
        } catch (Exception e) {
            return ResponseUtils.error(400, e.getMessage());
        }
    }

    /**
     * 文件预览接口
     * 提供文件内容的预览功能
     *
     * @param caseId 案例ID
     * @param filename 文件名
     * @return 文件预览数据
     */
    @GetMapping("/{caseId}/preview")
    public ResponseEntity<Map<String,Object>> previewData(
            @PathVariable String caseId,
            @RequestParam String filename) {
        try {
            return ResponseUtils.success("文件预览成功",
                    fileService.previewData(caseId, filename));
        } catch (Exception e) {
            return ResponseUtils.error(400, e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getFilesList(@RequestParam(required = false) String directory) {
        try {
            // 默认目录为cleaned目录
            String targetDir = directory != null ? directory : "cleaned";
            List<Map<String, Object>> fileList = new ArrayList<>();
            
            // 使用Files.walk遍历目录
            Files.walk(Paths.get(targetDir))
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("fileName", path.getFileName().toString());
                    fileInfo.put("filePath", path.toString());
                    try {
                        fileInfo.put("fileSize", Files.size(path));
                        fileInfo.put("lastModified", Files.getLastModifiedTime(path).toMillis());
                        fileInfo.put("type", getFileType(path.toString()));
                    } catch (IOException e) {
                        logger.error("获取文件信息失败: {}", e.getMessage());
                    }
                    fileList.add(fileInfo);
                });

            return ResponseEntity.ok(createSuccessResponse("获取文件列表成功", fileList));
        } catch (Exception e) {
            logger.error("获取文件列表失败: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("获取文件列表失败: " + e.getMessage()));
        }
    }

    @GetMapping("/preview")
    public ResponseEntity<?> previewFile(
            @RequestParam(required = false) String filePath,
            @RequestParam(required = false, defaultValue = "cleaned") String directory,
            @RequestParam(defaultValue = "100") int limit) {
        try {
            // 如果没有提供 filePath，则使用 directory 参数
            String targetPath;
            if (filePath == null || filePath.trim().isEmpty()) {
                if (directory == null || directory.trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                        .body(createErrorResponse("必须提供 filePath 或 directory 参数"));
                }
                targetPath = directory;
            } else {
                targetPath = filePath;
            }

            // 验证文件或目录是否存在
            if (!Files.exists(Paths.get(targetPath))) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("文件或目录不存在: " + targetPath));
            }

            // 如果是目录，获取目录下的第一个可用文件
            if (Files.isDirectory(Paths.get(targetPath))) {
                Optional<Path> firstFile = Files.walk(Paths.get(targetPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.toString().toLowerCase();
                        return fileName.endsWith(".parquet") || fileName.endsWith(".csv");
                    })
                    .findFirst();

                if (!firstFile.isPresent()) {
                    return ResponseEntity.badRequest()
                        .body(createErrorResponse("目录中没有可用的数据文件: " + targetPath));
                }
                targetPath = firstFile.get().toString();
            }

            Dataset<Row> data;
            // 根据文件类型读取数据
            if (targetPath.toLowerCase().endsWith(".parquet")) {
                data = sparkSession.read().parquet(targetPath);
            } else if (targetPath.toLowerCase().endsWith(".csv")) {
                data = sparkSession.read()
                    .option("header", "true")
                    .option("encoding", "GBK")
                    .option("inferSchema", "true")
                    .csv(targetPath);
            } else {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("不支持的文件类型: " + targetPath));
            }

            // 获取数据预览
            Map<String, Object> preview = new HashMap<>();
            preview.put("filePath", targetPath);
            preview.put("schema", getSchemaInfo(data));
            preview.put("totalRows", data.count());
            preview.put("previewData", getPreviewData(data, limit));
            preview.put("columnStats", getColumnStats(data));

            return ResponseEntity.ok(createSuccessResponse("获取文件预览成功", preview));
        } catch (Exception e) {
            logger.error("预览文件失败: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("预览文件失败: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getFileStats(@RequestParam String filePath) {
        try {
            if (!Files.exists(Paths.get(filePath))) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("文件不存在: " + filePath));
            }

            Dataset<Row> data;
            if (filePath.endsWith(".parquet")) {
                data = sparkSession.read().parquet(filePath);
            } else if (filePath.endsWith(".csv")) {
                data = sparkSession.read()
                    .option("header", "true")
                    .option("encoding", "GBK")
                    .option("inferSchema", "true")
                    .csv(filePath);
            } else {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("不支持的文件类型"));
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("rowCount", data.count());
            stats.put("columnStats", getColumnStats(data));
            stats.put("fileSize", Files.size(Paths.get(filePath)));
            stats.put("lastModified", Files.getLastModifiedTime(Paths.get(filePath)).toMillis());

            return ResponseEntity.ok(createSuccessResponse("获取文件统计信息成功", stats));
        } catch (Exception e) {
            logger.error("获取文件统计信息失败: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("获取文件统计信息失败: " + e.getMessage()));
        }
    }

    private String getFileType(String filePath) {
        if (filePath.endsWith(".parquet")) {
            return "PARQUET";
        } else if (filePath.endsWith(".csv")) {
            return "CSV";
        } else {
            return "UNKNOWN";
        }
    }

    private List<Map<String, String>> getSchemaInfo(Dataset<Row> data) {
        List<Map<String, String>> schemaInfo = new ArrayList<>();
        for (StructField field : data.schema().fields()) {
            Map<String, String> fieldInfo = new HashMap<>();
            fieldInfo.put("name", field.name());
            fieldInfo.put("type", field.dataType().toString());
            fieldInfo.put("nullable", String.valueOf(field.nullable()));
            schemaInfo.add(fieldInfo);
        }
        return schemaInfo;
    }

    private List<Map<String, Object>> getPreviewData(Dataset<Row> data, int limit) {
        List<Map<String, Object>> previewData = new ArrayList<>();
        List<Row> rows = data.limit(limit).collectAsList();
        for (Row row : rows) {
            Map<String, Object> rowData = new HashMap<>();
            for (StructField field : data.schema().fields()) {
                rowData.put(field.name(), row.get(row.fieldIndex(field.name())));
            }
            previewData.add(rowData);
        }
        return previewData;
    }

    private Map<String, Map<String, Object>> getColumnStats(Dataset<Row> data) {
        Map<String, Map<String, Object>> columnStats = new HashMap<>();
        for (StructField field : data.schema().fields()) {
            Map<String, Object> stats = new HashMap<>();
            String colName = field.name();
            
            try {
                // 获取基本统计信息
                Dataset<Row> colStats = data.select(
                    functions.count(functions.col(colName)).as("count"),
                    functions.countDistinct(functions.col(colName)).as("distinctCount"),
                    functions.when(functions.lit(field.dataType() instanceof NumericType), 
                        functions.expr(String.format("percentile(%s, array(0.25, 0.5, 0.75))", colName)))
                        .otherwise(functions.lit(null)).as("percentiles")
                );
                
                Row statsRow = colStats.first();
                stats.put("count", statsRow.getLong(0));
                stats.put("distinctCount", statsRow.getLong(1));
                
                if (field.dataType() instanceof NumericType) {
                    stats.put("percentiles", statsRow.get(2));
                }
                
                columnStats.put(colName, stats);
            } catch (Exception e) {
                logger.warn("计算列 {} 的统计信息时出错: {}", colName, e.getMessage());
                // 添加基本信息
                stats.put("count", 0L);
                stats.put("distinctCount", 0L);
                columnStats.put(colName, stats);
            }
        }
        return columnStats;
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    /**
     * 错误日志记录方法
     * 将错误信息输出到标准错误流
     *
     * @param message 错误消息
     * @param ex 异常对象
     */
    private void LogError(String message, Throwable ex) {
        System.err.println(message + ":" + ex.getMessage());
        ex.printStackTrace();
    }
}