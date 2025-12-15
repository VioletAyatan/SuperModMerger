package ankol.mod.merger.merger;

import ankol.mod.merger.antlr4.scr.TechlandScriptLexer;
import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Techland脚本解析器 - 负责将脚本文件解析为ANTLR4语法树
 * <p>
 * 功能：
 * 1. 从文件路径读取脚本内容
 * 2. 将脚本文本转换为ANTLR4字符流
 * 3. 使用词法分析器（Lexer）进行词法分析
 * 4. 使用语法分析器（Parser）进行语法分析
 * 5. 生成完整的抽象语法树（AST）
 * 6. 捕获并报告语法错误
 * <p>
 * 支持的语法：TechlandScript.g4 定义的完整语法
 */
public class ScrScriptParser {

    /**
     * 从文件路径解析脚本
     * 执行流程：
     * 1. 使用Files.readString读取文件内容为字符串
     * 2. 将字符串内容传给parseContent方法进行解析
     *
     * @param scriptPath 脚本文件的路径
     * @return 脚本的语法树根节点 FileContext
     * @throws IOException 文件读取失败时抛出
     */
    public static TechlandScriptParser.FileContext parseFile(Path scriptPath) throws IOException {
        // 读取脚本文件的全部内容为字符串
        String content = Files.readString(scriptPath);
        // 将内容交给parseContent方法进行实际的解析
        return parseContent(content);
    }

    /**
     * 从字符串内容解析脚本
     * 执行流程：
     * 1. 将字符串转换为ANTLR4的字符流
     * 2. 创建词法分析器和语法分析器
     * 3. 添加自定义错误监听器用于错误处理
     * 4. 调用file()方法启动语法分析，返回语法树
     *
     * @param content 脚本的文本内容
     * @return 脚本的抽象语法树 (FileContext 根节点)
     */
    public static TechlandScriptParser.FileContext parseContent(String content) {
        // 将字符串转换为ANTLR4字符流
        CharStream input = CharStreams.fromString(content);
        // 创建词法和语法分析器
        TechlandScriptParser parser = getTechlandScriptParser(input);
        // 添加自定义错误监听器用于捕获语法分析错误
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg,
                                    RecognitionException e) {
                // 打印语法错误信息（行号、列号、错误描述）
                System.err.println("Parser Error at line " + line + ":" + charPositionInLine + " - " + msg);
            }
        });

        // 启动语法分析，file() 对应 TechlandScript.g4 中的 "file" 规则
        return parser.file();
    }

    /**
     * 创建并配置词法分析器和语法分析器
     * 执行流程：
     * 1. 创建词法分析器
     * 2. 配置词法分析器的错误监听器
     * 3. 创建词元流
     * 4. 创建语法分析器
     * 5. 配置语法分析器的错误监听器
     *
     * @param input 字符流输入
     * @return 配置好的语法分析器实例
     */
    private static TechlandScriptParser getTechlandScriptParser(CharStream input) {
        // 创建词法分析器，将输入字符流转换为词元序列
        TechlandScriptLexer lexer = new TechlandScriptLexer(input);

        // 移除默认错误监听器，避免冗长的默认错误输出
        lexer.removeErrorListeners();
        // 添加自定义错误监听器用于捕获词法分析错误
        lexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg,
                                    RecognitionException e) {
                // 打印词法错误信息（如未识别的字符）
                System.err.println("Lexer Error at line " + line + ":" + charPositionInLine + " - " + msg);
            }
        });

        // 创建词元流，作为词法分析器和语法分析器的中间层
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        // 创建语法分析器，使用词元流和语法规则生成语法树
        TechlandScriptParser parser = new TechlandScriptParser(tokens);

        // 移除默认错误监听器，避免重复的错误输出
        parser.removeErrorListeners();

        return parser;
    }
}

