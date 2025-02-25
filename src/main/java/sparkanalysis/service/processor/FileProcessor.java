package sparkanalysis.service.processor;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

/**
 * 文件处理器接口
 * 定义了文件处理的标准操作流程，包括数据处理、保存和类型支持判断
 * 所有具体的文件处理器(如CsvFileProcessor, JingcaiDataProcessor等)都需要实现此接口
 */
public interface FileProcessor {

    /**
     * 处理文件数据
     * 将输入文件转换为Spark DataFrame格式，并进行必要的数据处理
     *
     * @param filePath 输入文件路径
     * @return 处理后的DataFrame数据集
     * @throws RuntimeException 当文件处理失败时抛出
     */
    Dataset<Row> process(String filePath);

    /**
     * 保存处理后的数据
     * 将处理后的DataFrame保存到指定位置
     *
     * @param df DataFrame数据集
     * @param savePath 保存路径
     * @throws RuntimeException 当保存失败时抛出
     */
    void save(Dataset<Row> df, String savePath);

    /**
     * 判断是否支持处理该类型文件
     * 用于在FileProcessorFactory中判断处理器是否能处理指定文件
     *
     * @param filename 文件名
     * @return 如果支持处理该文件返回true，否则返回false
     */
    boolean supports(String filename);
}