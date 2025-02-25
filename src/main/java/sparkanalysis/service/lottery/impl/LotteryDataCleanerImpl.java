package sparkanalysis.service.lottery.impl;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sparkanalysis.domain.lottery.model.LotteryDataSource;
import sparkanalysis.service.lottery.LotteryDataCleaner;
import sparkanalysis.model.CleanedData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.apache.spark.sql.functions.*;
@Service
public class LotteryDataCleanerImpl implements LotteryDataCleaner {
    private static final Logger logger = LoggerFactory.getLogger(LotteryDataCleanerImpl.class);

    @Autowired
    private SparkSession sparkSession;

    @Override
    public CleanedData cleanData(LotteryDataSource source) {
        CleanedData cleanedData = new CleanedData();

        try {
            logger.info("开始竞彩数据清洗");
            // 1. 读取竞彩数据
            Dataset<Row> rawData = sparkSession.read()
                    .option("header", true)
                    .option("encoding", "GBK")
                    .option("delimiter", ",")
                    .option("mode", "PERMISSIVE")
                    .option("inferSchema", true)
                    .csv(source.getSourcePath());

            logger.info("原始数据行数: {}", rawData.count());

            // 2. 基础清洗 - 只处理必要的字段
            Dataset<Row> cleanedDF = rawData
                    .select(
                            trim(col("票号")).cast("string").as("ticket_number"),
                            trim(col("省中心名称")).cast("string").as("province"),
                            trim(col("市中心名称")).cast("string").as("city"),
                            trim(col("门店编号")).cast("string").as("store_id"),
                            trim(col("销售终端编号")).cast("string").as("terminal_id"),
                            trim(col("体育项目")).cast("string").as("sport_type"),
                            trim(col("过关方式")).cast("string").as("pass_type"),
                            trim(col("游戏")).cast("string").as("game_type"),
                            col("票面金额").cast("double").as("amount"),
                            trim(col("投注内容")).cast("string").as("bet_content"),
                            col("倍数").cast("integer").as("multiple")
                    );

            logger.info("数据类型转换后行数: {}", cleanedDF.count());

            // 3. 处理空值和异常值
            cleanedDF = cleanedDF
                    .na().fill("未知", new String[]{"province", "city", "sport_type", "game_type"})
                    .na().fill(0.0, new String[]{"amount"})
                    .na().fill(1, new String[]{"multiple"})
                    .filter(
                            col("ticket_number").isNotNull()
                                    .and(col("amount").gt(0))
                                    .and(col("multiple").gt(0))
                    );

            // 4. 标准化体育项目
            cleanedDF = cleanedDF
                    .withColumn("sport_type",
                            when(col("sport_type").equalTo("足球"), "FOOTBALL")
                                    .when(col("sport_type").equalTo("篮球"), "BASKETBALL")
                                    .otherwise(col("sport_type")));

            logger.info("清洗后最终数据行数: {}", cleanedDF.count());

            // 5. 设置清洗结果
            String dataId = "LOTTERY_" + System.currentTimeMillis();
            cleanedData.setDataId(dataId);
            cleanedData.setCleanType("LOTTERY");
            cleanedData.setCleanTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            cleanedData.setStorageLocation("cleaned/lottery/" + dataId);

            // 6. 保存数据
            cleanedDF.repartition(10)
                    .write()
                    .mode("overwrite")
                    .parquet(cleanedData.getStorageLocation());

            logger.info("数据清洗完成，保存到: {}", cleanedData.getStorageLocation());

            return cleanedData;

        } catch (Exception e) {
            logger.error("竞彩数据处理失败", e);
            throw new RuntimeException("竞彩数据Spark清洗失败: " + e.getMessage());
        }
    }

    @Override
    public void saveCleanedData(CleanedData cleanedData, String targetPath) {
        logger.info("数据保存到: {}", cleanedData.getStorageLocation());
    }
}