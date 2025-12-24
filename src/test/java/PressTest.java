import ankol.mod.merger.antlr.xml.TechlandXMLLexer;
import ankol.mod.merger.antlr.xml.TechlandXMLParser;
import ankol.mod.merger.merger.xml.TechlandXmlFileVisitor;
import ankol.mod.merger.tools.Tools;
import cn.hutool.core.io.FileUtil;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class PressTest {

    @Test
    public void visitorXmlTest() throws IOException {
        TechlandXMLParser parser = getParser(FileUtil.getInputStream(Tools.getUserDir() + "/examples/common_skills.xml"));
        TechlandXMLParser.DocumentContext document = parser.document();
        TechlandXmlFileVisitor visitor = new TechlandXmlFileVisitor();
        visitor.visit(document);
    }


    private TechlandXMLParser getParser(InputStream code) throws IOException {
        var input = CharStreams.fromStream(code);
        var lexer = new TechlandXMLLexer(input);
        var tokens = new CommonTokenStream(lexer);
        return new TechlandXMLParser(tokens);
    }
}
