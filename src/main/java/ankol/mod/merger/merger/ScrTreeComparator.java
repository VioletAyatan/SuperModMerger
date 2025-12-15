package ankol.mod.merger.merger;

import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 语法树对比器 - 比较两个脚本的语法树，找出差异部分
 * <p>
 * 功能：
 * 1. 对比两个脚本的AST结构
 * 2. 按定义类型和名称索引脚本元素
 * 3. 精确识别哪些定义被删除、修改或新增
 * 4. 生成差异报告 (DiffResult)
 * <p>
 * 核心算法：
 * - 将两个脚本的所有顶级定义按唯一键索引
 * - 唯一键 = 定义类型 + 名称（如 "sub:player_init"）
 * - 对比两个索引表，找出差异
 */
public class ScrTreeComparator {

    /**
     * 差异结果内部类 - 表示两个脚本之间的一个差异
     * <p>
     * 字段说明：
     * - tree1: 模组1中的定义（如果为null表示该定义只存在于模组2）
     * - tree2: 模组2中的定义（如果为null表示该定义只存在于模组1）
     * - ruleName: 语法规则名称（如 "definition"）
     * - description: 人类可读的差异描述（如 "Different: sub:player_init"）
     */
    public static class DiffResult {
        /**
         * 模组1中的语法树节点（可能为null）
         */
        public final ParseTree tree1;
        /**
         * 模组2中的语法树节点（可能为null）
         */
        public final ParseTree tree2;
        /**
         * 语法规则的名称
         */
        public final String ruleName;
        /**
         * 差异的文字描述
         */
        public final String description;

        /**
         * 构造函数 - 创建一个差异结果
         *
         * @param tree1       模组1的节点
         * @param tree2       模组2的节点
         * @param ruleName    规则名称
         * @param description 描述
         */
        public DiffResult(ParseTree tree1, ParseTree tree2, String ruleName, String description) {
            this.tree1 = tree1;
            this.tree2 = tree2;
            this.ruleName = ruleName;
            this.description = description;
        }

        /**
         * 返回差异的字符串表示，用于输出到用户
         *
         * @return 格式化的差异描述
         */
        @Override
        public String toString() {
            String text1 = tree1 != null ? tree1.getText() : "null";
            String text2 = tree2 != null ? tree2.getText() : "null";
            return "[" + ruleName + "] " + description + "\nTree1: " + text1 + "\nTree2: " + text2;
        }
    }

    /**
     * 对比两个脚本文件的语法树
     * <p>
     * 执行流程：
     * 1. 获取两个文件的所有顶级定义
     * 2. 将定义列表转换为索引表（按唯一键）
     * 3. 遍历模组1的定义：
     * - 如果在模组2中不存在，记录为"删除"
     * - 如果存在但内容不同，记录为"修改"
     * 4. 遍历模组2的定义：
     * - 如果在模组1中不存在，记录为"新增"
     * 5. 返回所有差异结果
     *
     * @param file1 模组1的语法树根节点
     * @param file2 模组2的语法树根节点
     * @return 差异列表，每个元素代表一个差异
     * 如果列表为空，表示两个脚本完全相同
     */
    public static List<DiffResult> compareFiles(TechlandScriptParser.FileContext file1,
                                                TechlandScriptParser.FileContext file2) {
        List<DiffResult> diffs = new ArrayList<>();

        // 获取两个文件的所有顶级定义（import, export, sub, 等）
        List<TechlandScriptParser.DefinitionContext> defs1 = file1.definition();
        List<TechlandScriptParser.DefinitionContext> defs2 = file2.definition();

        // 将定义列表转换为索引表，以唯一键为索引
        // 键的格式：type:name（如 "sub:player_init"）
        Map<String, TechlandScriptParser.DefinitionContext> map1 = indexDefinitions(defs1);
        Map<String, TechlandScriptParser.DefinitionContext> map2 = indexDefinitions(defs2);

        // 检查模组1中的定义
        for (String key : map1.keySet()) {
            if (!map2.containsKey(key)) {
                // 该定义只存在于模组1，在模组2中不存在（删除）
                diffs.add(new DiffResult(map1.get(key), null, "definition", "In Mod2: " + key));
            } else if (!treeEquals(map1.get(key), map2.get(key))) {
                // 该定义在两个模组中都存在，但内容不同（修改）
                diffs.add(new DiffResult(map1.get(key), map2.get(key), "definition", "Different: " + key));
            }
            // 如果内容相同，不记录差异
        }

        // 检查模组2中的定义
        for (String key : map2.keySet()) {
            if (!map1.containsKey(key)) {
                // 该定义只存在于模组2，在模组1中不存在（新增）
                diffs.add(new DiffResult(null, map2.get(key), "definition", "In Mod2: " + key));
            }
            // 已经存在于模组1的定义已经在上面处理过了，不需要重复处理
        }

        return diffs;
    }

    /**
     * 将定义列表转换为索引表
     * <p>
     * 目的：为了快速查找和对比定义
     * <p>
     * 执行流程：
     * 1. 创建一个LinkedHashMap（有序Map，保持插入顺序）
     * 2. 遍历所有定义
     * 3. 为每个定义提取唯一键
     * 4. 将定义存入索引表
     *
     * @param definitions 定义列表
     * @return 索引表，键为唯一键，值为定义的语法树节点
     */
    private static Map<String, TechlandScriptParser.DefinitionContext> indexDefinitions(
            List<TechlandScriptParser.DefinitionContext> definitions) {
        // 使用LinkedHashMap保持定义的原始顺序
        Map<String, TechlandScriptParser.DefinitionContext> map = new LinkedHashMap<>();

        // 遍历所有定义
        for (TechlandScriptParser.DefinitionContext def : definitions) {
            // 为定义提取唯一键（type:name 格式）
            String key = extractDefinitionKey(def);
            if (key != null) {
                // 将定义存入索引表
                map.put(key, def);
            }
        }

        return map;
    }

    /**
     * 从定义的语法树节点提取唯一键
     * <p>
     * 唯一键用于识别和区分定义。键的格式是 "type:name"：
     * - "import:import_statement_text"  - 导入语句
     * - "export:variable_name"          - 导出变量
     * - "sub:function_name"             - 函数声明
     * - "macro:macro_name"              - 宏定义
     * - "var:variable_name"             - 变量声明
     * - "directive:directive_name"      - 指令调用
     * - "call:function_name"            - 函数调用
     * - "block:block_name"              - 代码块
     * <p>
     * 对于没有名称的定义（如import），使用完整的文本作为键。
     *
     * @param def 定义的语法树节点
     * @return 唯一键，如果无法确定则返回 "unknown:" + 文本
     */
    private static String extractDefinitionKey(TechlandScriptParser.DefinitionContext def) {
        if (def.importDecl() != null) {
            // 导入声明：import "path"
            return "import:" + def.importDecl().getText();
        } else if (def.exportDecl() != null) {
            // 导出声明：export type name = value
            TechlandScriptParser.ExportDeclContext exp = def.exportDecl();
            String id = exp.Id() != null ? exp.Id().getText() : "";
            return "export:" + id;
        } else if (def.subDecl() != null) {
            // 函数声明：sub name() { ... }
            String id = def.subDecl().Id().getText();
            return "sub:" + id;
        } else if (def.macroDecl() != null) {
            // 宏定义：$MACRO(params)
            String id = def.macroDecl().MacroId().getText();
            return "macro:" + id;
        } else if (def.variableDecl() != null) {
            // 变量声明：type name = value
            String id = def.variableDecl().Id().getText();
            return "var:" + id;
        } else if (def.directiveCall() != null) {
            // 指令调用：!directive(params)
            String id = def.directiveCall().Id().getText();
            return "directive:" + id;
        } else if (def.funtionCallDecl() != null) {
            // 函数调用：function(params)
            String id = def.funtionCallDecl().Id().getText();
            return "call:" + id;
        } else if (def.funtionBlockDecl() != null) {
            // 函数块：name(params) { ... }
            String id = def.funtionBlockDecl().Id().getText();
            return "block:" + id;
        }

        // 未知定义类型，使用完整文本作为键
        return "unknown:" + def.getText();
    }

    /**
     * 比较两个语法树节点是否相等
     * <p>
     * 比较方法：获取两个节点的文本表示，去除首尾空格后进行字符串比较
     * 这种方法虽然简单，但对于大多数场景都够用，因为相同的语义应该产生相同的文本
     * <p>
     * 处理的特殊情况：
     * - 如果两个都为null，返回true（都不存在，视为相等）
     * - 如果其中一个为null，返回false（一个存在一个不存在）
     *
     * @param tree1 第一个语法树节点
     * @param tree2 第二个语法树节点
     * @return true 如果两个节点的文本表示相同，false 否则
     */
    public static boolean treeEquals(ParseTree tree1, ParseTree tree2) {
        // 都为null，视为相等
        if (tree1 == null && tree2 == null) return true;
        // 其中一个为null，不相等
        if (tree1 == null || tree2 == null) return false;

        // 获取节点的文本表示，去除首尾空格
        String text1 = tree1.getText().trim();
        String text2 = tree2.getText().trim();

        // 比较文本是否相同
        return text1.equals(text2);
    }

    /**
     * 获取语法树节点的文本表示
     * <p>
     * 这个方法用于将语法树节点转换为人类可读的文本，便于显示和调试。
     *
     * @param tree 语法树节点
     * @return 节点的文本表示，如果节点为null返回空字符串
     */
    public static String getTreeText(ParseTree tree) {
        if (tree == null) return "";
        return tree.getText();
    }

    /**
     * 获取语法树节点在原始文本中的行号
     * <p>
     * 用途：在报告错误或差异时，显示定义的位置信息，便于用户找到源代码。
     *
     * @param tree 语法树节点
     * @return 行号（1-based），如果无法确定则返回-1
     */
    public static int getLineNumber(ParseTree tree) {
        if (tree instanceof RuleContext) {
            RuleContext ctx = (RuleContext) tree;
            // getSourceInterval() 返回这个节点在源代码中的区间
            // 区间的第一个索引是���始位置
            if (ctx.getSourceInterval().a >= 0) {
                return ctx.getSourceInterval().a;
            }
        }
        return -1;
    }
}

