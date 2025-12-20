package ankol.mod.merger.merger.scr;

import ankol.mod.merger.antlr4.scr.TechlandScriptLexer;
import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import ankol.mod.merger.merger.scr.node.ScrContainerNode;
import ankol.mod.merger.tools.Tools;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestMain {

    static void main() throws IOException {
        String userDir = Tools.getUserDir();
//        String fileName = "damagedefinitions.scr";
//        String fileName2 = "damagedefinitions2.scr";
        String fileName = "player_variables.scr";
        String fileName2 = "player_variables2.scr";
        String baseContent = Files.readString(Path.of(userDir + "/examples/" + fileName));
        String modContent = Files.readString(Path.of(userDir + "/examples/" + fileName2));

        ScrContainerNode parse = parse(baseContent);
        ScrContainerNode parse1 = parse(modContent);

//        SourcePatchMerger merger = new SourcePatchMerger("data3.pak", "data4.pak", "player_variables.scr");
//        String merged = merger.merge(baseContent, parse, parse1);
//        Files.writeString(Path.of("D:\\Projects\\ModMergerTool\\examples\\merged_" + fileName), merged);
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
