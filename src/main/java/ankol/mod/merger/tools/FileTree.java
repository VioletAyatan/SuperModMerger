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
    /**
     * 文件来自哪个mod包（如果是压缩包嵌套的话，使用 mod.zip -> mod.pak 这样的名字显示）
     */
    private String archiveFileName;

    public FileTree(String fileName, String fullPathName) {
        this.fileName = fileName;
        this.fullPathName = fullPathName;
    }

    public FileTree(String fileName, String fullPathName, String archiveFileName) {
        this.fileName = fileName;
        this.fullPathName = fullPathName;
        this.archiveFileName = archiveFileName;
    }

    /**
     * 拼接ArchiveFileName
     *
     * @param archiveFileName 压缩包名称
     */
    public void appendArchiveFileName(String archiveFileName) {
        if (this.archiveFileName == null || this.archiveFileName.isEmpty()) {
            this.archiveFileName = archiveFileName;
        } else {
            this.archiveFileName = this.archiveFileName + " -> " + archiveFileName;
        }
    }
}
