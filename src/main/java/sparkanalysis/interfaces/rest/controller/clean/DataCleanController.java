package sparkanalysis.interfaces.rest.controller.clean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sparkanalysis.domain.common.model.CommonDataSource;
import sparkanalysis.domain.lottery.model.LotteryDataSource;
import sparkanalysis.service.common.CommonDataCleaner;
import sparkanalysis.service.lottery.LotteryDataCleaner;
import sparkanalysis.model.CleanedData;
import sparkanalysis.infrastructure.util.FileUtils;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files/data-clean")
public class DataCleanController {

    @Autowired
    private CommonDataCleaner commonDataCleaner;

    @Autowired
    private LotteryDataCleaner lotteryDataCleaner;

    @PostMapping("/common")
    public ResponseEntity<?> cleanCommonData(
            @RequestBody CommonDataSource source,
            @RequestParam(required = false, defaultValue = "cleaned/common") String targetPath) {
        try {
            // 1. 验证输入
            if (!FileUtils.exists(source.getSourcePath())) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("源文件不存在: " + source.getSourcePath()));
            }

            // 2. 创建目标目录
            FileUtils.createDirectory(targetPath);

            // 3. 执行数据清洗
            CleanedData cleanedData = commonDataCleaner.cleanData(source);
            
            // 4. 保存清洗后的数据
            commonDataCleaner.saveCleanedData(cleanedData, targetPath);

            // 5. 返回结果
            return ResponseEntity.ok(createSuccessResponse(cleanedData));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("通用数据清洗失败: " + e.getMessage()));
        }
    }

    @PostMapping("/lottery")
    public ResponseEntity<?> cleanLotteryData(
            @RequestBody LotteryDataSource source,
            @RequestParam(required = false, defaultValue = "cleaned/lottery") String targetPath) {
        try {
            // 1. 验证输入
            if (!FileUtils.exists(source.getSourcePath())) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("源文件不存在: " + source.getSourcePath()));
            }

            // 2. 创建目标目录
            FileUtils.createDirectory(targetPath);

            // 3. 执行数据清洗
            CleanedData cleanedData = lotteryDataCleaner.cleanData(source);
            
            // 4. 保存清洗后的数据
            lotteryDataCleaner.saveCleanedData(cleanedData, targetPath);

            // 5. 返回结果
            return ResponseEntity.ok(createSuccessResponse(cleanedData));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("竞彩数据清洗失败: " + e.getMessage()));
        }
    }

    private Map<String, Object> createSuccessResponse(CleanedData cleanedData) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", cleanedData);
        response.put("message", "数据清洗成功");
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
} 