package ankol.mod.merger.merger;

import java.io.IOException;
import java.nio.file.Path;

public class XmlFileMerger implements IFileMerger {
    @Override
    public MergeResult merge(Path file1, Path file2) throws IOException {
        throw new UnsupportedOperationException("XML file merging is not yet implemented.");
    }
}