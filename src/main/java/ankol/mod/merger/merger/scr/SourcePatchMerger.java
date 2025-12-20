package ankol.mod.merger.merger.scr;

import ankol.mod.merger.antlr4.scr.TechlandScriptLexer;
import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import ankol.mod.merger.core.IFileMerger;
import ankol.mod.merger.core.MergerContext;
import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.merger.scr.node.ConflictRecord;
import ankol.mod.merger.merger.scr.node.EditOp;
import ankol.mod.merger.merger.scr.node.ScrContainerNode;
import ankol.mod.merger.merger.scr.node.ScrNode;
import ankol.mod.merger.tools.FileTree;
import cn.hutool.core.util.StrUtil;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SourcePatchMerger extends IFileMerger {
    /**
     * 标记冲突项的容器
     */
    private final List<ConflictRecord> conflicts = new ArrayList<>();
    private final List<EditOp> finalEdits = new ArrayList<>();

    public SourcePatchMerger(MergerContext context) {
        super(context);
    }

    // 入口
    @Override
    public MergeResult merge(FileTree file1, FileTree file2) {
        try {
            ScrContainerNode baseRoot = parse(Files.readString(Path.of(file1.getFullPathName())));
            ScrContainerNode modRoot = parse(Files.readString(Path.of(file2.getFullPathName())));
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
            StringBuilder sb = new StringBuilder(baseRoot.getSourceText());
            for (EditOp op : finalEdits) {
                sb.replace(op.getStart(), op.getEnd(), op.getText());
            }
            return new MergeResult(sb.toString(), !conflicts.isEmpty());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
                        conflicts.add(new ConflictRecord(context.getFileName(), context.getMod1Name(), context.getMod2Name(), signature, baseNode, modNode));
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

    private static ScrContainerNode parse(String content) throws IOException {
        CharStream input = CharStreams.fromString(content);
        TechlandScriptLexer lexer = new TechlandScriptLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TechlandScriptParser parser = new TechlandScriptParser(tokens);
        ScrModelVisitor visitor = new ScrModelVisitor();
        // 注意：visitFile 返回的一定是我们定义的 ROOT Container
        return (ScrContainerNode) visitor.visitFile(parser.file());
    }
}
