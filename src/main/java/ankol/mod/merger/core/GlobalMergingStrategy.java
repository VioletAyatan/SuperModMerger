package ankol.mod.merger.core;

import ankol.mod.merger.tools.ColorPrinter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * 全局合并策略
 *
 * @author Ankol
 */
@Slf4j
public class GlobalMergingStrategy {
    private static final Scanner SCANNER = new Scanner(System.in);
    /**
     * 是否自动合并不冲突的代码行
     */
    private static boolean autoMergingCodeLine = false;
    /**
     * 是否自动修正错误的文件路径
     */
    private static boolean autoFixPath = true;

    public static void askAutoMergingCode() {
        ColorPrinter.print("=".repeat(75));
        ColorPrinter.info("请选择冲突合并策略：");
        ColorPrinter.bold("1、智能合并冲突项（推荐）");
        ColorPrinter.bold("2、手动合并所有检测到的冲突项");
        ColorPrinter.print("=".repeat(75));
        while (true) {
            String input = SCANNER.next();
        }
    }
}
