package sparkanalysis.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件存储服务
 * 负责处理文件的存储和验证
 * 配合FileController使用，处理文件上传功能
 */
@Component
public class FileStorageService {
    public static final String BASE_DIR = "data";
    
    @Value("${file.upload.max-size:10485760}") // 默认10MB
    private long maxFileSize;

    @Value("${file.upload.allowed-types:csv,xlsx,xls,parquet}")
    private String allowedFileTypes;

    private final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "text/csv",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/octet-stream"
    );

    /**
     * 存储上传的文件
     * 将文件保存到指定案例ID对应的目录中
     *
     * @param caseID 案例ID，用于创建独立的存储目录
     * @param file 上传的文件对象
     * @return 保存后的文件名
     * @throws Exception 当文件存储过程中发生错误时抛出
     */
    public String store(String caseID, MultipartFile file) throws Exception {
        Path caseDir = Paths.get(BASE_DIR, caseID)
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(caseDir);
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + extension;
        
        Path targetPath = caseDir.resolve(newFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        return newFilename;
    }

    /**
     * 验证上传的文件
     * 检查文件大小、类型和内容
     *
     * @param file 要验证的文件对象
     * @throws IllegalArgumentException 当文件验证失败时抛出
     */
    public void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                String.format("文件大小不能超过 %d MB", maxFileSize / (1024 * 1024)));
        }

        // 检查文件类型
        String extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
        if (!Arrays.asList(allowedFileTypes.split(",")).contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        // 检查MIME类型
        String contentType = file.getContentType();
        if (!ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("不支持的文件内容类型: " + contentType);
        }

        // 验证文件名安全性
        validateFileName(file.getOriginalFilename());
    }

    /**
     * 验证文件名安全性
     * 
     * @param fileName 文件名
     * @throws IllegalArgumentException 当文件名不安全时抛出
     */
    private void validateFileName(String fileName) {
        if (fileName == null || fileName.contains("..")) {
            throw new IllegalArgumentException("文件名不安全");
        }
        
        // 检查文件名是否包含特殊字符
        if (!fileName.matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException("文件名只能包含字母、数字、点、下划线和横线");
        }
    }
}