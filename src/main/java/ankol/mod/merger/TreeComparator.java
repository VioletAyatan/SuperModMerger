package ankol.mod.merger;

import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TreeComparator {

    public static class DiffResult {
        public final ParseTree tree1;
        public final ParseTree tree2;
        public final String ruleName;
        public final String description;

        public DiffResult(ParseTree tree1, ParseTree tree2, String ruleName, String description) {
            this.tree1 = tree1;
            this.tree2 = tree2;
            this.ruleName = ruleName;
            this.description = description;
        }

        @Override
        public String toString() {
            String text1 = tree1 != null ? tree1.getText() : "null";
            String text2 = tree2 != null ? tree2.getText() : "null";
            return "[" + ruleName + "] " + description + "\nTree1: " + text1 + "\nTree2: " + text2;
        }
    }

    public static List<DiffResult> compareFiles(TechlandScriptParser.FileContext file1,
                                                 TechlandScriptParser.FileContext file2) {
        List<DiffResult> diffs = new ArrayList<>();

        List<TechlandScriptParser.DefinitionContext> defs1 = file1.definition();
        List<TechlandScriptParser.DefinitionContext> defs2 = file2.definition();

        Map<String, TechlandScriptParser.DefinitionContext> map1 = indexDefinitions(defs1);
        Map<String, TechlandScriptParser.DefinitionContext> map2 = indexDefinitions(defs2);

        for (String key : map1.keySet()) {
            if (!map2.containsKey(key)) {
                diffs.add(new DiffResult(map1.get(key), null, "definition", "In Mod2: " + key));
            } else if (!treeEquals(map1.get(key), map2.get(key))) {
                diffs.add(new DiffResult(map1.get(key), map2.get(key), "definition", "Different: " + key));
            }
        }

        for (String key : map2.keySet()) {
            if (!map1.containsKey(key)) {
                diffs.add(new DiffResult(null, map2.get(key), "definition", "In Mod2: " + key));
            }
        }

        return diffs;
    }

    private static Map<String, TechlandScriptParser.DefinitionContext> indexDefinitions(
            List<TechlandScriptParser.DefinitionContext> definitions) {
        Map<String, TechlandScriptParser.DefinitionContext> map = new LinkedHashMap<>();

        for (TechlandScriptParser.DefinitionContext def : definitions) {
            String key = extractDefinitionKey(def);
            if (key != null) {
                map.put(key, def);
            }
        }

        return map;
    }

    private static String extractDefinitionKey(TechlandScriptParser.DefinitionContext def) {
        if (def.importDecl() != null) {
            return "import:" + def.importDecl().getText();
        } else if (def.exportDecl() != null) {
            TechlandScriptParser.ExportDeclContext exp = def.exportDecl();
            String id = exp.Id() != null ? exp.Id().getText() : "";
            return "export:" + id;
        } else if (def.subDecl() != null) {
            String id = def.subDecl().Id().getText();
            return "sub:" + id;
        } else if (def.macroDecl() != null) {
            String id = def.macroDecl().MacroId().getText();
            return "macro:" + id;
        } else if (def.variableDecl() != null) {
            String id = def.variableDecl().Id().getText();
            return "var:" + id;
        } else if (def.directiveCall() != null) {
            String id = def.directiveCall().Id().getText();
            return "directive:" + id;
        } else if (def.funtionCallDecl() != null) {
            String id = def.funtionCallDecl().Id().getText();
            return "call:" + id;
        } else if (def.funtionBlockDecl() != null) {
            String id = def.funtionBlockDecl().Id().getText();
            return "block:" + id;
        }

        return "unknown:" + def.getText();
    }

    public static boolean treeEquals(ParseTree tree1, ParseTree tree2) {
        if (tree1 == null && tree2 == null) return true;
        if (tree1 == null || tree2 == null) return false;

        String text1 = tree1.getText().trim();
        String text2 = tree2.getText().trim();

        return text1.equals(text2);
    }

    public static String getTreeText(ParseTree tree) {
        if (tree == null) return "";
        return tree.getText();
    }

    public static int getLineNumber(ParseTree tree) {
        if (tree instanceof RuleContext) {
            RuleContext ctx = (RuleContext) tree;
            if (ctx.getSourceInterval().a >= 0) {
                return ctx.getSourceInterval().a;
            }
        }
        return -1;
    }
}

