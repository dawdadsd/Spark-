package sparkanalysis.service.processor.factory;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sparkanalysis.service.processor.FileProcessor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件处理器工厂类
 * 负责管理和分发不同类型文件的处理器
 * 使用Spring的依赖注入自动装配所有FileProcessor实现
 */
@Component
public class FileProcessorFactory {

    /**
     * 存储所有注册的文件处理器
     * 通过Spring自动注入所有FileProcessor的实现类
     */
    private final List<FileProcessor> processors;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FileProcessorFactory.class);

    /**
     * 构造函数
     * 通过依赖注入接收所有FileProcessor实现类
     * 并在初始化时记录已注册的处理器信息
     *
     * @param processors 所有FileProcessor实现类的列表
     */
    @Autowired
    public FileProcessorFactory(List<FileProcessor> processors) {
        this.processors = processors;
        // 记录所有已注册的处理器名称
        logger.info("已注册的处理器:" +
                processors.stream()
                        .map(p -> p.getClass().getSimpleName())
                        .collect(Collectors.joining(",")));
    }

    /**
     * 获取适合处理指定文件的处理器
     * 通过遍历所有处理器，找到第一个支持处理该文件的处理器
     *
     * @param filename 需要处理的文件名
     * @return 匹配的文件处理器
     * @throws IllegalArgumentException 当没有找到匹配的处理器时抛出
     */
    public FileProcessor getProcessor(String filename) {
        return processors.stream()
                .filter(p -> {
                    // 检查每个处理器是否支持处理该文件
                    boolean supported = p.supports(filename);
                    // 记录处理器匹配结果
                    logger.debug("处理器 {} {} 处理文件 {}",
                            p.getClass().getSimpleName(),
                            supported ? "支持" : "不支持",
                            filename);
                    return supported;
                })
                .findFirst()
                .orElseThrow(() -> {
                    // 当没有找到匹配的处理器时，记录错误并抛出异常
                    logger.error("没有找到支持处理文件 {} 的处理器", filename);
                    return new IllegalArgumentException("没有匹配的处理器: " + filename);
                });
    }
}