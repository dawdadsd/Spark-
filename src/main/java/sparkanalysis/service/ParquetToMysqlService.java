package sparkanalysis.service;

public interface ParquetToMysqlService {
    /**
     * 将Parquet文件数据导入到MySQL
     * @param parquetPath Parquet文件路径
     * @param tableName MySQL表名
     * @param saveMode 保存模式 (overwrite/append)
     * @throws RuntimeException 当处理失败时抛出
     */
    void importToMysql(String parquetPath, String tableName, String saveMode);
} 