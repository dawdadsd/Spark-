package sparkanalysis.service.processor.impl;

import org.slf4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sparkanalysis.service.processor.FileProcessor;

/**
 * CSV文件处理器
 * 实现FileProcessor接口，专门处理CSV格式文件的读取、处理和保存
 */
@Component
public class CsvFileProcessor implements FileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CsvFileProcessor.class);
    @Autowired
    private SparkSession spark;
    /**
     * 处理CSV文件
     * 将CSV文件读取为Spark DataFrame格式
     *
     * @param filePath 文件路径，支持file:///格式的本地文件路径
     * @return 处理后的DataFrame
     * @throws RuntimeException 当文件处理失败时抛出
     */
    @Override
    public Dataset<Row> process(String filePath) {
        logger.info("正在处理CSV文件: {}", filePath);
        try {
            return spark.read()
                    .option("header", "true")
                    .option("inferSchema", "true")
                    .option("encoding", "GBK")
                    .csv(filePath);
        } catch (Exception e) {
            logger.error("处理CSV文件失败: {}", filePath, e);
            throw new RuntimeException("处理CSV文件失败: " + filePath, e);
        }
    }
    /**
     * 保存处理后的数据
     * 将DataFrame保存为CSV格式
     *
     * @param df DataFrame对象，包含要保存的数据
     * @param savePath 保存路径
     * @throws RuntimeException 当保存失败时抛出
     */
    @Override
    public void save(Dataset<Row> df, String savePath) {
        logger.info("保存结果到CSV文件: {}", savePath);
        try {
            df.write()
                    .option("header", "true")
                    .mode("overwrite")
                    .csv(savePath);
        } catch (Exception e) {
            logger.error("保存CSV文件失败: {}", savePath, e);
            throw new RuntimeException("保存CSV文件失败: " + savePath, e);
        }
    }

    /**
     * 判断是否支持处理该文件
     * 通过文件扩展名判断是否为CSV文件
     *
     * @param filename 文件名
     * @return 如果是CSV文件返回true，否则返回false
     */
    @Override
    public boolean supports(String filename) {
        return filename.toLowerCase().endsWith(".csv");
    }
}