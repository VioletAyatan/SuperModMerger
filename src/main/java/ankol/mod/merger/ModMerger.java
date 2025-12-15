package ankol.mod.merger;

import ankol.mod.merger.ConflictResolver.MergeChoice;
import ankol.mod.merger.ConflictResolver.MergeDecision;
import ankol.mod.merger.TreeComparator.DiffResult;
import ankol.mod.merger.antlr4.scr.TechlandScriptParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ModMerger {

    private final Path mod1Dir;
    private final Path mod2Dir;
    private final Path outputDir;
    private final boolean interactive;
    private final MergeChoice defaultStrategy;

    public ModMerger(Path mod1Dir, Path mod2Dir, Path outputDir,
                     boolean interactive, MergeChoice defaultStrategy) {
        this.mod1Dir = mod1Dir;
        this.mod2Dir = mod2Dir;
        this.outputDir = outputDir;
        this.interactive = interactive;
        this.defaultStrategy = defaultStrategy;
    }

    public void merge() throws IOException {
        System.out.println("====== Techland Mod Merger ======");
        System.out.println("Mod1: " + mod1Dir);
        System.out.println("Mod2: " + mod2Dir);
        System.out.println("Output: " + outputDir);
        System.out.println("Mode: " + (interactive ? "Interactive" : "Auto (" + defaultStrategy.getDescription() + ")"));
        System.out.println();

        Files.createDirectories(outputDir);

        List<Path> scripts1 = findScriptFiles(mod1Dir);
        List<Path> scripts2 = findScriptFiles(mod2Dir);

        System.out.println("Found " + scripts1.size() + " scripts in Mod1");
        System.out.println("Found " + scripts2.size() + " scripts in Mod2");
        System.out.println();

        Map<String, Path> map1 = buildFileMap(mod1Dir, scripts1);
        Map<String, Path> map2 = buildFileMap(mod2Dir, scripts2);

        int mergedCount = 0;
        int conflictCount = 0;
        int addedCount = 0;

        Set<String> processedFiles = new HashSet<>();
        for (String filename : map1.keySet()) {
            processedFiles.add(filename);

            if (map2.containsKey(filename)) {
                System.out.println("Merging: " + filename);

                try {
                    MergeResult result = mergeScriptFiles(map1.get(filename), map2.get(filename));

                    Path outputPath = outputDir.resolve(filename);
                    Files.createDirectories(outputPath.getParent());
                    Files.writeString(outputPath, result.mergedContent);

                    if (result.hasConflicts) {
                        conflictCount++;
                        System.out.println("  ! " + result.conflicts.size() + " conflicts");
                    } else {
                        mergedCount++;
                        System.out.println("  OK");
                    }

                } catch (Exception e) {
                    System.err.println("  ERROR: " + e.getMessage());
                }
            } else {
                System.out.println("Copying: " + filename);
                Path outputPath = outputDir.resolve(filename);
                Files.createDirectories(outputPath.getParent());
                Files.copy(map1.get(filename), outputPath, StandardCopyOption.REPLACE_EXISTING);
                mergedCount++;
            }
        }

        for (String filename : map2.keySet()) {
            if (!processedFiles.contains(filename)) {
                System.out.println("Copying: " + filename);
                Path outputPath = outputDir.resolve(filename);
                Files.createDirectories(outputPath.getParent());
                Files.copy(map2.get(filename), outputPath, StandardCopyOption.REPLACE_EXISTING);
                addedCount++;
            }
        }

        System.out.println("\n====== Merge Complete ======");
        System.out.println("Merged: " + mergedCount);
        System.out.println("With conflicts: " + conflictCount);
        System.out.println("New files: " + addedCount);
        System.out.println("Output: " + outputDir);
    }

    private MergeResult mergeScriptFiles(Path script1, Path script2) throws IOException {
        TechlandScriptParser.FileContext file1 = ScriptParser.parseFile(script1);
        TechlandScriptParser.FileContext file2 = ScriptParser.parseFile(script2);

        List<DiffResult> diffs = TreeComparator.compareFiles(file1, file2);

        MergeResult result = new MergeResult();

        if (diffs.isEmpty()) {
            result.mergedContent = Files.readString(script1);
            result.hasConflicts = false;
            return result;
        }

        List<MergeDecision> decisions;
        if (interactive) {
            decisions = ConflictResolver.resolveConflicts(diffs);
        } else {
            decisions = ConflictResolver.autoResolve(diffs, defaultStrategy);
        }

        result = buildMergedContent(script1, script2, file1, file2, diffs, decisions);

        return result;
    }

    private MergeResult buildMergedContent(Path script1Path, Path script2Path,
                                          TechlandScriptParser.FileContext file1,
                                          TechlandScriptParser.FileContext file2,
                                          List<DiffResult> diffs,
                                          List<MergeDecision> decisions) throws IOException {
        MergeResult result = new MergeResult();

        String content1 = Files.readString(script1Path);
        String content2 = Files.readString(script2Path);

        StringBuilder merged = new StringBuilder(content1);

        for (MergeDecision decision : decisions) {
            DiffResult diff = decision.diff;

            switch (decision.choice) {
                case KEEP_MOD1:
                    break;

                case KEEP_MOD2:
                    if (diff.tree1 != null && diff.tree2 != null) {
                        String text1 = diff.tree1.getText();
                        String text2 = diff.tree2.getText();
                        String contentToMerge = merged.toString();
                        int index = contentToMerge.indexOf(text1);
                        if (index >= 0) {
                            merged.replace(index, index + text1.length(), text2);
                        }
                    } else if (diff.tree2 != null) {
                        merged.append("\n\n").append(diff.tree2.getText());
                    }
                    break;

                case KEEP_BOTH:
                    if (diff.tree2 != null) {
                        merged.append("\n\n// Merged from Mod2\n");
                        merged.append(diff.tree2.getText());
                    }
                    break;

                case MANUAL:
                    if (decision.customContent != null && !decision.customContent.isEmpty()) {
                        if (diff.tree1 != null) {
                            String text1 = diff.tree1.getText();
                            String contentToMerge = merged.toString();
                            int index = contentToMerge.indexOf(text1);
                            if (index >= 0) {
                                merged.replace(index, index + text1.length(), decision.customContent);
                            }
                        }
                    }
                    break;

                case SKIP:
                    break;
            }

            if (decision.choice != MergeChoice.SKIP) {
                result.conflicts.add(diff);
            }
        }

        result.mergedContent = merged.toString();
        result.hasConflicts = !result.conflicts.isEmpty();

        return result;
    }

    private List<Path> findScriptFiles(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return new ArrayList<>();
        }

        List<Path> scripts = new ArrayList<>();
        Files.walk(directory)
            .filter(Files::isRegularFile)
            .filter(p -> {
                String name = p.getFileName().toString().toLowerCase();
                return name.endsWith(".scr") || name.endsWith(".txt");
            })
            .forEach(scripts::add);
        return scripts;
    }

    private Map<String, Path> buildFileMap(Path baseDir, List<Path> scripts) {
        Map<String, Path> map = new LinkedHashMap<>();

        for (Path script : scripts) {
            String relativePath = baseDir.relativize(script).toString();
            map.put(relativePath, script);
        }

        return map;
    }

    public static class MergeResult {
        public String mergedContent;
        public boolean hasConflicts;
        public List<DiffResult> conflicts = new ArrayList<>();
    }
}

