package ankol.mod.merger.tools;

import lombok.Data;

import java.nio.file.Path;

@Data
public class FileTree {
    /**
     * 文件名（不带路径）
     */
    private String fileName;
    /**
     * 文件名，在压缩包中的相对路径
     */
    private String fileEntryName;
    /**
     * 文件来自哪个mod包（如果是压缩包嵌套的话，使用 mod.zip -> mod.pak 这样的名字显示）
     */
    private String archiveFileName;
    /**
     * 解压出来后的文件路径
     */
    private Path fullPathName;

    public FileTree(String fileName, String fileEntryName, String archiveFileName) {
        this.fileName = fileName;
        this.fileEntryName = fileEntryName;
        this.archiveFileName = archiveFileName;
    }

    public FileTree(String fileName, String fileEntryName, String archiveFileName, Path fullPathName) {
        this.fileName = fileName;
        this.fileEntryName = fileEntryName;
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
