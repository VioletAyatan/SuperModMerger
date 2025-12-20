package ankol.mod.merger.merger.scr.news;

import ankol.mod.merger.antlr4.scr.TechlandScriptLexer;
import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import ankol.mod.merger.merger.scr.news.node.ScrContainerNode;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestMain {

    static void main() throws IOException {
        String baseContent = Files.readString(Path.of("D:\\Projects\\ModMergerTool\\examples\\jump_parameters.scr"));
        String modContent = Files.readString(Path.of("D:\\Projects\\ModMergerTool\\examples\\jump_parameters2.scr"));

        ScrContainerNode parse = parse(baseContent);
        ScrContainerNode parse1 = parse(modContent);
        System.out.println("parse = " + parse);
        System.out.println("parse1 = " + parse1);

        SourcePatchMerger merger = new SourcePatchMerger("jump_parameters.scr");
        String merged = merger.merge(baseContent, parse, parse1);

        System.out.println("merged = " + merged);

        Files.writeString(Path.of("D:\\Projects\\ModMergerTool\\examples\\merged_jump_parameters.scr"), merged);
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
