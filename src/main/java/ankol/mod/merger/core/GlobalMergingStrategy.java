package ankol.mod.merger.core;

import ankol.mod.merger.tools.ColorPrinter;
import ankol.mod.merger.tools.Localizations;
import lombok.Getter;

/**
 * 全局合并策略
 *
 * @author Ankol
 */
public class GlobalMergingStrategy {
    /**
     * 是否自动合并不冲突的代码行
     */
    @Getter
    private static boolean autoMergingCodeLine = true;
    /**
     * 是否自动修正错误的文件路径
     */
    @Getter
    private static boolean autoFixPath = true;

    /**
     * 询问用户是否智能合并代码项
     */
    public static void askAutoMergingCode() {
        ColorPrinter.print("=".repeat(75));
        ColorPrinter.bold(Localizations.t("GLOBAL_STRATEGY_TITLE"));
        ColorPrinter.success(Localizations.t("GLOBAL_STRATEGY_OPTION_1"));
        ColorPrinter.info(Localizations.t("GLOBAL_STRATEGY_OPTION_2"));
        ColorPrinter.print("=".repeat(75));
        ColorPrinter.info(Localizations.t("CRESOLVER_CHOOSE_PROMPT"));
        while (true) {
            String input = IO.readln();
            if (input.equals("1")) {
                autoMergingCodeLine = true;
                break;
            } else if (input.equals("2")) {
                autoMergingCodeLine = false;
                break;
            } else {
                ColorPrinter.error(Localizations.t("ASSET_INVALID_INPUT_PLEASE_ENTER_NUMBER", "1", "2"));
            }
        }
    }

    /**
     * 询问用户是否自动修正错误的文件路径
     */
    public static void askAutoFixPath() {
        ColorPrinter.print("=".repeat(75));
        ColorPrinter.bold(Localizations.t("GLOBAL_STRATEGY_FIX_PATH_TITLE"));
        ColorPrinter.success(Localizations.t("PATH_CORRECTION_STRATEGY_SMART"));
        ColorPrinter.info(Localizations.t("PATH_CORRECTION_STRATEGY_KEEP"));
        ColorPrinter.print("=".repeat(75));
        ColorPrinter.info(Localizations.t("CRESOLVER_CHOOSE_PROMPT"));
        while (true) {
            String input = IO.readln();
            if (input.equals("1")) {
                autoFixPath = true;
                break;
            } else if (input.equals("2")) {
                autoFixPath = false;
                break;
            } else {
                ColorPrinter.error(Localizations.t("ASSET_INVALID_INPUT_PLEASE_ENTER_NUMBER", "1", "2"));
            }
        }
    }
}
