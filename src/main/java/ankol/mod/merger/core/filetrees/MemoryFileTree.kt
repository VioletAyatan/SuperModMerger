package ankol.mod.merger.core.filetrees;

/**
 * 内存文件树，用于存储内存中的文件内容
 *
 * @author Ankol
 */
public class MemoryFileTree extends AbstractFileTree {
    private final String contentStr;

    public MemoryFileTree(String fileName, String fileEntryName, String archiveFileName, String contentStr) {
        super(fileName, fileEntryName, archiveFileName);
        this.contentStr = contentStr;
    }

    @Override
    public String getContent() {
        return contentStr;
    }
}
