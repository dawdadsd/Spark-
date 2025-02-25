package sparkanalysis.config;

import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SparkConfig {
    private static final Logger logger = LoggerFactory.getLogger(SparkConfig.class);
    
    @Value("${spark.app-name:SparkDataAnalysis}")
    private String appName;
    
    @Value("${spark.master:local[*]}")
    private String master;
    
    @Value("${spark.local.dir:#{T(System).getProperty('java.io.tmpdir')}}")
    private String sparkLocalDir;

    @PostConstruct
    public void init() {
        try {
            // 添加必要的JVM参数
            System.setProperty("java.security.manager", "allow");
            System.setProperty("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED");
            System.setProperty("--add-opens", "java.base/java.lang=ALL-UNNAMED");
            System.setProperty("--add-opens", "java.base/java.util=ALL-UNNAMED");
            System.setProperty("--add-opens", "java.base/java.nio=ALL-UNNAMED");
            System.setProperty("--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED");

            // 禁用警告
            System.setProperty("spark.ui.showConsoleProgress", "false");
            
            logger.info("Spark配置初始化完成");
        } catch (Exception e) {
            logger.error("初始化Spark配置时发生错误", e);
            throw new RuntimeException("Spark配置初始化失败", e);
        }
    }

    @Bean
    public SparkSession sparkSession() {
        return SparkSession.builder()
                .appName(appName)
                .master(master)
                .config("spark.sql.warehouse.dir", "spark-warehouse")
                .config("spark.driver.memory", "4g")
                .config("spark.executor.memory", "4g")
                .config("spark.driver.host", "localhost")
                .config("spark.driver.bindAddress", "localhost")
                .config("spark.driver.port", "10000")
                .config("spark.port.maxRetries", "100")
                .config("spark.driver.maxResultSize", "4g")
                .config("spark.ui.enabled", "false")
                .config("spark.sql.shuffle.partitions", "10")
                .config("spark.memory.offHeap.enabled", "true")
                .config("spark.memory.offHeap.size", "4g")
                .config("spark.metrics.staticSources.enabled", "false")
                .config("spark.metrics.appStatusSource.enabled", "false")
                .config("spark.metrics.executorMetricsSource.enabled", "false")
                .config("spark.metrics.enabled", "false")
                .getOrCreate();
    }
}