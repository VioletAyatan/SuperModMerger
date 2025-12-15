package ankol.mod.merger.merger.scr;

import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.merger.scr.ScrConflictResolver.MergeDecision;
import ankol.mod.merger.merger.scr.ScrTreeComparator.DiffResult;
import ankol.mod.merger.core.IFileMerger;
import ankol.mod.merger.tools.FileTree;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScrFileMerger implements IFileMerger {

    @Override
    public MergeResult merge(FileTree script1, FileTree script2) throws IOException {
        ScrScriptParser parser = new ScrScriptParser();
        ScrScriptParser.ParsedScript p1 = parser.parseFileWithTokens(Path.of(script1.getFullPathName()));
        ScrScriptParser.ParsedScript p2 = parser.parseFileWithTokens(Path.of(script2.getFullPathName()));

        TechlandScriptParser.FileContext fileTree1 = p1.file();
        TechlandScriptParser.FileContext fileTree2 = p2.file();

        List<DiffResult> diffs = ScrTreeComparator.compareFiles(fileTree1, fileTree2);

        if (diffs.isEmpty()) {
            return new MergeResult(Files.readString(Path.of(script1.getFullPathName())), false);
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("⚠️ CONFLICTS DETECTED IN [" + script1.getFileName() + "] - User Interaction Required");
        System.out.println("=".repeat(80));
        List<MergeDecision> decisions = ScrConflictResolver.resolveConflicts(diffs, script1, script2);

        MergeResult result = buildMergedContent(Path.of(script1.getFullPathName()), decisions, p1, p2);
        // attach conflict diffs for engine-level reporting
        result.conflicts.addAll(diffs);
        return result;
    }

    private record Replacement(int start, int end, String text) {}

    private MergeResult buildMergedContent(Path script1Path, List<MergeDecision> decisions, ScrScriptParser.ParsedScript p1, ScrScriptParser.ParsedScript p2) throws IOException {
        String content1 = Files.readString(script1Path);
        StringBuilder mergedContent = new StringBuilder(content1);
        List<Replacement> replacements = new ArrayList<>();

        for (MergeDecision decision : decisions) {
            DiffResult diff = decision.diff();
            ParseTree tree1 = diff.tree1;
            ParseTree tree2 = diff.tree2;

            if (decision.choice() == ScrConflictResolver.MergeChoice.KEEP_MOD2) {
                if (tree1 != null && tree2 == null) { // Removed in Mod2
                    Interval interval = tree1.getSourceInterval();
                    int charStart = tokenStartChar(p1.tokens(), interval.a);
                    int charEnd = tokenEndChar(p1.tokens(), interval.b);
                    replacements.add(new Replacement(charStart, charEnd, ""));
                } else if (tree1 == null && tree2 != null) { // Added in Mod2
                    // Heuristic: find parent and insert before its closing brace (fallback behavior)
                    int insertPos = findInsertionPoint(mergedContent, diff.lineNumber2, tree2);
                    replacements.add(new Replacement(insertPos, insertPos, "\t" + tree2.getText() + "\n"));
                } else if (tree1 != null && tree2 != null) { // Modified
                    Interval interval = tree1.getSourceInterval();
                    int charStart = tokenStartChar(p1.tokens(), interval.a);
                    int charEnd = tokenEndChar(p1.tokens(), interval.b);
                    replacements.add(new Replacement(charStart, charEnd, tree2.getText()));
                }
            }
        }

        replacements.sort(Comparator.comparingInt(Replacement::start).reversed());

        for (Replacement rep : replacements) {
            mergedContent.replace(rep.start, rep.end, rep.text);
        }

        MergeResult mergeResult = new MergeResult(mergedContent.toString(), !decisions.isEmpty());
        return mergeResult;
    }

    private int tokenStartChar(TokenStream tokens, int tokenIndex) {
        if (tokens == null) return 0;
        if (tokenIndex < 0 || tokenIndex >= ((org.antlr.v4.runtime.CommonTokenStream) tokens).getTokens().size()) return 0;
        Token t = ((org.antlr.v4.runtime.CommonTokenStream) tokens).getTokens().get(tokenIndex);
        return Math.max(0, t.getStartIndex());
    }

    private int tokenEndChar(TokenStream tokens, int tokenIndex) {
        if (tokens == null) return 0;
        if (tokenIndex < 0 || tokenIndex >= ((org.antlr.v4.runtime.CommonTokenStream) tokens).getTokens().size()) return 0;
        Token t = ((org.antlr.v4.runtime.CommonTokenStream) tokens).getTokens().get(tokenIndex);
        return Math.max(0, t.getStopIndex() + 1);
    }

    private int findInsertionPoint(StringBuilder content, int targetLine, ParseTree nodeToInsert) {
        // Default to end of file if no better place is found
        int insertionPoint = content.length() -1;

        // Find the parent block's closing brace '}'
        String[] lines = content.toString().split("\n");
        int searchStartLine = targetLine > 0 ? targetLine - 1 : 0;

        // This is a simplified heuristic. A truly robust implementation would
        // navigate the parse tree to find the parent block.
        // For now, we search for the next closing brace at the same or higher indentation level.
        int braceLevel = 0;
        boolean inBlock = false;
        int lineOffset = 0;
        for(int i=0; i<lines.length; i++) {
            if(i >= searchStartLine) {
                if(lines[i].contains("{")) {
                    if(!inBlock) inBlock = true;
                    braceLevel++;
                }
                if(lines[i].contains("}")) {
                    braceLevel--;
                    if(inBlock && braceLevel == 0) {
                        insertionPoint = lineOffset + lines[i].indexOf("}");
                        break;
                    }
                }
            }
            lineOffset += lines[i].length() + 1;
        }
        return insertionPoint;
    }
}
