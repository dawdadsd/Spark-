package sparkanalysis.service.analysis;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import sparkanalysis.domain.analysis.model.AnalysisRequest;

public interface AnalysisService {
    /**
     * 导入数据
     * @param filePath 文件路径
     * @return 数据集
     */
    Dataset<Row> importData(String filePath);

    /**
     * 分析数据
     * @param dataset 数据集
     * @param request 分析请求
     * @return 分析结果
     */
    Dataset<Row> analyzeData(Dataset<Row> dataset, AnalysisRequest request);

    /**
     * 导出结果
     * @param result 分析结果
     * @param outputPath 输出路径
     */
    void exportResult(Dataset<Row> result, String outputPath);

    /**
     * 保存到Hadoop
     * @param dataset 数据集
     * @param hadoopPath Hadoop路径
     */
    void saveToHadoop(Dataset<Row> dataset, String hadoopPath);
} 