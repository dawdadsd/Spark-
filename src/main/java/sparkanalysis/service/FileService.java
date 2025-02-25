package sparkanalysis.service;

import org.springframework.stereotype.Service;
import sparkanalysis.model.FileInfo;
import java.util.List;
import java.util.Map;

/**
 * 文件服务接口
 * 定义了文件处理的核心业务功能，包括文件列表获取、数据预览和文件处理
 * 由FileServiceImpl实现具体的业务逻辑
 */
@Service("fileServiceImpl")
public interface FileService {

    /**
     * 获取文件列表
     * 返回指定案例ID下的所有文件信息
     *
     * @param caseId 案例ID，用于定位具体的文件目录
     * @return 文件信息列表，包含文件名、大小、修改时间等信息
     * @throws RuntimeException 当目录访问失败或其他IO错误时抛出
     */
    List<FileInfo> getFilesList(String caseId);

    /**
     * 预览文件数据
     * 读取指定文件De前几行数据，用于数据预览
     *
     * @param caseId 案例ID
     * @param filename 要预览的文件名
     * @return 预览数据列表，每个元素为一行数据的Map表示
     * @throws RuntimeException 当文件读取失败时抛出
     */
    List<Map<String, Object>> previewData(String caseId, String filename);

    /**
     * 处理文件
     * 对指定文件进行处理，包括数据清洗、转换等操作
     *
     * @param caseId 案例ID
     * @param filename 要处理的文件名
     * @throws RuntimeException 当文件处理过程中出错时抛出
     */
    void processFile(String caseId, String filename);
}