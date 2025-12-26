package ankol.mod.merger.core;

import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.tools.FileTree;

import java.io.IOException;

/**
 * 文件合并器通用接口
 * <p>
 * 定义了所有文件类型合并器必须实现的方法。
 * 每个实现类负责处理一种特定的文件类型（如.scr, .xml等）。
 */
public abstract class FileMerger {
    /**
     * 当前执行合并逻辑的上下文信息
     */
    protected MergerContext context;

    public FileMerger(MergerContext context) {
        this.context = context;
    }

    /**
     * 合并两个文件。
     *
     * @param file1 第一个文件（来自Mod1）的路径。
     * @param file2 第二个文件（来自Mod2）的路径。
     * @return 一个包含合并后内容和冲突信息的 {@link MergeResult} 对象。
     */
    public abstract MergeResult merge(FileTree file1, FileTree file2) throws RuntimeException;

}