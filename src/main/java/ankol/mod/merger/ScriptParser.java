package ankol.mod.merger;

import ankol.mod.merger.antlr4.scr.TechlandScriptLexer;
import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScriptParser {

    public static TechlandScriptParser.FileContext parseFile(Path scriptPath) throws IOException {
        String content = Files.readString(scriptPath);
        return parseContent(content);
    }

    public static TechlandScriptParser.FileContext parseContent(String content) {
        CharStream input = CharStreams.fromString(content);
        TechlandScriptLexer lexer = new TechlandScriptLexer(input);

        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                  int line, int charPositionInLine, String msg,
                                  RecognitionException e) {
                System.err.println("Lexer Error at line " + line + ":" + charPositionInLine + " - " + msg);
            }
        });

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TechlandScriptParser parser = new TechlandScriptParser(tokens);

        parser.removeErrorListeners();
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
}

