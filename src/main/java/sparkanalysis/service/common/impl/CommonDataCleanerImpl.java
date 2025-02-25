package sparkanalysis.service.common.impl;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sparkanalysis.domain.common.model.CommonDataSource;
import sparkanalysis.service.common.CommonDataCleaner;
import sparkanalysis.model.CleanedData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.spark.sql.functions.*;

@Service
public class CommonDataCleanerImpl implements CommonDataCleaner {
    
    private static final Logger logger = LoggerFactory.getLogger(CommonDataCleanerImpl.class);
    
    @Autowired
    private SparkSession sparkSession;
    
    @Override
    public CleanedData cleanData(CommonDataSource source) {
        CleanedData cleanedData = new CleanedData();
        
        try {
            // 1. 读取数据
            Dataset<Row> rawData = sparkSession.read()
                .option("header", "true")
                .option("delimiter", source.getDelimiter())
                .option("encoding", source.getEncoding())
                .option("inferSchema", "true")
                .csv(source.getSourcePath());
            
            logger.info("原始数据行数: {}", rawData.count());
            
            if (rawData.isEmpty()) {
                throw new RuntimeException("源文件为空或无法正确读取数据");
            }
            
            // 2. 基础清洗
            Dataset<Row> cleanedDF = rawData
                .na().drop()  // 删除空值
                .dropDuplicates();  // 删除重复行
            
            logger.info("清洗后数据行数: {}", cleanedDF.count());
            
            if (cleanedDF.isEmpty()) {
                throw new RuntimeException("清洗后数据为空，请检查清洗规则");
            }
            
            // 3. 数据类型转换和标准化
            for (String colName : cleanedDF.columns()) {
                cleanedDF = cleanedDF.withColumn(colName, 
                    when(col(colName).equalTo("NULL"), lit(null))
                    .when(col(colName).equalTo("N/A"), lit(null))
                    .otherwise(col(colName)));
            }
            
            // 4. 设置清洗结果
            String dataId = generateDataId();
            cleanedData.setDataId(dataId);
            cleanedData.setCleanType("COMMON");
            cleanedData.setCleanTimestamp(getCurrentTimestamp());
              // 5. 缓存清洗后的数据用于后续保存
            cleanedDF.persist();
            cleanedData.setStorageLocation("cleaned/common/" + dataId);
            
            // 6. 保存清洗后的数据
            cleanedDF.repartition(10)  // 增加分区数以优化性能
                .write()
                .mode("overwrite")
                .parquet(cleanedData.getStorageLocation());
            
            // 7. 释放缓存
            cleanedDF.unpersist();
            
            logger.info("数据清洗完成，生成的数据ID: {}", dataId);
            
        } catch (Exception e) {
            logger.error("数据清洗失败", e);
            throw new RuntimeException("Spark数据清洗失败: " + e.getMessage());
        }
        
        return cleanedData;
    }

    @Override
    public void saveCleanedData(CleanedData cleanedData, String targetPath) {
        logger.info("数据已保存到路径: {}", cleanedData.getStorageLocation());
    }
    
    private String generateDataId() {
        return "COMMON_" + System.currentTimeMillis();
    }
    
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
} 