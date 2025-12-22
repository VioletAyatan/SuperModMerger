package ankol.mod.merger.tools;

import lombok.Data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件来源信息 - 记录文件的完整来源链
 * <p>
 * 用于追踪文件在嵌套压缩包中的来源，例如：
 * - 直接来自 data3.pak
 * - 来自 mymod.zip -> data3.pak
 * - 来自 container.zip -> inner.pak -> actual_file.scr
 *
 * @author Ankol
 */
@Data
public class FileSourceInfo {

    /**
     * 文件实际路径（解压后的物理路径）
     */
    private final Path filePath;
    /**
     * 文件在pak包中的相对路径
     */
    private final String fileEnterName;
    /**
     * 来源链：记录文件从外到内的所有容器
     * 例如：["mymod.zip", "data3.pak"] 表示文件来自 mymod.zip 内的 data3.pak
     */
    private final List<String> sourceChain = new ArrayList<>();

    public FileSourceInfo(Path filePath, String fileEnterName) {
        this.filePath = filePath;
        this.fileEnterName = fileEnterName;
    }

    /**
     * 添加一个来源容器
     *
     * @param source 容器名称（如 "data3.pak" 或 "mymod.zip"）
     */
    public void addSource(String source) {
        if (!sourceChain.contains(source)) {
            sourceChain.add(source);
        }
    }

    /**
     * 获取完整来源链字符串表示
     * 例如："mymod.zip -> data3.pak" 或 "data3.pak"
     */
    public String getSourceChainString() {
        if (sourceChain.isEmpty()) {
            return "unknown";
        }
        return String.join(" -> ", sourceChain);
    }

    /**
     * 获取主要来源（最外层的容器）
     * 例如：如果来源链是 ["mymod.zip", "data3.pak"]，返回 "mymod.zip"
     */
    public String getPrimarySource() {
        if (sourceChain.isEmpty()) {
            return "unknown";
        }
        return sourceChain.getFirst();
    }

    /**
     * 获取直接来源（最内层的容器）
     * 例如：如果来源链是 ["mymod.zip", "data3.pak"]，返回 "data3.pak"
     */
    public String getDirectSource() {
        if (sourceChain.isEmpty()) {
            return "unknown";
        }
        return sourceChain.getLast();
    }

    /**
     * 检查是否来自嵌套压缩包
     *
     * @return 如果来源链长度 > 1，说明是嵌套的
     */
    public boolean isFromNestedArchive() {
        return sourceChain.size() > 1;
    }
}

