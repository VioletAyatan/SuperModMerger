package ankol.mod.merger.merger.xml;

import ankol.mod.merger.antlr.xml.TechlandXMLLexer;
import ankol.mod.merger.antlr.xml.TechlandXMLParser;
import ankol.mod.merger.core.AbstractFileMerger;
import ankol.mod.merger.core.BaseTreeNode;
import ankol.mod.merger.core.ConflictResolver;
import ankol.mod.merger.core.MergerContext;
import ankol.mod.merger.exception.BusinessException;
import ankol.mod.merger.merger.ConflictRecord;
import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.merger.xml.node.XmlContainerNode;
import ankol.mod.merger.merger.xml.node.XmlNode;
import ankol.mod.merger.tools.ColorPrinter;
import ankol.mod.merger.tools.FileTree;
import ankol.mod.merger.tools.Localizations;
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
import java.util.Scanner;

/**
 * XML文件合并器
 *
 * @author Ankol
 */
@Slf4j
public class TechlandXmlAbstractFileMerger extends AbstractFileMerger {
    /**
     * 冲突项列表
     */
    private final List<ConflictRecord> conflicts = new ArrayList<>();

    /**
     * 新增节点记录
     */
    private record NewNodeRecord(XmlContainerNode parentContainer, XmlNode previousSibling, XmlNode newNode) {
    }

    /**
     * 新增节点列表
     */
    private final List<NewNodeRecord> newNodes = new ArrayList<>();

    /**
     * 解析结果包装类，包含AST和TokenStream
     */
    private record ParseResult(XmlContainerNode astNode, CommonTokenStream tokens) {
    }

    private static final Cache<String, ParseResult> PARSE_CACHE = CacheUtil.newWeakCache(30 * 1000);

    /**
     * 原始基准MOD对应文件的语法树
     */
    private XmlContainerNode originalBaseModRoot = null;

    public TechlandXmlAbstractFileMerger(MergerContext context) {
        super(context);
    }

    @Override
    public MergeResult merge(FileTree file1, FileTree file2) {
        try {
            // 解析原始基准MOD文件（如果存在）
            if (context.getOriginalBaseModContent() != null) {
                String contentHash = Tools.computeHash(context.getOriginalBaseModContent());
                ParseResult cached = PARSE_CACHE.get(contentHash);
                if (cached != null) {
                    originalBaseModRoot = cached.astNode;
                } else {
                    ParseResult result = parseContent(context.getOriginalBaseModContent());
                    originalBaseModRoot = result.astNode;
                    PARSE_CACHE.put(contentHash, result);
                }
            }

            // 解析base和mod文件
            ParseResult baseResult = parseFile(file1.getFullPathName());
            ParseResult modResult = parseFile(file2.getFullPathName());
            XmlContainerNode baseRoot = baseResult.astNode;
            XmlContainerNode modRoot = modResult.astNode;

            // 递归对比，找到冲突项
            reduceCompare(originalBaseModRoot, baseRoot, modRoot);

            // 第一个mod与原版文件的对比，直接通过，不提示冲突
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
            log.error(StrUtil.format("Error during XML file merge: {} Reason: {}", file1.getFullPathName(), e.getMessage()), e);
            throw new BusinessException("文件" + file1.getFileName() + "合并失败");
        } finally {
            // 清理状态，准备下一个文件合并
            conflicts.clear();
            newNodes.clear();
            originalBaseModRoot = null;
        }
    }

    /**
     * 获取合并后的内容
     */
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

        // 处理新增节点的插入
        for (NewNodeRecord record : newNodes) {
            XmlNode previousSibling = record.previousSibling();
            XmlNode newNode = record.newNode();

            int insertPosition;
            if (previousSibling != null) {
                // 如果有前一个兄弟节点，在其后面插入
                // 使用stopTokenIndex + 1
                insertPosition = previousSibling.getStopTokenIndex() + 1;
            } else {
                // 如果没有前一个兄弟节点（即这是第一个子节点），在父容器的结束标签前面插入
                insertPosition = record.parentContainer().getStopTokenIndex();
            }
            rewriter.insertBefore(insertPosition, "\n" + newNode.getSourceText());
        }

        return rewriter.getText();
    }

    /**
     * 递归对比树节点
     */
    private void reduceCompare(XmlContainerNode originalContainer, XmlContainerNode baseContainer, XmlContainerNode modContainer) {
        // 遍历Mod的所有子节点
        XmlNode previousSiblingInBase = null;  // 追踪前一个兄弟节点
        for (Map.Entry<String, XmlNode> entry : modContainer.getChildren().entrySet()) {
            try {
                String signature = entry.getKey();
                XmlNode modNode = entry.getValue();
                XmlNode originalNode = null;

                if (originalContainer != null) {
                    originalNode = originalContainer.getChildren().get(signature);
                }

                XmlNode baseNode = baseContainer.getChildren().get(signature);

                if (baseNode == null) {
                    // Base中不存在这个节点 - 新增节点，需要添加到合并结果中
                    // 记录前一个兄弟节点，用于确定插入位置
                    newNodes.add(new NewNodeRecord(baseContainer, previousSiblingInBase, modNode));
                } else {
                    // 更新前一个兄弟节点
                    previousSiblingInBase = baseNode;
                    if (baseNode instanceof XmlContainerNode baseContainerNode && modNode instanceof XmlContainerNode modContainerNode) {
                        reduceCompare(
                                (originalNode instanceof XmlContainerNode) ? (XmlContainerNode) originalNode : null,
                                baseContainerNode,
                                modContainerNode
                        );
                    }
                    //子节点，对比内容
                    else if (!(baseNode instanceof XmlContainerNode) && !(modNode instanceof XmlContainerNode)) {
                        // 使用规范化文本进行对比，避免空格、换行等格式差异
                        Map<String, String> baseAttr = baseNode.getAttributes();
                        Map<String, String> modAttr = modNode.getAttributes();
                        if (!baseAttr.equals(modAttr)) {
                            // 不相同，检查是否跟基准mod的一样，不一样视为冲突
                            if (!isNodeSameAsOriginalBaseMod(originalNode, modNode)) {
                                conflicts.add(new ConflictRecord(
                                        context.getFileName(),
                                        context.getMod1Name(),
                                        context.getMod2Name(),
                                        signature,
                                        baseNode,
                                        modNode
                                ));
                            }
                        }
                    } else {
                        // 一个是容器，一个不是容器，这种情况下认为是冲突
                        if (!isNodeSameAsOriginalBaseMod(originalNode, modNode)) {
                            conflicts.add(new ConflictRecord(
                                    context.getFileName(),
                                    context.getMod1Name(),
                                    context.getMod2Name(),
                                    signature,
                                    baseNode,
                                    modNode
                            ));
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in processing XML node with signature: '" + entry.getKey() + "'");
            }
        }
    }

    /**
     * 检查节点是否与原始基准MOD中的对应节点内容相同
     */
    private boolean isNodeSameAsOriginalBaseMod(XmlNode originalNode, XmlNode modNode) {
        // 如果没有原始基准MOD，则认为不相同
        if (originalBaseModRoot == null) {
            return false;
        }
        if (originalNode == null) {
            // 原始基准MOD中不存在这个节点
            return false;
        }
        return modNode.getAttributes().equals(originalNode.getAttributes());
    }

    /**
     * 将XML文件解析成语法树
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
        TechlandXMLLexer lexer = new TechlandXMLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TechlandXMLParser parser = new TechlandXMLParser(tokens);
        TechlandXmlFileVisitor visitor = new TechlandXmlFileVisitor();
        // 访问document节点，应该返回ROOT容器
        XmlNode root = visitor.visitDocument(parser.document());
        XmlContainerNode containerRoot = (XmlContainerNode) root;
        return new ParseResult(containerRoot, tokens);
    }
}