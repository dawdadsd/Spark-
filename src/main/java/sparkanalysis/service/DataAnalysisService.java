package sparkanalysis.service;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.stereotype.Service;
import sparkanalysis.model.AnalysisRequest;

/**
 * 数据分析服务接口
 * 定义了数据分析的完整流程，包括数据导入、分析和导出
 * 使用Spark进行大数据处理和分析
 */
@Service("dataAnalysisServiceImpl")
public interface DataAnalysisService {

    /**
     * 导入数据
     * 将指定路径的数据文件加载为Spark DataFrame
     *
     * @param filePath 数据文件路径
     * @return 加载后的DataFrame数据集
     * @throws RuntimeException 当数据导入失败时抛出
     */
    Dataset<Row> importData(String filePath);

    /**
     * 分析数据
     * 根据分析请求对数据集进行处理和分析
     *
     * @param dataset 待分析的DataFrame数据集
     * @param request 分析请求参数对象
     * @return 分析后的DataFrame结果集
     * @throws RuntimeException 当分析过程出错时抛出
     */
    Dataset<Row> analyzeData(Dataset<Row> dataset, AnalysisRequest request);

    /**
     * 导出结果
     * 将分析结果保存到指定路径
     *
     * @param result 分析结果DataFrame
     * @param outputPath 输出路径
     * @throws RuntimeException 当结果导出失败时抛出
     */
    void exportResult(Dataset<Row> result, String outputPath);
    void saveToHadoop(Dataset<Row> dataset, String hadoopPath);
}