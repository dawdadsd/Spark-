package sparkanalysis.service;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sparkanalysis.service.processor.FileProcessor;
import sparkanalysis.service.processor.factory.FileProcessorFactory;
import java.nio.file.Paths;
@Service
public class AsyncTaskService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncTaskService.class);

    @Autowired
    private FileProcessorFactory processorFactory;

    /**
     * 提交异步处理任务
     */
    @Async("taskExecutor") // 需要配置线程池
    public void processFileAsync(String caseId, String filename) {
        try {
            FileProcessor processor = processorFactory.getProcessor(filename);
            Dataset<Row> data = processor.process(getFilePath(caseId, filename));
            processor.save(data, getSavePath(caseId));
        } catch (Exception e) {
            logger.error("异步处理失败: caseId={}, filename={}", caseId, filename, e);
        }
    }

    private String getFilePath(String caseId, String filename) {
        return Paths.get(FileStorageService.BASE_DIR, caseId, filename).toString();
    }

    private String getSavePath(String caseId) {
        return Paths.get(FileStorageService.BASE_DIR, caseId, "processed").toString();
    }
}