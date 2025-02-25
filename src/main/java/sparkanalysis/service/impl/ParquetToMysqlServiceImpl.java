package sparkanalysis.service.impl;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sparkanalysis.service.ParquetToMysqlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Service
public class ParquetToMysqlServiceImpl implements ParquetToMysqlService {
    
    private static final Logger logger = LoggerFactory.getLogger(ParquetToMysqlServiceImpl.class);
    
    @Autowired
    private SparkSession sparkSession;
    
    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    @Override
    public void importToMysql(String parquetPath, String tableName, String saveMode) {
        try {
            // 1. 验证Parquet文件是否存在
            Path path = Paths.get(parquetPath);
            if (!Files.exists(path)) {
                throw new RuntimeException("Parquet文件路径不存在: " + parquetPath);
            }
            
            // 2. 验证是否为Parquet文件
            if (!Files.exists(Paths.get(parquetPath, "_SUCCESS")) && 
                !Files.list(path).anyMatch(p -> p.toString().endsWith(".parquet"))) {
                throw new RuntimeException("指定路径下没有有效的Parquet文件");
            }
            
            // 3. 读取Parquet文件
            Dataset<Row> parquetDF = sparkSession.read().parquet(parquetPath);
            
            if (parquetDF.isEmpty()) {
                throw new RuntimeException("Parquet文件中没有数据");
            }
            
            logger.info("成功读取Parquet文件，数据行数: {}", parquetDF.count());
            
            // 4. 设置MySQL连接属性
            Properties connectionProperties = new Properties();
            connectionProperties.put("user", username);
            connectionProperties.put("password", password);
            connectionProperties.put("driver", "com.mysql.cj.jdbc.Driver");
            
            // 5. 写入MySQL
            parquetDF.write()
                .mode(saveMode)
                .jdbc(jdbcUrl, tableName, connectionProperties);
            
            logger.info("数据已成功导入到MySQL表: {}", tableName);
            
        } catch (Exception e) {
            logger.error("导入MySQL失败", e);
            throw new RuntimeException("Parquet数据导入MySQL失败: " + e.getMessage());
        }
    }
} 