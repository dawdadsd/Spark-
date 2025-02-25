package sparkanalysis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spark")
public class SparkProperties {
    private String appName;
    private String master;

    public String getDriverMemory() {
        return driverMemory;
    }

    public void setDriverMemory(String driverMemory) {
        this.driverMemory = driverMemory;
    }

    private String driverMemory;

    public String getExecutorMemory() {
        return executorMemory;
    }

    public void setExecutorMemory(String executorMemory) {
        this.executorMemory = executorMemory;
    }

    private String executorMemory;
    private String localDir;

    public String getLocalDir() {
        return localDir;
    }

    public void setLocalDir(String localDir) {
        this.localDir = localDir;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

}