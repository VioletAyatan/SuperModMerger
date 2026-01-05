package ankol.mod.merger.core;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 路径文件树，当文件是在某文件路径时的表示
 *
 * @author Ankol
 */
@Data
public class PathFileTree extends AbstractFileTree {
    /**
     * 解压出来后的文件路径
     */
    private Path fullPathName;

    public PathFileTree(String fileName, String fileEntryName, String archiveFileName) {
        super(fileName, fileEntryName, archiveFileName);
    }

    public PathFileTree(String fileName, String fileEntryName, String archiveFileName, Path fullPathName) {
        super(fileName, fileEntryName, archiveFileName);
        this.fullPathName = fullPathName;
    }

    @Override
    public String getContent() {
        try {
            return Files.readString(fullPathName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
