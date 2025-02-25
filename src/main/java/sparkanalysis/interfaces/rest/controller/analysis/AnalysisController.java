package sparkanalysis.interfaces.rest.controller.analysis;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sparkanalysis.domain.analysis.model.AnalysisRequest;
import sparkanalysis.service.analysis.AnalysisService;
import sparkanalysis.infrastructure.util.FileUtils;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeData(@RequestBody AnalysisRequest request) {
        try {
            // 1. 验证输入
            if (!FileUtils.exists(request.getFilePath())) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("源文件不存在: " + request.getFilePath()));
            }

            // 2. 导入数据
            Dataset<Row> dataset = analysisService.importData(request.getFilePath());

            // 3. 执行分析
            Dataset<Row> result = analysisService.analyzeData(dataset, request);

            // 4. 导出结果
            String outputPath = "analysis/result_" + System.currentTimeMillis();
            analysisService.exportResult(result, outputPath);

            // 5. 返回结果
            return ResponseEntity.ok(createSuccessResponse(outputPath, result.count()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("数据分析失败: " + e.getMessage()));
        }
    }

    @PostMapping("/hadoop")
    public ResponseEntity<?> saveToHadoop(
            @RequestBody AnalysisRequest request,
            @RequestParam String hadoopPath) {
        try {
            // 1. 验证输入
            if (!FileUtils.exists(request.getFilePath())) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("源文件不存在: " + request.getFilePath()));
            }

            // 2. 导入数据
            Dataset<Row> dataset = analysisService.importData(request.getFilePath());

            // 3. 保存到Hadoop
            analysisService.saveToHadoop(dataset, hadoopPath);

            // 4. 返回结果
            return ResponseEntity.ok(createSuccessResponse(hadoopPath, dataset.count()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("保存到Hadoop失败: " + e.getMessage()));
        }
    }

    private Map<String, Object> createSuccessResponse(String path, long count) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("path", path);
        response.put("count", count);
        response.put("message", "操作成功");
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
} 