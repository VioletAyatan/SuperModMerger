package ankol.mod.merger.merger;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 文件合并器通用接口
 * <p>
 * 定义了所有文件类型合并器必须实现的方法。
 * 每个实现类负责处理一种特定的文件类型（如.scr, .xml等）。
 */
public interface IFileMerger {

    /**
     * 合并两个文件。
     *
     * @param file1 第一个文件（来自Mod1）的路径。
     * @param file2 第二个文件（来自Mod2）的路径。
     * @return 一个包含合并后内容和冲突信息的 {@link MergeResult} 对象。
     * @throws IOException 如果在文件读取或处理过程中发生IO错误。
     */
    MergeResult merge(Path file1, Path file2) throws IOException;

}