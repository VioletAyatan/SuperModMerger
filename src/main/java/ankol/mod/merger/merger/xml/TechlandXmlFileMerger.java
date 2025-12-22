package ankol.mod.merger.merger.xml;

import ankol.mod.merger.core.FileMerger;
import ankol.mod.merger.core.MergerContext;
import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.tools.FileTree;

public class TechlandXmlFileMerger extends FileMerger {
    public TechlandXmlFileMerger(MergerContext context) {
        super(context);
    }

    @Override
    public MergeResult merge(FileTree file1, FileTree file2) {
        throw new UnsupportedOperationException("XML file merging is not yet implemented.");
    }
}