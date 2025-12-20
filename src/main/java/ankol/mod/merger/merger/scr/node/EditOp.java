package ankol.mod.merger.merger.scr.node;

import lombok.Data;

/**
 * 冲突标记类
 *
 * @author Ankol
 */
@Data
public class EditOp implements Comparable<EditOp> {
    /**
     * 原始文本
     */
    private String originalText;
    /**
     * 冲突文本
     */
    private String conflitText;
    private int start;
    private int end;     // start == end 为插入，start < end 为替换
    private String text; // 要写入的新文本

    public EditOp(String originalText, String conflitText, int start, int end, String text) {
        this.originalText = originalText;
        this.conflitText = conflitText;
        this.start = start;
        this.end = end;
        this.text = text;
    }

    public EditOp(int start, int end, String text) {
        this.start = start;
        this.end = end;
        this.text = text;
    }

    // 倒序排序：优先修改文件末尾，防止坐标偏移
    @Override
    public int compareTo(EditOp other) {
        return Integer.compare(other.start, this.start);
    }
}
