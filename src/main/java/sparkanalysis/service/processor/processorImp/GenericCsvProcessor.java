package sparkanalysis.service.processor.processorImp;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sparkanalysis.service.processor.FileProcessor;

@Component
public class GenericCsvProcessor implements FileProcessor {
    @Autowired
    private SparkSession spark;
    
    @Override
    public boolean supports(String filename) {
        return filename.toLowerCase().endsWith(".csv");
    }
    
    @Override
    public Dataset<Row> process(String filePath) {
        Dataset<Row> rawData = spark.read()
                .option("header", true)
                .option("encoding","GBK")
                .option("inferSchema", true)
                .csv(filePath);
                
        // 基础数据清理
        return rawData.na().drop()
                     .dropDuplicates();
    }

    @Override
    public void save(Dataset<Row> data, String savePath) {
        data.write()
                .mode("overwrite")
                .parquet(savePath);
    }
}
