package ankol.mod.merger.merger.scr;

import ankol.mod.merger.antlr4.scr.TechlandScriptLexer;
import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import cn.hutool.core.io.FileUtil;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;

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
    private TokenStream tokenStream;
    private TechlandScriptParser techlandScriptParser;
    private TechlandScriptLexer techlandScriptLexer;

    /**
     * Parsed script container holding AST, tokens and original source text
     */
    public record ParsedScript(TechlandScriptParser.FileContext file, CommonTokenStream tokens, String originalText) {}

    /**
     * 从文件路径解析脚本
     * 执行流程：
     * 1. 使用Files.readString读取文件内容为字符串
     * 2. 将字符串内容传给parseContent方法进行解析
     *
     * @param scriptPath 脚本文件的路径
     * @return 脚本的语法树根节点 FileContext
     */
    public TechlandScriptParser.FileContext parseFile(Path scriptPath) {
        try {
            return parseContent(FileUtil.getInputStream(scriptPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse file and return parsed script with token stream and original text.
     */
    public ParsedScript parseFileWithTokens(Path scriptPath) throws IOException {
        String text = Files.readString(scriptPath, StandardCharsets.UTF_8);
        TechlandScriptParser parser = getTechlandScriptParser(CharStreams.fromString(text));
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg,
                                    RecognitionException e) {
                System.err.println("Parser Error at line " + line + ":" + charPositionInLine + " - " + msg);
            }
        });
        TechlandScriptParser.FileContext file = parser.file();
        CommonTokenStream tokens = (CommonTokenStream) this.getTokenStream();
        return new ParsedScript(file, tokens, text);
    }

    /**
     * 从字符串内容解析脚本
     * 执行流程：
     * 1. 将字符串转换为ANTLR4的字符流
     * 2. 创建词法分析器和语法分析器
     * 3. 添加自定义错误监听器用于错误处理
     * 4. 调用file()方法启动语法分析，返回语法树
     *
     * @param inputStream 文件输入流
     * @return 脚本的抽象语法树 (FileContext 根节点)
     */
    public TechlandScriptParser.FileContext parseContent(InputStream inputStream) throws IOException {
        TechlandScriptParser parser = getTechlandScriptParser(CharStreams.fromStream(inputStream, StandardCharsets.UTF_8));
        //解析器添加错误监听器，用户捕获解析过程中出现的错误
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg,
                                    RecognitionException e) {
                System.err.println("Parser Error at line " + line + ":" + charPositionInLine + " - " + msg);
            }
        });
        return parser.file();
    }

    /**
     * 获取Techland语法树解析器
     *
     * @param input 输入字符流
     * @return {@link TechlandScriptParser}
     */
    private TechlandScriptParser getTechlandScriptParser(CharStream input) {
        // 创建词法分析器，将输入字符流转换为词元序列
        this.techlandScriptLexer = new TechlandScriptLexer(input);
        this.techlandScriptLexer.removeErrorListeners();
        // 添加自定义错误监听器用于捕获词法分析错误
        this.techlandScriptLexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg,
                                    RecognitionException e) {
                // 打印词法错误信息（如未识别的字符）
                System.err.println("Lexer Error at line " + line + ":" + charPositionInLine + " - " + msg);
            }
        });
        this.tokenStream = new CommonTokenStream(techlandScriptLexer);
        this.techlandScriptParser = new TechlandScriptParser(tokenStream);
        this.techlandScriptParser.removeErrorListeners();
        return techlandScriptParser;
    }

    /**
     * 获取 {@link TokenStream}
     *
     * @return {@link TokenStream}
     */
    public TokenStream getTokenStream() {
        return tokenStream;
    }

    /**
     * 获取 {@link TechlandScriptParser}
     *
     * @return {@link TechlandScriptParser}
     */
    public TechlandScriptParser getTechlandScriptParser() {
        return techlandScriptParser;
    }

    /**
     * 获取 {@link TechlandScriptLexer}
     *
     * @return {@link TechlandScriptLexer}
     */
    public TechlandScriptLexer getTechlandScriptLexer() {
        return techlandScriptLexer;
    }
}
