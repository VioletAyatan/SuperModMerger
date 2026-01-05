package ankol.mod.merger.core;

public abstract class AbstractFileTree {
    /**
     * 文件名（不带路径）
     */
    protected String fileName;
    /**
     * 文件名，在压缩包中的相对路径
     */
    protected String fileEntryName;
    /**
     * 文件来自哪个mod包（如果是压缩包嵌套的话，使用 mod.zip -> mod.pak 这样的名字显示）
     */
    protected String archiveFileName;

    protected AbstractFileTree(String fileName, String fileEntryName, String archiveFileName) {
        this.fileName = fileName;
        this.fileEntryName = fileEntryName;
        this.archiveFileName = archiveFileName;
    }

    public abstract String getContent();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileEntryName() {
        return fileEntryName;
    }

    public void setFileEntryName(String fileEntryName) {
        this.fileEntryName = fileEntryName;
    }

    public String getArchiveFileName() {
        return archiveFileName;
    }

    public void setArchiveFileName(String archiveFileName) {
        this.archiveFileName = archiveFileName;
    }
}
