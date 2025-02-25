package sparkanalysis.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogConfig {
    
    @PostConstruct
    public void init() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO");
        // 禁用 Spark 的日志重定向
        System.setProperty("spark.driver.extraJavaOptions", "-Dlogback.configurationFile=logback-spring.xml");
    }
}