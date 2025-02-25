package sparkanalysis.service.analysis.impl;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sparkanalysis.domain.analysis.model.AnalysisRequest;
import sparkanalysis.service.analysis.AnalysisService;
import static org.apache.spark.sql.functions.*;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Autowired
    private SparkSession sparkSession;

    @Override
    public Dataset<Row> importData(String filePath) {
        return sparkSession.read()
            .option("header", "true")
            .option("inferSchema", "true")
            .csv(filePath);
    }

    @Override
    public Dataset<Row> analyzeData(Dataset<Row> dataset, AnalysisRequest request) {
        if (dataset.isEmpty()) {
            throw new IllegalArgumentException("数据集为空");
        }

        dataset.createOrReplaceTempView("raw_data");

        switch (request.getAnalysisType().toLowerCase()) {
            case "threshold":
                return analyzeThreshold(dataset, request);
            case "time_series":
                return analyzeTimeSeries(dataset, request);
            case "group":
                return analyzeGroup(dataset, request);
            default:
                throw new IllegalArgumentException("不支持的分析类型: " + request.getAnalysisType());
        }
    }

    @Override
    public void exportResult(Dataset<Row> result, String outputPath) {
        result.coalesce(1)
            .write()
            .mode("overwrite")
            .option("header", "true")
            .csv(outputPath);
    }

    @Override
    public void saveToHadoop(Dataset<Row> dataset, String hadoopPath) {
        dataset.write()
            .mode("overwrite")
            .option("header", "true")
            .parquet(hadoopPath);
    }

    private Dataset<Row> analyzeThreshold(Dataset<Row> dataset, AnalysisRequest request) {
        return dataset.filter(col("amount").gt(request.getThreshold()));
    }

    private Dataset<Row> analyzeTimeSeries(Dataset<Row> dataset, AnalysisRequest request) {
        String[] timeRange = request.getTimeRange().split(",");
        String startTime = timeRange[0].trim();
        String endTime = timeRange[1].trim();
        
        return dataset.filter(
            col("time").between(lit(startTime), lit(endTime))
        );
    }

    private Dataset<Row> analyzeGroup(Dataset<Row> dataset, AnalysisRequest request) {
        return dataset.groupBy(col(request.getGroupBy()))
            .agg(
                count("*").as("count"),
                sum("amount").as("total_amount"),
                avg("amount").as("avg_amount")
            );
    }
} 