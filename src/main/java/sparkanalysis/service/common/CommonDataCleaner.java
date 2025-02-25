package sparkanalysis.service.common;
import sparkanalysis.domain.common.model.CommonDataSource;
import sparkanalysis.model.CleanedData;
public interface CommonDataCleaner {
    /**
     * 清洗通用数据
     * @param source 数据源
     * @return 清洗后的数据
     */
    CleanedData cleanData(CommonDataSource source);

    /**
     * 保存清洗后的数据
     * @param cleanedData 清洗后的数据
     * @param targetPath 目标路径
     */
    void saveCleanedData(CleanedData cleanedData, String targetPath);
} 