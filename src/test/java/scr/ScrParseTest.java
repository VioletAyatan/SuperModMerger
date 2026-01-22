package scr;

import ankol.mod.merger.antlr.scr.TechlandScriptLexer;
import ankol.mod.merger.antlr.scr.TechlandScriptParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import static org.junit.Assert.*;

public class ScrParseTest {

    @Test
    public void testFunctionBlockWithNewline() {
        // 测试换行的函数块声明
        String code = "UIStats()\n" +
                "        {\n" +
                "         UIStat(\"&StatsUI_Hook&\", \"&Hook4&\");\n" +
                "		 UIStat(\"&StatsUI_Hook&\", \"&Hook1&\");\n" +
                "        }\n";

        TechlandScriptParser parser = createParser(code);
        TechlandScriptParser.FileContext fileCtx = parser.file();

        // 打印解析树结构
        System.out.println("Parse tree: " + fileCtx.toStringTree(parser));

        // 应该只有一个 definition
        assertEquals("应该只有一个 definition", 1, fileCtx.definition().size());

        // 这个 definition 应该是 funtionBlockDecl
        TechlandScriptParser.DefinitionContext def = fileCtx.definition(0);
        assertNotNull("应该被解析为 funtionBlockDecl", def.funtionBlockDecl());
        assertNull("不应该被解析为 funtionCallDecl", def.funtionCallDecl());

        System.out.println("✓ 换行的函数块声明解析正确");
    }

    @Test
    public void testFunctionBlockInline() {
        // 测试内联的函数块声明
        String code = "UIStats() {\n" +
                "    UIStat(\"&StatsUI_Hook&\", \"&Hook4&\");\n" +
                "}\n";

        TechlandScriptParser parser = createParser(code);
        TechlandScriptParser.FileContext fileCtx = parser.file();

        assertEquals("应该只有一个 definition", 1, fileCtx.definition().size());

        TechlandScriptParser.DefinitionContext def = fileCtx.definition(0);
        assertNotNull("应该被解析为 funtionBlockDecl", def.funtionBlockDecl());

        System.out.println("✓ 内联函数块声明解析正确");
    }

    @Test
    public void testFunctionCall() {
        // 测试普通函数调用
        String code = "UIStat(\"&StatsUI_Hook&\", \"&Hook4&\");\n";

        TechlandScriptParser parser = createParser(code);
        TechlandScriptParser.FileContext fileCtx = parser.file();

        assertEquals("应该只有一个 definition", 1, fileCtx.definition().size());

        TechlandScriptParser.DefinitionContext def = fileCtx.definition(0);
        assertNotNull("应该被解析为 funtionCallDecl", def.funtionCallDecl());

        System.out.println("✓ 函数调用解析正确");
    }

    @Test
    public void testVariableDecl() {
        // 测试变量声明
        String code = "float health = 100.0;\n";

        TechlandScriptParser parser = createParser(code);
        TechlandScriptParser.FileContext fileCtx = parser.file();

        System.out.println("Parse tree: " + fileCtx.toStringTree(parser));

        assertEquals("应该只有一个 definition", 1, fileCtx.definition().size());

        TechlandScriptParser.DefinitionContext def = fileCtx.definition(0);
        assertNotNull("应该被解析为 variableDecl", def.variableDecl());

        System.out.println("✓ 变量声明解析正确");
    }

    @Test
    public void testNestedFunctionBlockInSub() {
        // 测试 sub 函数内部的嵌套函数块（换行格式）
        String code = "sub main() {\n" +
                "    UIStats()\n" +
                "    {\n" +
                "        UIStat(\"test\");\n" +
                "    }\n" +
                "}\n";

        TechlandScriptParser parser = createParser(code);
        TechlandScriptParser.FileContext fileCtx = parser.file();

        System.out.println("Parse tree: " + fileCtx.toStringTree(parser));

        assertEquals("应该只有一个 definition", 1, fileCtx.definition().size());

        TechlandScriptParser.DefinitionContext def = fileCtx.definition(0);
        assertNotNull("应该被解析为 subDecl", def.subDecl());

        // 检查 sub 内部的 statements
        TechlandScriptParser.SubDeclContext subDecl = def.subDecl();
        TechlandScriptParser.FunctionBlockContext block = subDecl.functionBlock();
        assertEquals("sub 内部应该有一个 statement", 1, block.statements().size());

        TechlandScriptParser.StatementsContext stmt = block.statements(0);
        assertNotNull("内部应该是 funtionBlockDecl", stmt.funtionBlockDecl());
        assertNull("不应该是 funtionCallDecl", stmt.funtionCallDecl());

        System.out.println("✓ sub 函数内部的嵌套函数块解析正确");
    }

    @Test
    public void testFunctionCallWithoutSemicolon() {
        // 测试没有分号的函数调用（后面跟着另一个语句）
        String code = "sub main() {\n" +
                "    Call1()\n" +
                "    Call2();\n" +
                "}\n";

        TechlandScriptParser parser = createParser(code);
        TechlandScriptParser.FileContext fileCtx = parser.file();

        System.out.println("Parse tree: " + fileCtx.toStringTree(parser));

        TechlandScriptParser.SubDeclContext subDecl = fileCtx.definition(0).subDecl();
        TechlandScriptParser.FunctionBlockContext block = subDecl.functionBlock();

        assertEquals("sub 内部应该有两个 statements", 2, block.statements().size());
        assertNotNull("第一个应该是 funtionCallDecl", block.statements(0).funtionCallDecl());
        assertNotNull("第二个应该是 funtionCallDecl", block.statements(1).funtionCallDecl());

        System.out.println("✓ 没有分号的连续函数调用解析正确");
    }

    @Test
    public void testMethodReferenceCall() {
        // 测试方法引用调用 (::)
        String code = "EDiscoverableType::HUB();\n";

        TechlandScriptParser parser = createParser(code);
        TechlandScriptParser.FileContext fileCtx = parser.file();

        System.out.println("Parse tree: " + fileCtx.toStringTree(parser));

        assertEquals("应该只有一个 definition", 1, fileCtx.definition().size());
        assertNotNull("应该被解析为 methodReferenceFunCallDecl",
                fileCtx.definition(0).methodReferenceFunCallDecl());

        System.out.println("✓ 方法引用调用解析正确");
    }

    private TechlandScriptParser createParser(String code) {
        var charStream = CharStreams.fromString(code);
        var lexer = new TechlandScriptLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);
        return new TechlandScriptParser(tokenStream);
    }
}
