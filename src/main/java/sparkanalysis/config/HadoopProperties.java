package sparkanalysis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hadoop")
public class HadoopProperties {
    private String homeDir;
    private String tmpDir;
    private String userName;

    public String getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(String homeDir) {
        this.homeDir = homeDir;
    }

    public String getTmpDir() {
        return tmpDir;
    }

    public void setTmpDir(String tmpDir) {
        this.tmpDir = tmpDir;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}