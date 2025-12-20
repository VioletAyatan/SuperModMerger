package ankol.mod.merger.merger.scr.news;

import ankol.mod.merger.core.IFileMerger;
import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.merger.scr.news.node.ConflictRecord;
import ankol.mod.merger.merger.scr.news.node.EditOp;
import ankol.mod.merger.merger.scr.news.node.ScrContainerNode;
import ankol.mod.merger.merger.scr.news.node.ScrNode;
import ankol.mod.merger.tools.FileTree;
import cn.hutool.core.util.StrUtil;

import java.io.IOException;
import java.util.*;

public class SourcePatchMerger implements IFileMerger {
    /**
     * 标记冲突项的容器
     */
    private final List<ConflictRecord> conflicts = new ArrayList<>();
    private final List<EditOp> finalEdits = new ArrayList<>();

    //记录当前对比的mod，文件名等
    private final String sourceMod;
    private final String mergerMod;
    private final String fileName;

    public SourcePatchMerger(String sourceMod, String mergerMod, String fileName) {
        this.sourceMod = sourceMod;
        this.mergerMod = mergerMod;
        this.fileName = fileName;
    }

    // 入口
    @Override
    public MergeResult merge(FileTree file1, FileTree file2) throws IOException {
        return null;
    }

    public String merge(String baseSource, ScrContainerNode baseRoot, ScrContainerNode modRoot) {
        conflicts.clear();
        // 递归对比，找到冲突项
        reduceCompare(baseRoot, modRoot);
        if (!conflicts.isEmpty()) {
            resolveConflictsInteractively();
        }
        // 3. 将用户选择的 Mod 代码转化为 EditOp
        for (ConflictRecord record : conflicts) {
            //选择第1位时不用变
            if (record.getUserChoice() == 2) { // 用户选择了 Mod
                finalEdits.add(new EditOp(
                        record.getBaseNode().getStartIndex(),
                        record.getBaseNode().getStopIndex() + 1,
                        record.getModNode().getSourceText()
                ));
            }
        }
        // 4. 应用所有修改 (从后往前)
        Collections.sort(finalEdits);
        StringBuilder sb = new StringBuilder(baseSource);
        for (EditOp op : finalEdits) {
            sb.replace(op.getStart(), op.getEnd(), op.getText());
        }
        return sb.toString();
    }

    private void reduceCompare(ScrContainerNode baseContainer, ScrContainerNode modContainer) {
        // 遍历 Mod 的所有子节点
        for (Map.Entry<String, ScrNode> entry : modContainer.getChildren().entrySet()) {
            String signature = entry.getKey();
            ScrNode baseNode = baseContainer.getChildren().get(signature);
            ScrNode modNode = entry.getValue();

            if (baseNode == null) {
                // [新增] Base 没有这个节点 -> 插入
                handleInsertion(baseContainer, modNode);
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
                        conflicts.add(new ConflictRecord(fileName, sourceMod, mergerMod, signature, baseNode, modNode));
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
        System.out.println("\n======= 检测到 " + conflicts.size() + " 处代码冲突 =======");

        for (int i = 0; i < conflicts.size(); i++) {
            ConflictRecord record = conflicts.get(i);
            System.out.println("\n------------------------------------------------");
            System.out.printf("[%d/%d] 文件: %s\n", i + 1, conflicts.size(), record.getFileName());
            System.out.printf("位置签名: %s\n", record.getSignature());
            System.out.println(StrUtil.format("{}: Line:[{}] {}", record.getBaseModName(), record.getBaseNode().getLine(), record.getBaseNode().getSourceText().trim()));
            System.out.println(StrUtil.format("{}: Line:[{}] {}", record.getMergeModName(), record.getModNode().getLine(), record.getModNode().getSourceText().trim()));
            System.out.print("请选择 (1/2): ");

            while (true) {
                String input = scanner.nextLine();
                if (input.equals("1") || input.equals("2")) {
                    record.setUserChoice(Integer.parseInt(input));
                    break;
                }
                System.out.print("输入无效，请输入 1 或 2: ");
            }
        }
        System.out.println("\n======= 冲突处理完成，正在应用修改 =======");
    }

    private void handleInsertion(ScrContainerNode baseContainer, ScrNode modNode) {
        // 插入位置：Base 容器的 '}' 之前
        // 注意：baseContainer.getStopIndex() 指向 '}' 字符的位置
        int insertPos = baseContainer.getStopIndex();
        // 构造插入文本：加换行和缩进 (简单模拟，假设是4空格)
        // 如果想做得更完美，可以计算 baseContainer 的缩进层级
        String newContent = "\n    " + modNode.getSourceText();
        finalEdits.add(new EditOp(insertPos, insertPos, newContent));
    }
}
