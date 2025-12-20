package ankol.mod.merger.merger.scr;

import ankol.mod.merger.antlr4.scr.TechlandScriptLexer;
import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import ankol.mod.merger.core.IFileMerger;
import ankol.mod.merger.core.MergerContext;
import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.merger.scr.node.*;
import ankol.mod.merger.tools.FileTree;
import cn.hutool.core.util.StrUtil;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ScrFileMerger extends IFileMerger {
    /**
     * 标记冲突项的容器
     */
    private final List<ConflictRecord> conflicts = new ArrayList<>();
    private final List<EditOp> finalEdits = new ArrayList<>();
    /**
     * Parse 缓存，避免重复解析相同内容的文件
     */
    private static final Map<String, ScrContainerNode> PARSE_CACHE = new WeakHashMap<>();

    public ScrFileMerger(MergerContext context) {
        super(context);
    }

    // 入口
    @Override
    public MergeResult merge(FileTree file1, FileTree file2) {
        try {
            ScrContainerNode baseRoot = parseWithCache(Path.of(file1.getFullPathName()));
            ScrContainerNode modRoot = parseWithCache(Path.of(file2.getFullPathName()));
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

    // ...existing code...

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
                }
                //函数调用，比较参数，不对比String，因为各类mod可能会
                else if (baseNode instanceof ScrFunCallNode baseFunCall && modNode instanceof ScrFunCallNode modFunCall) {
                    if (!baseFunCall.getArguments().equals(modFunCall.getArguments())) {
                        // 参数不一致，标记发生冲突
                        conflicts.add(new ConflictRecord(context.getFileName(), context.getMod1Name(), context.getMod2Name(), signature, baseNode, modNode));
                    }
                }
                //普通子节点，直接处理
                else {
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
     * 冲突解决
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

    /**
     * 带缓存的解析方法
     * <p>
     * 优化：计算文件内容的哈希值作为缓存键，避免解析相同内容的文件多次。
     * 这对于包含大量重复文件的 mod 合并场景特别有效。
     *
     * @param filePath 文件路径
     * @return 解析后的 AST 树
     * @throws IOException 如果文件不可读
     */
    private static ScrContainerNode parseWithCache(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        String contentHash = computeHash(content);
        // 先查缓存
        ScrContainerNode cached = PARSE_CACHE.get(contentHash);
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，执行解析
        ScrContainerNode result = parse(content);
        // 存入缓存
        PARSE_CACHE.put(contentHash, result);
        return result;
    }

    /**
     * 计算字符串内容的 SHA-256 哈希值
     */
    private static String computeHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // 作为备选方案，使用 hashCode()（虽然不如 SHA-256 安全，但足以识别大多数不同的内容）
            return String.valueOf(content.hashCode());
        }
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
