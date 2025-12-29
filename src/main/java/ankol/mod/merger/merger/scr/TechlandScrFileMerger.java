package ankol.mod.merger.merger.scr;

import ankol.mod.merger.antlr.scr.TechlandScriptLexer;
import ankol.mod.merger.antlr.scr.TechlandScriptParser;
import ankol.mod.merger.core.AbstractFileMerger;
import ankol.mod.merger.core.BaseTreeNode;
import ankol.mod.merger.core.ConflictResolver;
import ankol.mod.merger.core.MergerContext;
import ankol.mod.merger.exception.BusinessException;
import ankol.mod.merger.merger.ConflictRecord;
import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.merger.scr.node.ScrContainerScriptNode;
import ankol.mod.merger.merger.scr.node.ScrFunCallScriptNode;
import ankol.mod.merger.tools.FileTree;
import ankol.mod.merger.tools.Tools;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class TechlandScrFileMerger extends AbstractFileMerger {
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
    private static final Cache<String, ParseResult> PARSE_CACHE = CacheUtil.newWeakCache(30 * 1000);

    /**
     * 基准MOD（data0.pak）对应文件的语法树，用于三方对比
     */
    private ScrContainerScriptNode originalBaseModRoot = null;

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
                ParseResult parseResult = PARSE_CACHE.get(contentHash, () -> parseContent(context.getOriginalBaseModContent()));
                originalBaseModRoot = parseResult.astNode;
            }
            // 解析base和mod文件，保留TokenStream
            ParseResult baseResult = parseFile(file1.getFullPathName());
            ParseResult modResult = parseFile(file2.getFullPathName());
            ScrContainerScriptNode baseRoot = baseResult.astNode;
            ScrContainerScriptNode modRoot = modResult.astNode;
            // 递归对比，找到冲突项
            reduceCompare(originalBaseModRoot, baseRoot, modRoot);
            //第一个mod与原版文件的对比，直接使用MOD修改的版本，不提示冲突
            if (context.isFirstModMergeWithBaseMod() && !conflicts.isEmpty()) {
                for (ConflictRecord record : conflicts) {
                    record.setUserChoice(2); // 自动选择第一个mod的版本
                }
            } else if (!conflicts.isEmpty()) {
                // 正常情况下，提示用户解决冲突
                ConflictResolver.resolveConflict(conflicts);
            }
            return new MergeResult(getMergedContent(baseResult), !conflicts.isEmpty());
        } catch (Exception e) {
            log.error(StrUtil.format("Error during SCR file merge: {} Reason: {}", file1.getFileName(), e.getMessage()), e);
            throw new BusinessException("文件" + file1.getFileName() + "合并失败");
        } finally {
            //清理状态，准备下一个文件合并
            conflicts.clear();
            insertOperations.clear();
            originalBaseModRoot = null;
        }
    }

    private void reduceCompare(ScrContainerScriptNode originalContainer, ScrContainerScriptNode baseContainer, ScrContainerScriptNode modContainer) {
        // 遍历 Mod 的所有子节点
        for (Map.Entry<String, BaseTreeNode> entry : modContainer.getChildren().entrySet()) {
            try {
                String signature = entry.getKey();
                BaseTreeNode modNode = entry.getValue();
                BaseTreeNode originalNode = null;
                if (originalContainer != null) {
                    originalNode = originalContainer.getChildren().get(signature);
                }
                BaseTreeNode baseNode = baseContainer.getChildren().get(signature);

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
                        String baseText = baseNode.getSourceText();
                        String modText = modNode.getSourceText();
                        //内容不一致
                        if (!equalsTrimmed(baseText, modText)) {
                            // 检查modNode是否与原始基准MOD相同
                            if (!isNodeSameAsOriginalNode(originalNode, modNode)) {
                                conflicts.add(new ConflictRecord(context.getFileName(), context.getMod1Name(), context.getMod2Name(), signature, baseNode, modNode));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error in processing scr node with signature: " + entry.getKey(), e);
            }
        }
    }

    private String getMergedContent(ParseResult baseResult) {
        TokenStreamRewriter rewriter = new TokenStreamRewriter(baseResult.tokens);
        // 处理冲突节点的替换
        for (ConflictRecord record : conflicts) {
            if (record.getUserChoice() == 2) { // 用户选择了 Mod
                BaseTreeNode baseNode = record.getBaseNode();
                BaseTreeNode modNode = record.getModNode();
                rewriter.replace(
                        baseNode.getStartTokenIndex(),
                        baseNode.getStopTokenIndex(),
                        modNode.getSourceText()
                );
            }
        }
        for (InsertOperation op : insertOperations) {
            rewriter.insertBefore(op.tokenIndex, op.content);
        }

        // 获取重写后的文本
        return rewriter.getText();
    }

    private boolean isNodeSameAsOriginalNode(BaseTreeNode originalNode, BaseTreeNode modNode) {
        // 如果没有原始基准MOD，则认为不相同
        if (originalBaseModRoot == null) {
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
            return equalsTrimmed(modNode.getSourceText(), originalNode.getSourceText());
        }
    }

    private void handleInsertion(ScrContainerScriptNode baseContainer, BaseTreeNode modNode) {
        // 插入位置：Base 容器的 '}' 之前
        int insertPos = baseContainer.getStopTokenIndex();
        String newContent = "\n    " + modNode.getSourceText();
        insertOperations.add(new InsertOperation(insertPos, newContent));
    }

    private static ParseResult parseFile(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        String contentHash = Tools.computeHash(content);
        return PARSE_CACHE.get(contentHash, () -> parseContent(content));
    }

    private static ParseResult parseContent(String content) {
        CharStream input = CharStreams.fromString(content);
        TechlandScriptLexer lexer = new TechlandScriptLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TechlandScriptParser parser = new TechlandScriptParser(tokens);
        TechlandScrFileVisitor visitor = new TechlandScrFileVisitor(tokens);
        // 注意：visitFile 返回的一定是我们定义的 ROOT Container
        ScrContainerScriptNode ast = (ScrContainerScriptNode) visitor.visitFile(parser.file());
        return new ParseResult(ast, tokens);
    }

    private static boolean equalsTrimmed(String a, String b) {
        // 快路径：同一引用或 equals（完全一致时直接返回，避免扫描）
        if (Objects.equals(a, b)) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }

        int ai = 0;
        int bi = 0;
        int alen = a.length();
        int blen = b.length();

        while (true) {
            // 跳过 a 的所有空白
            while (ai < alen && Character.isWhitespace(a.charAt(ai))) {
                ai++;
            }
            // 跳过 b 的所有空白
            while (bi < blen && Character.isWhitespace(b.charAt(bi))) {
                bi++;
            }

            // 两边都到尾了 -> 相等
            if (ai >= alen || bi >= blen) {
                // 需要确保剩余部分也都是空白
                while (ai < alen && Character.isWhitespace(a.charAt(ai))) ai++;
                while (bi < blen && Character.isWhitespace(b.charAt(bi))) bi++;
                return ai >= alen && bi >= blen;
            }

            // 比较当前非空白字符
            if (a.charAt(ai) != b.charAt(bi)) {
                return false;
            }
            ai++;
            bi++;
        }
    }
}
