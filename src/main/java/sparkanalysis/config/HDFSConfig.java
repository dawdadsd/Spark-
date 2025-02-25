package sparkanalysis.config;

import org.apache.hadoop.conf.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@org.springframework.context.annotation.Configuration
public class HDFSConfig {
    
    @Value("${hadoop.namenode.url:hdfs://192.168.2.243:30070}")
    private String namenodeUrl;
    
    @Bean
    public Configuration hadoopConfiguration() {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", namenodeUrl);
        conf.set("dfs.namenode.rpc-address", "192.168.2.243:30070");
        conf.set("dfs.namenode.http-address", "192.168.2.243:30071");
        conf.set("yarn.resourcemanager.hostname", "192.168.2.243");
        conf.set("yarn.resourcemanager.address", "192.168.2.243:30888");
        conf.set("yarn.resourcemanager.webapp.address", "192.168.2.243:30889");
        conf.set("ipc.maximum.data.length", "268435456");
        return conf;
    }
}
