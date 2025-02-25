package sparkanalysis.service.processor.processorImp;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sparkanalysis.service.analysis.AnalysisService;
import sparkanalysis.service.lottery.LotteryDataCleaner;
import sparkanalysis.service.processor.FileProcessor;
import sparkanalysis.schema.JingcaiSchema;
import org.slf4j.Logger;
import org.apache.spark.sql.functions;

import static org.apache.spark.sql.functions.*;

@Component
public class JingcaiDataProcessor implements FileProcessor {
     private static final Logger logger = LoggerFactory.getLogger(JingcaiDataProcessor.class);
    @Autowired
    private SparkSession spark;
    
    @Autowired
    private AnalysisService analysisService;
    
    @Autowired
    private LotteryDataCleaner lotteryDataCleaner;

    @Override
    public boolean supports(String filename) {
        return filename.contains("竞彩");
    }

    @Override
    public Dataset<Row> process(String filePath) {
        // 1. 读取数据
        Dataset<Row> rawData = spark.read()
                .option("header", true)
                .option("encoding", "GBK")
                .option("delimiter", ",")
                .option("nullValue", "")  // 添加空值处理
                .option("nanValue", "0")  // 添加NaN值处理
                .csv(filePath);

        logger.info("读取数据完成，数据量：{}", rawData.count());
        // 2. 数据清洗
        Dataset<Row> cleanedData = rawData
        .select(
            trim(col("票号")).cast("string").as("ticket_number"),
            trim(col("省中心名称")).cast("string").as("province"),
            trim(col("市中心名称")).cast("string").as("city"),
            trim(col("门店编号")).cast("string").as("store_id"),
            trim(col("销售终端编号")).cast("string").as("terminal_id"),
            to_date(col("销售日期"), "yyyy/MM/dd").as("sale_date"),
            to_timestamp(col("销售时间"), "yyyy/MM/dd HH:mm").as("sale_time"),
            trim(col("体育项目")).cast("string").as("sport_type"),
            trim(col("过关方式")).cast("string").as("pass_type"),
            trim(col("游戏")).cast("string").as("game_type"),
            col("票面金额").cast("double").as("amount"),
            trim(col("投注内容")).cast("string").as("bet_content"),
            col("倍数").cast("integer").as("multiple")
        );
        logger.info("数据类型转换后行数: {}", cleanedData.count());

        // 3. 处理空值和异常值
        cleanedData = cleanedData
            .na().fill("未知", new String[]{"province", "city", "sport_type", "game_type"})
            .na().fill(0.0, new String[]{"amount"})
            .na().fill(1, new String[]{"multiple"})
            .filter(
                col("ticket_number").isNotNull()
                .and(col("amount").gt(0))
                .and(col("multiple").gt(0))
            );
         // 4. 标准化体育项目
         cleanedData = cleanedData
         .withColumn("sport_type", 
             when(col("sport_type").equalTo("足球"), "FOOTBALL")
             .when(col("sport_type").equalTo("篮球"), "BASKETBALL")
             .otherwise(col("sport_type")));

     logger.info("清洗后最终数据行数: {}", cleanedData.count());
     
     // 5. 数据验证
     if (cleanedData.isEmpty()) {
         throw new RuntimeException("清洗后数据为空，请检查数据清洗规则");
     }      
            return cleanedData;
    }

    @Override
    public void save(Dataset<Row> data, String savePath) {
        try{data.repartition(10)
        .write()
        .mode("overwrite")
       .partitionBy("province","sport_type")
        .parquet(savePath);
        logger.info("数据保存完成，保存路径：{}", savePath);
    }catch(Exception e){
        logger.error("数据保存失败，错误信息：{}", e.getMessage());
    }
}
}