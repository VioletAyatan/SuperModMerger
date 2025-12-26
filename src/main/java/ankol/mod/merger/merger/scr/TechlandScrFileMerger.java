package ankol.mod.merger.merger.scr;

import ankol.mod.merger.antlr.scr.TechlandScriptLexer;
import ankol.mod.merger.antlr.scr.TechlandScriptParser;
import ankol.mod.merger.core.FileMerger;
import ankol.mod.merger.core.MergerContext;
import ankol.mod.merger.exception.BusinessException;
import ankol.mod.merger.merger.ConflictRecord;
import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.merger.scr.node.*;
import ankol.mod.merger.tools.ColorPrinter;
import ankol.mod.merger.tools.FileTree;
import ankol.mod.merger.tools.Localizations;
import ankol.mod.merger.tools.Tools;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
public class TechlandScrFileMerger extends FileMerger {
    /**
     * 标记冲突项的容器
     */
    private final List<ConflictRecord> conflicts = new ArrayList<>();

    /**
     * 插入操作记录
     */
    private record InsertOperation(int tokenIndex, String content) {
    }

    private final List<InsertOperation> insertOperations = new ArrayList<>();

    /**
     * Parse 缓存，避免重复解析相同内容的文件
     * 存储 ParseResult 包含 AST 和 TokenStream
     */
    private static final Map<String, ParseResult> PARSE_CACHE = new WeakHashMap<>();

    /**
     * 基准MOD（data0.pak）对应文件的语法树，用于三方对比
     */
    private ScrContainerScriptNode originalBasModRoot = null;

    /**
     * 解析结果包装类，包含AST和TokenStream
     */
    private record ParseResult(ScrContainerScriptNode astNode, CommonTokenStream tokens) {
    }

    public TechlandScrFileMerger(MergerContext context) {
        super(context);
    }

    @Override
    public MergeResult merge(FileTree file1, FileTree file2) {
        try {
            // 解析基准MOD文件（如果存在）
            if (context.getOriginalBaseModContent() != null) {
                String contentHash = Tools.computeHash(context.getOriginalBaseModContent());
                ParseResult cached = PARSE_CACHE.get(contentHash);
                if (cached != null) {
                    originalBasModRoot = cached.astNode;
                } else {
                    ParseResult result = parseContent(context.getOriginalBaseModContent());
                    originalBasModRoot = result.astNode;
                    PARSE_CACHE.put(contentHash, result);
                }
            }
            // 解析base和mod文件，保留TokenStream
            ParseResult baseResult = parseFile(file1.getFullPathName());
            ParseResult modResult = parseFile(file2.getFullPathName());
            ScrContainerScriptNode baseRoot = baseResult.astNode;
            ScrContainerScriptNode modRoot = modResult.astNode;
            // 递归对比，找到冲突项
            reduceCompare(originalBasModRoot, baseRoot, modRoot);
            //第一个mod与原版文件的对比，直接使用MOD修改的版本，不提示冲突
            if (context.isFirstModMergeWithBaseMod() && !conflicts.isEmpty()) {
                for (ConflictRecord record : conflicts) {
                    record.setUserChoice(2); // 自动选择第一个mod的版本
                }
            } else if (!conflicts.isEmpty()) {
                // 正常情况下，提示用户解决冲突
                resolveConflictsInteractively();
            }
            String mergedContent = getMergedContent(baseResult);
            return new MergeResult(mergedContent, !conflicts.isEmpty());
        } catch (Exception e) {
            log.error(StrUtil.format("Error during SCR file merge: {} Reason: {}", file1.getFileName(), e.getMessage()), e);
            throw new BusinessException("文件" + file1.getFileName() + "合并失败");
        } finally {
            //清理状态，准备下一个文件合并
            conflicts.clear();
            insertOperations.clear();
            originalBasModRoot = null;
        }
    }

    private String getMergedContent(ParseResult baseResult) {
        TokenStreamRewriter rewriter = new TokenStreamRewriter(baseResult.tokens);

        // 处理冲突节点的替换
        for (ConflictRecord record : conflicts) {
            if (record.getUserChoice() == 2) { // 用户选择了 Mod
                ScrScriptNode baseNode = record.getBaseNode();
                ScrScriptNode modNode = record.getModNode();

                // 直接使用节点中存储的token索引
                rewriter.replace(
                        baseNode.getStartTokenIndex(),
                        baseNode.getStopTokenIndex(),
                        modNode.getSourceText()
                );
            }
        }
/*        if (!automaticMergeNode.isEmpty()) {
            ColorPrinter.success(Localizations.t("SCR_MERGER_AUTO_MERGE_COUNT", automaticMergeNode.size()));
            //智能合并节点的替换
            for (ConflictRecord record : automaticMergeNode) {
                ScrScriptNode baseNode = record.getBaseNode();
                ScrScriptNode modNode = record.getModNode();

                // 直接使用节点中存储的token索引
                rewriter.replace(
                        baseNode.getStartTokenIndex(),
                        baseNode.getStopTokenIndex(),
                        modNode.getSourceText()
                );
            }
        }*/


        // 处理新增节点（插入操作）
        for (InsertOperation op : insertOperations) {
            rewriter.insertBefore(op.tokenIndex, op.content);
        }

        // 获取重写后的文本
        return rewriter.getText();
    }

    private void reduceCompare(ScrContainerScriptNode originalContainer, ScrContainerScriptNode baseContainer, ScrContainerScriptNode modContainer) {
        // 遍历 Mod 的所有子节点
        for (Map.Entry<String, ScrScriptNode> entry : modContainer.getChildren().entrySet()) {
            try {
                String signature = entry.getKey();
                ScrScriptNode modNode = entry.getValue();
                ScrScriptNode originalNode = null;
                if (originalContainer != null) {
                    originalNode = originalContainer.getChildren().get(signature);
                }
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
                        //先检查当前待合并节点内容是否相同
                        if (!baseFunCall.getArguments().equals(modFunCall.getArguments())) {
                            //两者内容不同，检查mod节点内容与原版是否相同
                            if (!isNodeSameAsOriginalNode(originalNode, modNode)) {
                                //当前节点与原版一致，说明此处节点未变动，使用mod的内容
                                if (isNodeSameAsOriginalNode(originalNode, baseFunCall)) {
                                    ConflictRecord record = new ConflictRecord(context.getFileName(),
                                            context.getMod1Name(),
                                            context.getMod2Name(),
                                            signature,
                                            baseNode,
                                            modNode);
                                    record.setUserChoice(2);
                                    conflicts.add(record);
                                }
                                //当前节点不与原版相同，也不与待合并mod相同，标记为真正的冲突处
                                else {
                                    conflicts.add(new ConflictRecord(context.getFileName(),
                                            context.getMod1Name(),
                                            context.getMod2Name(),
                                            signature,
                                            baseNode,
                                            modNode));
                                }
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
                            if (!isNodeSameAsOriginalNode(originalNode, modNode)) {
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
    private boolean isNodeSameAsOriginalNode(ScrScriptNode originalNode, ScrScriptNode modNode) {
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
     * 冲突解决
     */
    private void resolveConflictsInteractively() {
        //筛选出智能合并的节点
        List<ConflictRecord> automaticMerge = conflicts.stream()
                .filter(conflict -> conflict.getUserChoice() != null)
                .toList();
        if (!automaticMerge.isEmpty()) {
            ColorPrinter.success(Localizations.t("SCR_MERGER_AUTO_MERGE_COUNT", automaticMerge.size()));
            conflicts.removeAll(automaticMerge);
        }
        if (conflicts.isEmpty()) {
            conflicts.addAll(automaticMerge); //智能合并的节点加回去，这里注意是为了不出现冲突提示
        } else {
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
            conflicts.addAll(automaticMerge);  //智能合并的节点加回去，这里注意是为了不出现冲突提示
            ColorPrinter.success(Localizations.t("SCR_MERGER_CONFLICT_RESOLVED"));
        }
    }

    private void handleInsertion(ScrContainerScriptNode baseContainer, ScrScriptNode modNode) {
        // 插入位置：Base 容器的 '}' 之前
        int insertPos = baseContainer.getStopTokenIndex();
        String newContent = "\n    " + modNode.getSourceText();
        insertOperations.add(new InsertOperation(insertPos, newContent));
    }

    /**
     * 将MOD文件解析成语法树
     *
     * @param filePath 文件路径
     * @return 解析后的 ParseResult（包含AST和TokenStream）
     * @throws IOException 如果文件不可读
     */
    private static ParseResult parseFile(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        String contentHash = Tools.computeHash(content);
        // 先查缓存
        ParseResult cached = PARSE_CACHE.get(contentHash);
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，执行解析
        ParseResult result = parseContent(content);
        // 存入缓存
        PARSE_CACHE.put(contentHash, result);
        return result;
    }

    /**
     * 解析字符串内容为ParseResult
     */
    private static ParseResult parseContent(String content) {
        CharStream input = CharStreams.fromString(content);
        TechlandScriptLexer lexer = new TechlandScriptLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TechlandScriptParser parser = new TechlandScriptParser(tokens);
        TechlandScrFileVisitor visitor = new TechlandScrFileVisitor();
        // 注意：visitFile 返回的一定是我们定义的 ROOT Container
        ScrContainerScriptNode ast = (ScrContainerScriptNode) visitor.visitFile(parser.file());
        return new ParseResult(ast, tokens);
    }
}
