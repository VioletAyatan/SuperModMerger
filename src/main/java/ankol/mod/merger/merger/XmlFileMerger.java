package ankol.mod.merger.merger;

import ankol.mod.merger.tools.FileTree;

import java.io.IOException;

public class XmlFileMerger implements IFileMerger {
    @Override
    public MergeResult merge(FileTree file1, FileTree file2) throws IOException {
        throw new UnsupportedOperationException("XML file merging is not yet implemented.");
    }
}