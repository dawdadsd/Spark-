package sparkanalysis.infrastructure.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FileUtils {
    
    /**
     * 读取文件内容
     * @param filePath 文件路径
     * @param encoding 编码方式
     * @return 文件内容
     * @throws IOException IO异常
     */
    public static String readFile(String filePath, String encoding) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), Charset.forName(encoding))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    /**
     * 写入文件内容
     * @param filePath 文件路径
     * @param content 文件内容
     * @throws IOException IO异常
     */
    public static void writeFile(String filePath, String content) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            writer.write(content);
        }
    }
    
    /**
     * 检查文件是否存在
     * @param filePath 文件路径
     * @return 是否存在
     */
    public static boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * 创建目录
     * @param dirPath 目录路径
     * @throws IOException IO异常
     */
    public static void createDirectory(String dirPath) throws IOException {
        Files.createDirectories(Paths.get(dirPath));
    }
} 