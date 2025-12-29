package ankol.mod.merger.core;

import ankol.mod.merger.merger.ConflictRecord;
import ankol.mod.merger.tools.ColorPrinter;
import ankol.mod.merger.tools.Localizations;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Scanner;

/**
 * 冲突解决器
 *
 * @author Ankol
 */
@Slf4j
public class ConflictResolver {
    /**
     * 交互式解决冲突
     *
     * @param conflicts 冲突项目
     */
    public static void resolveConflict(List<ConflictRecord> conflicts) {
        //筛选出智能合并的节点
        List<ConflictRecord> automaticMerge = conflicts.stream()
                .filter(conflict -> conflict.getUserChoice() != null)
                .toList();
        if (!automaticMerge.isEmpty()) {
            for (ConflictRecord item : automaticMerge) {
                log.info("AutoMerging Code-line: {} -> {}", item.getBaseNode().getSourceText(), item.getModNode().getSourceText());
            }
            ColorPrinter.success(Localizations.t("SCR_MERGER_AUTO_MERGE_COUNT", automaticMerge.size()));
            conflicts.removeAll(automaticMerge); //暂时移除，主要是为了不出现冲突提示.
        }
        //对于真正的冲突项，提示用户选择使用哪一个版本解决
        if (!conflicts.isEmpty()) {
            Scanner scanner = new Scanner(System.in);
            System.out.println(); //换行
            ColorPrinter.warning(Localizations.t("SCR_MERGER_CONFLICT_DETECTED", conflicts.size()));
            int chose = 0;
            for (int i = 0; i < conflicts.size(); i++) {
                ConflictRecord record = conflicts.get(i);
                if (chose == 3) {
                    record.setUserChoice(1); //3表示用户全部选择baseMod的配置来处理
                } else if (chose == 4) {
                    record.setUserChoice(2); //4表示用户全部选择mergeMod的配置来处理
                } else {
                    String baseNodeSource = record.getBaseNode().getSourceText().trim();
                    String modNodeSource = record.getModNode().getSourceText().trim();
                    //打印代码提示框
                    ColorPrinter.info("=".repeat(75));
                    ColorPrinter.info(Localizations.t("SCR_MERGER_FILE_INFO", i + 1, conflicts.size(), record.getFileName()));
                    ColorPrinter.warning(Localizations.t("SCR_MERGER_MOD_VERSION_1", record.getBaseModName()));
                    ColorPrinter.bold(Localizations.t("SCR_MERGER_LINE_INFO", record.getBaseNode().getLineNumber(), baseNodeSource));
                    ColorPrinter.warning(Localizations.t("SCR_MERGER_MOD_VERSION_2", record.getMergeModName()));
                    ColorPrinter.bold(Localizations.t("SCR_MERGER_LINE_INFO", record.getModNode().getLineNumber(), modNodeSource));
                    ColorPrinter.info("=".repeat(75));
                    //选择对话框
                    ColorPrinter.info(Localizations.t("SCR_MERGER_CHOOSE_PROMPT"));
                    ColorPrinter.info(Localizations.t("SCR_MERGER_USE_OPTION_1", baseNodeSource));
                    ColorPrinter.info(Localizations.t("SCR_MERGER_USE_OPTION_2", modNodeSource));
                    ColorPrinter.info(Localizations.t("SCR_MERGER_USE_ALL_FROM_MOD_1", record.getBaseModName()));
                    ColorPrinter.info(Localizations.t("SCR_MERGER_USE_ALL_FROM_MOD_2", record.getMergeModName()));

                    while (true) {
                        String input = scanner.nextLine();
                        if (input.equals("1") || input.equals("2")) {
                            record.setUserChoice(Integer.parseInt(input));
                            break;
                        }
                        if (input.equals("3") || input.equals("4")) {
                            chose = Integer.parseInt(input);
                            record.setUserChoice(chose == 3 ? 1 : 2);
                            break;
                        }
                        ColorPrinter.warning(Localizations.t("SCR_MERGER_INVALID_INPUT"));
                    }
                }
            }
            ColorPrinter.success(Localizations.t("SCR_MERGER_CONFLICT_RESOLVED"));
        }
        //最后把自动合并的节点加回去，让后续处理合并的逻辑使用同一个容器
        conflicts.addAll(automaticMerge);
    }
}
