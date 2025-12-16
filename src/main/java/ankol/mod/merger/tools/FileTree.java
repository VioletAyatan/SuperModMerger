package ankol.mod.merger.tools;

import lombok.Data;

@Data
public class FileTree {
    /**
     * 文件名（不带路径）
     */
    private String fileName;
    /**
     * 带文件路径的文件名
     */
    private String fullPathName;

    public FileTree(String fileName, String fullPathName) {
        this.fileName = fileName;
        this.fullPathName = fullPathName;
    }
}
