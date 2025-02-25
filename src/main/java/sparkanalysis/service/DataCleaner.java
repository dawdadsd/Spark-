package sparkanalysis.service;

import sparkanalysis.model.DataSource;
import sparkanalysis.model.CleanedData;

public interface DataCleaner {
    /**
     * 清洗数据
     * @param source 数据源
     * @return 清洗后的数据
     */
    CleanedData cleanData(DataSource source);

    /**
     * 保存清洗后的数据
     * @param cleanedData 清洗后的数据
     */
    void saveCleanedData(CleanedData cleanedData);
} 