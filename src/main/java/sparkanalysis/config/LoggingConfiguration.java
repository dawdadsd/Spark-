package sparkanalysis.config;

import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class LoggingConfiguration {
    
    @PostConstruct
    public void init() {
        // 禁用 Spark 的日志重定向
        System.setProperty("spark.driver.allowMultipleContexts", "true");
        System.setProperty("spark.master", "local[*]");
    }
}
