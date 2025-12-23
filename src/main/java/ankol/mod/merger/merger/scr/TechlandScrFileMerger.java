package ankol.mod.merger.merger.scr;

import ankol.mod.merger.antlr4.scr.TechlandScriptLexer;
import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import ankol.mod.merger.core.FileMerger;
import ankol.mod.merger.core.MergerContext;
import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.merger.scr.node.*;
import ankol.mod.merger.tools.ColorPrinter;
import ankol.mod.merger.tools.FileTree;
import ankol.mod.merger.tools.Localizations;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class TechlandScrFileMerger extends FileMerger {
    /**
     * 标记冲突项的容器
     */
    private final List<ConflictRecord> conflicts = new ArrayList<>();
    private final List<EditOp> finalEdits = new ArrayList<>();
    /**
     * Parse 缓存，避免重复解析相同内容的文件
     */
    private static final Map<String, ScrContainerScriptNode> PARSE_CACHE = new WeakHashMap<>();

    /**
     * 基准MOD（data0.pak）对应文件的语法树，用于三方对比
     */
    private ScrContainerScriptNode originalBasModRoot = null;

    public TechlandScrFileMerger(MergerContext context) {
        super(context);
    }

    // 入口
    @Override
    public MergeResult merge(FileTree file1, FileTree file2) {
        try {
            // 解析基准MOD文件（如果存在）
            if (context.getOriginalBaseModContent() != null) {
                String contentHash = computeHash(context.getOriginalBaseModContent());
                ScrContainerScriptNode cached = PARSE_CACHE.get(contentHash);
                if (cached != null) {
                    originalBasModRoot = cached;
                } else {
                    originalBasModRoot = parse(context.getOriginalBaseModContent());
                    PARSE_CACHE.put(contentHash, originalBasModRoot);
                }
            }
            ScrContainerScriptNode baseRoot = parseTree(Path.of(file1.getFullPathName()));
            ScrContainerScriptNode modRoot = parseTree(Path.of(file2.getFullPathName()));
            // 递归对比，找到冲突项
            reduceCompare(originalBasModRoot, baseRoot, modRoot);
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
        } finally {
            //清理状态，准备下一个文件合并
            conflicts.clear();
            finalEdits.clear();
            originalBasModRoot = null;
        }
    }

    private void reduceCompare(ScrContainerScriptNode originalContainer, ScrContainerScriptNode baseContainer, ScrContainerScriptNode modContainer) {
        // 遍历 Mod 的所有子节点
        for (Map.Entry<String, ScrScriptNode> entry : modContainer.getChildren().entrySet()) {
            try {
                String signature = entry.getKey();
                ScrScriptNode modNode = entry.getValue();
                ScrScriptNode originalNode = originalContainer.getChildren().get(signature);
                ScrScriptNode baseNode = baseContainer.getChildren().get(signature);

                if (baseNode == null) {
                    // 新增 Base 没有这个节点 -> 插入
                    handleInsertion(baseContainer, modNode);
                } else {
                    // [存在] 检查是否冲突
                    if (baseNode instanceof ScrContainerScriptNode && modNode instanceof ScrContainerScriptNode) {
                        // 容器节点，递归进入内部对比
                        reduceCompare((ScrContainerScriptNode) originalNode, (ScrContainerScriptNode) baseNode, (ScrContainerScriptNode) modNode);
                    }
                    //函数调用，比较参数，不对比String，因为对比字符会有各种空行不规范问题
                    else if (baseNode instanceof ScrFunCallScriptNode baseFunCall && modNode instanceof ScrFunCallScriptNode modFunCall) {
                        if (!baseFunCall.getArguments().equals(modFunCall.getArguments())) {
                            // 参数不一致，检查是否与原始基准MOD相同
                            if (!isNodeSameAsOriginalBaseMod(originalNode, modNode)) {
                                // 与原始基准MOD不同，才标记为冲突
                                conflicts.add(new ConflictRecord(context.getFileName(), context.getMod1Name(), context.getMod2Name(), signature, baseNode, modNode));
                            }
                        }
                    }
                    //普通子节点，直接处理
                    else {
                        // 叶子节点，对比内容
                        String baseText = baseNode.getSourceText().trim();
                        String modText = modNode.getSourceText().trim();
                        //内容不一致
                        if (!baseText.equals(modText)) {
                            // 检查modNode是否与原始基准MOD相同
                            if (!isNodeSameAsOriginalBaseMod(originalNode, modNode)) {
                                conflicts.add(new ConflictRecord(context.getFileName(), context.getMod1Name(), context.getMod2Name(), signature, baseNode, modNode));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in processing scr node with signature: '" + entry.getKey() + " Source code: " + originalContainer.getSourceText());
            }
        }
    }

    /**
     * 检查节点是否与原始基准MOD中的对应节点内容相同
     *
     * @param originalNode 节点签名
     * @param modNode      待检查的节点
     * @return 如果与原始基准MOD相同返回true，否则返回false
     */
    private boolean isNodeSameAsOriginalBaseMod(ScrScriptNode originalNode, ScrScriptNode modNode) {
        // 如果没有原始基准MOD，则认为不相同
        if (originalBasModRoot == null) {
            return false;
        }

        if (originalNode == null) {
            // 原始基准MOD中不存在这个节点，说明是新增的
            return false;
        }

        // 对比节点内容
        if (modNode instanceof ScrFunCallScriptNode modFunCall && originalNode instanceof ScrFunCallScriptNode originalFunCall) {
            // 函数调用节点，对比参数
            return modFunCall.getArguments().equals(originalFunCall.getArguments());
        } else {
            // 其他节点，对比文本内容
            return modNode.getSourceText().trim().equals(originalNode.getSourceText().trim());
        }
    }

    /**
     * 根据签名在容器中递归查找节点
     *
     * @param container 容器节点
     * @param signature 节点签名
     * @return 找到的节点，如果不存在返回null
     */
    private ScrScriptNode findNodeBySignature(ScrContainerScriptNode container, String signature) {
        // 检查当前容器的直接子节点
        ScrScriptNode node = container.getChildren().get(signature);
        if (node != null) {
            return node;
        }

        // 递归检查所有容器类型的子节点
        for (ScrScriptNode child : container.getChildren().values()) {
            if (child instanceof ScrContainerScriptNode childContainer) {
                ScrScriptNode found = findNodeBySignature(childContainer, signature);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /**
     * 冲突解决
     */
    private void resolveConflictsInteractively() {
        Scanner scanner = new Scanner(System.in);
        ColorPrinter.warning(Localizations.t("SCR_MERGER_CONFLICT_DETECTED", conflicts.size()));
        int chose = 0;
        for (int i = 0; i < conflicts.size(); i++) {
            ConflictRecord record = conflicts.get(i);
            if (chose == 3) {
                record.setUserChoice(1); //3表示用户全部选择baseMod的配置来处理
            } else if (chose == 4) {
                record.setUserChoice(2); //4表示用户全部选择mergeMod的配置来处理
            } else {
                ColorPrinter.info("=".repeat(75));
                ColorPrinter.info(Localizations.t("SCR_MERGER_FILE_INFO", i + 1, conflicts.size(), record.getFileName()));
//            ColorPrinter.highlight("位置签名: {}", record.getSignature());
                //打印代码提示框
                ColorPrinter.warning(Localizations.t("SCR_MERGER_MOD_VERSION_1", record.getBaseModName()));
                ColorPrinter.bold(Localizations.t("SCR_MERGER_LINE_INFO", record.getBaseNode().getLine(), record.getBaseNode().getSourceText().trim()));
                ColorPrinter.warning(Localizations.t("SCR_MERGER_MOD_VERSION_2", record.getMergeModName()));
                ColorPrinter.bold(Localizations.t("SCR_MERGER_LINE_INFO", record.getModNode().getLine(), record.getModNode().getSourceText().trim()));
                ColorPrinter.info("=".repeat(75));
                //选择对话框
                ColorPrinter.info(Localizations.t("SCR_MERGER_CHOOSE_PROMPT"));
                ColorPrinter.info(Localizations.t("SCR_MERGER_USE_OPTION_1", record.getBaseNode().getSourceText()));
                ColorPrinter.info(Localizations.t("SCR_MERGER_USE_OPTION_2", record.getModNode().getSourceText()));
                ColorPrinter.info(Localizations.t("SCR_MERGER_USE_ALL_FROM_MOD_1", record.getBaseModName()));
                ColorPrinter.info(Localizations.t("SCR_MERGER_USE_ALL_FROM_MOD_2", record.getMergeModName()));

                while (true) {
                    String input = scanner.nextLine();
                    if (input.equals("1") || input.equals("2")) {
                        record.setUserChoice(Integer.parseInt(input));
                        break;
                    }
                    if (input.equals("3") || input.equals("4")) {
                        chose = Integer.parseInt(input);
                        record.setUserChoice(chose == 3 ? 1 : 2);
                        break;
                    }
                    ColorPrinter.warning(Localizations.t("SCR_MERGER_INVALID_INPUT"));
                }
            }
        }
        ColorPrinter.success(Localizations.t("SCR_MERGER_CONFLICT_RESOLVED"));
    }

    private void handleInsertion(ScrContainerScriptNode baseContainer, ScrScriptNode modNode) {
        // 插入位置：Base 容器的 '}' 之前
        // baseContainer.getStopIndex() 指向 '}' 字符的位置
        int insertPos = baseContainer.getStopIndex();
        String newContent = "\n    " + modNode.getSourceText();
        finalEdits.add(new EditOp(insertPos, insertPos, newContent));
    }

    /**
     * 将MOD文件解析成语法树
     *
     * @param filePath 文件路径
     * @return 解析后的 AST 树
     * @throws IOException 如果文件不可读
     */
    private static ScrContainerScriptNode parseTree(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        String contentHash = computeHash(content);
        // 先查缓存
        ScrContainerScriptNode cached = PARSE_CACHE.get(contentHash);
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，执行解析
        ScrContainerScriptNode result = parse(content);
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

    private static ScrContainerScriptNode parse(String content) throws IOException {
        CharStream input = CharStreams.fromString(content);
        TechlandScriptLexer lexer = new TechlandScriptLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TechlandScriptParser parser = new TechlandScriptParser(tokens);
        TechlandScrFileVisitor visitor = new TechlandScrFileVisitor();
        // 注意：visitFile 返回的一定是我们定义的 ROOT Container
        return (ScrContainerScriptNode) visitor.visitFile(parser.file());
    }
}
