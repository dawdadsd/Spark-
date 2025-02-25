package sparkanalysis.service.lottery;

import sparkanalysis.domain.lottery.model.LotteryDataSource;
import sparkanalysis.model.CleanedData;

public interface LotteryDataCleaner {
    /**
     * 清洗竞彩数据
     * @param source 竞彩数据源
     * @return 清洗后的数据
     */
    CleanedData cleanData(LotteryDataSource source);

    /**
     * 保存清洗后的竞彩数据
     * @param cleanedData 清洗后的数据
     * @param targetPath 目标路径
     */
    void saveCleanedData(CleanedData cleanedData, String targetPath);
} 