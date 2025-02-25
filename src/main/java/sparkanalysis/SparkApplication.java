package sparkanalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "sparkanalysis.interfaces.rest.controller.analysis",
    "sparkanalysis.interfaces.rest.controller.clean",
    "sparkanalysis.service.analysis",
    "sparkanalysis.service.common",
    "sparkanalysis.service.lottery",
    "sparkanalysis.service.impl",
    "sparkanalysis.service.common.impl",
    "sparkanalysis.service.lottery.impl",
    "sparkanalysis.config"
})
public class SparkApplication {
    public static void main(String[] args) {
        SpringApplication.run(SparkApplication.class, args);
    }
}
