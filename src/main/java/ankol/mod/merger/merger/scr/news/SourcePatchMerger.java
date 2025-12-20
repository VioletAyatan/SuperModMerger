package ankol.mod.merger.merger.scr.news;

import ankol.mod.merger.merger.scr.news.node.ConflictRecord;
import ankol.mod.merger.merger.scr.news.node.ConflitMark;
import ankol.mod.merger.merger.scr.news.node.ScrContainerNode;
import ankol.mod.merger.merger.scr.news.node.ScrNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SourcePatchMerger {
    /**
     * 标记冲突项的容器
     */
    private final List<ConflictRecord> conflits = new ArrayList<>();
    private final List<ConflitMark> finalEdits = new ArrayList<>();
    /**
     * 当前处理的文件名
     */
    private final String fileName;

    public SourcePatchMerger(String fileName) {
        this.fileName = fileName;
    }

    // 入口
    public String merge(String baseSource, ScrContainerNode baseRoot, ScrContainerNode modRoot) {
        conflits.clear();
        // 递归对比，找到冲突项
        reduceCompare(baseRoot, modRoot);
        if (!conflits.isEmpty()) {
            resolveConflictsInteractively();
        }
        // 排序并应用修改
        StringBuilder sb = new StringBuilder(baseSource);
        if (!conflits.isEmpty()) {
            System.out.println("==================== Conflict Detected ====================");
        }
        return sb.toString();
    }

    private void reduceCompare(ScrContainerNode base, ScrContainerNode mod) {
        // 遍历 Mod 的所有子节点
        for (Map.Entry<String, ScrNode> entry : mod.getChildren().entrySet()) {
            String signature = entry.getKey();
            ScrNode baseNode = base.getChildren().get(signature);
            ScrNode modNode = entry.getValue();

            if (baseNode == null) {
                // [新增] Base 没有这个节点 -> 插入
                handleInsertion(base, modNode);
            } else {
                // [存在] 检查是否冲突
                if (baseNode instanceof ScrContainerNode && modNode instanceof ScrContainerNode) {
                    // 容器节点，递归进入内部对比
                    reduceCompare((ScrContainerNode) baseNode, (ScrContainerNode) modNode);
                } else {
                    // 叶子节点，对比内容。
                    String baseText = baseNode.getSourceText().trim();
                    String modText = modNode.getSourceText().trim();
                    //内容不一致，标记发生冲突
                    if (!baseText.equals(modText)) {
                        conflits.add(new ConflictRecord(fileName, signature, baseNode, modNode));
                    }
                }
            }
        }
    }

    /**
     * 提示用户解决冲突项
     */
    private void resolveConflictsInteractively() {
        Scanner scanner = new Scanner(System.in);
        for (ConflictRecord conflictRecord : conflits) {
        }
    }

    private void handleInsertion(ScrContainerNode baseContainer, ScrNode modNode) {
        // 插入位置：Base 容器的 '}' 之前
        // 注意：baseContainer.getStopIndex() 指向 '}' 字符的位置
        int insertPos = baseContainer.getStopIndex();

        // 构造插入文本：加换行和缩进 (简单模拟，假设是4空格)
        // 如果想做得更完美，可以计算 baseContainer 的缩进层级
        String newContent = "\n    " + modNode.getSourceText();

        finalEdits.add(new ConflitMark(insertPos, insertPos, newContent));
    }
}
