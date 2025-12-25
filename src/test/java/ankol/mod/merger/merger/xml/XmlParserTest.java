package ankol.mod.merger.merger.xml;

import ankol.mod.merger.antlr.xml.TechlandXMLLexer;
import ankol.mod.merger.antlr.xml.TechlandXMLParser;
import ankol.mod.merger.merger.xml.node.XmlContainerNode;
import ankol.mod.merger.merger.xml.node.XmlNode;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * XML Parser Test - 测试XML解析功能
 */
public class XmlParserTest {
    public static void main(String[] args) throws Exception {
        System.out.println("=== XML Parser Test ===\n");

        // 读取测试文件
        Path testBasePath = Path.of("examples/test_base.xml");
        if (!Files.exists(testBasePath)) {
            System.err.println("Test file not found: " + testBasePath);
            return;
        }

        String baseContent = Files.readString(testBasePath);
        System.out.println("Base XML Content:");
        System.out.println(baseContent);
        System.out.println("\n");

        // 测试解析
        System.out.println("Parsing base XML...");
        XmlContainerNode baseRoot = parseXml(baseContent);
        System.out.println("✓ Base XML parsed successfully\n");

        // 打印AST结构
        System.out.println("Base XML AST Structure:");
        printAst(baseRoot, 0);

        System.out.println("\n=== Test Completed Successfully ===");
    }

    private static XmlContainerNode parseXml(String content) throws Exception {
        try {
            CharStream input = CharStreams.fromString(content);
            TechlandXMLLexer lexer = new TechlandXMLLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            TechlandXMLParser parser = new TechlandXMLParser(tokens);
            TechlandXmlFileVisitor visitor = new TechlandXmlFileVisitor();

            XmlNode root = visitor.visitDocument(parser.document());
            if (!(root instanceof XmlContainerNode)) {
                throw new IllegalStateException("Root node is not a container");
            }
            return (XmlContainerNode) root;
        } catch (Exception e) {
            System.err.println("Error parsing XML: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private static void printAst(XmlNode node, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + "├─ " + node.getSignature()
            + " (Line: " + node.getLine() + ", Tokens: "
            + node.getStartTokenIndex() + "-" + node.getStopTokenIndex() + ")");

        if (node instanceof XmlContainerNode container) {
            int childCount = container.getChildren().size();
            int i = 0;
            for (XmlNode child : container.getChildren().values()) {
                i++;
                if (i == childCount) {
                    System.out.print(indent + "└─ ");
                    printAstChild(child, depth + 1, true);
                } else {
                    System.out.print(indent + "├─ ");
                    printAstChild(child, depth + 1, false);
                }
            }
        }
    }

    private static void printAstChild(XmlNode node, int depth, boolean isLast) {
        String prefix = isLast ? "└─ " : "├─ ";
        System.out.println(prefix + node.getSignature()
            + " (Line: " + node.getLine() + ", Tokens: "
            + node.getStartTokenIndex() + "-" + node.getStopTokenIndex() + ")");

        if (node instanceof XmlContainerNode container) {
            int childCount = container.getChildren().size();
            int i = 0;
            String indent = (isLast ? "   " : "│  ");
            for (XmlNode child : container.getChildren().values()) {
                i++;
                if (i == childCount) {
                    System.out.print(indent + "└─ ");
                    printAstChild(child, depth + 1, true);
                } else {
                    System.out.print(indent + "├─ ");
                    printAstChild(child, depth + 1, false);
                }
            }
        }
    }
}

