package ankol.mod.merger.tools;

import lombok.extern.slf4j.Slf4j;

/**
 * 控制台彩色打印工具类
 * <p>
 * 支持ANSI彩色输出，仅在Windows10+和Unix-like系统中有效，使用示例：
 * <ui>
 * <li>ColorPrinter.info("This is info message");</li>
 * <li>ColorPrinter.success("Operation successful");</li>
 * <li>ColorPrinter.warning("Warning message");</li>
 * <li>ColorPrinter.error("Error occurred");</li>
 * </ui>
 *
 * @author Ankol
 */
@Slf4j
public class ColorPrinter {

    // ANSI 颜色代码
    private static final String RESET = "\033[0m";
    private static final String BLACK = "\033[30m";
    private static final String RED = "\033[31m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String BLUE = "\033[34m";
    private static final String MAGENTA = "\033[35m";
    private static final String CYAN = "\033[36m";
    private static final String WHITE = "\033[37m";

    // 高亮颜色
    private static final String BRIGHT_RED = "\033[91m";
    private static final String BRIGHT_GREEN = "\033[92m";
    private static final String BRIGHT_YELLOW = "\033[93m";
    private static final String BRIGHT_BLUE = "\033[94m";
    private static final String BRIGHT_MAGENTA = "\033[95m";
    private static final String BRIGHT_CYAN = "\033[96m";
    private static final String BRIGHT_WHITE = "\033[97m";

    // 背景颜色
    private static final String BG_RED = "\033[41m";
    private static final String BG_GREEN = "\033[42m";
    private static final String BG_YELLOW = "\033[43m";
    private static final String BG_BLUE = "\033[44m";

    // 样式
    private static final String BOLD = "\033[1m";
    private static final String DIM = "\033[2m";
    private static final String ITALIC = "\033[3m";
    private static final String UNDERLINE = "\033[4m";

    // 检查是否支持彩色输出（Windows 10+ 或 Unix-like 系统）
    private static final boolean SUPPORTS_COLOR = supportsColor();

    /**
     * 检查系统是否支持 ANSI 彩色输出
     */
    private static boolean supportsColor() {
        // Windows 10+ 支持 ANSI，通过检查 OS 和版本
        String os = System.getProperty("os.name", "").toLowerCase();
        String osVersion = System.getProperty("os.version", "");

        // Unix-like 系统（Linux, macOS 等）
        if (os.contains("linux") || os.contains("mac") || os.contains("unix")) {
            return true;
        }

        // Windows 10+ 支持 ANSI
        if (os.contains("windows")) {
            try {
                // Windows 10 及更高版本
                String[] versionParts = osVersion.split("\\.");
                if (versionParts.length > 0) {
                    int major = Integer.parseInt(versionParts[0]);
                    // Windows 10 = 10.0，Windows 11 = 10.0 但 build > 21000
                    return major >= 10;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    /**
     * 应用颜色（如果不支持，返回原文本）
     */
    private static String applyColor(String text, String color) {
        if (!SUPPORTS_COLOR) {
            return text;
        }
        return color + text + RESET;
    }

    /**
     * 打印 INFO 级别日志（蓝色）
     */
    public static void info(String message) {
        System.out.println(applyColor(message, BRIGHT_BLUE));
        log.debug(message);
    }

    /**
     * 打印 INFO 级别日志（蓝色），带格式化参数
     */
    public static void info(String format, Object... args) {
        info(Tools.format(format, args));
    }

    /**
     * 打印成功消息（绿色）
     */
    public static void success(String message) {
        System.out.println(applyColor(message, BRIGHT_GREEN));
        log.debug(message);
    }

    /**
     * 打印成功消息（绿色），带格式化参数
     */
    public static void success(String format, Object... args) {
        success(Tools.format(format, args));
    }

    /**
     * 打印警告消息（黄色）
     */
    public static void warning(String message) {
        System.out.println(applyColor(message, BRIGHT_YELLOW));
        log.debug(message);
    }

    /**
     * 打印警告消息（黄色），带格式化参数
     */
    public static void warning(String format, Object... args) {
        warning(Tools.format(format, args));
    }

    /**
     * 打印错误消息（红色）
     */
    public static void error(String message) {
        System.err.println(applyColor(message, BRIGHT_RED));
        log.error(message);
    }

    /**
     * 打印错误消息（红色），带异常堆栈跟踪
     */
    public static void error(String message, Throwable e) {
        System.err.println(applyColor(message, BRIGHT_RED));
        log.debug(message, e);
    }

    /**
     * 打印错误消息（红色），带格式化参数
     */
    public static void error(String format, Object... args) {
        error(Tools.format(format, args));
    }

    /**
     * 打印调试消息（青色）
     */
    public static void debug(String message) {
        System.out.println(applyColor(message, BRIGHT_CYAN));
        log.debug(message);
    }

    /**
     * 打印调试消息（青色），带格式化参数
     */
    public static void debug(String format, Object... args) {
        debug(Tools.format(format, args));
    }

    /**
     * 打印普通消息（白色）
     */
    public static void print(String message) {
        System.out.println(applyColor(message, WHITE));
        log.debug(message);
    }

    /**
     * 打印普通消息（白色），带格式化参数
     */
    public static void print(String format, Object... args) {
        print(Tools.format(format, args));
    }

    /**
     * 打印加粗消息（白色加粗）
     */
    public static void bold(String message) {
        System.out.println(applyColor(BOLD + message, RESET));
        log.debug(message);
    }

    /**
     * 打印加粗消息（白色加粗），带格式化参数
     */
    public static void bold(String format, Object... args) {
        bold(Tools.format(format, args));
    }

    /**
     * 打印强调消息（洋红色）
     */
    public static void highlight(String message) {
        System.out.println(applyColor(message, BRIGHT_MAGENTA));
    }

    /**
     * 打印强调消息（洋红色），带格式化参数
     */
    public static void highlight(String format, Object... args) {
        System.out.println(applyColor(Tools.format(format, args), BRIGHT_MAGENTA));
    }

    /**
     * 自定义颜色打印
     *
     * @param message   消息内容
     * @param colorCode ANSI 颜色代码（如 ColorPrinter.RED）
     */
    public static void printWithColor(String message, String colorCode) {
        System.out.println(applyColor(message, colorCode));
    }

    /**
     * 获取彩色文本（不直接打印）
     */
    public static String getColoredText(String text, String colorCode) {
        return applyColor(text, colorCode);
    }

    /**
     * 检查是否支持彩色输出
     */
    public static boolean isColorSupported() {
        return SUPPORTS_COLOR;
    }

    // 公开常用颜色常量
    public static final String RED_CODE = RED;
    public static final String GREEN_CODE = GREEN;
    public static final String YELLOW_CODE = YELLOW;
    public static final String BLUE_CODE = BLUE;
    public static final String CYAN_CODE = CYAN;
    public static final String MAGENTA_CODE = MAGENTA;
    public static final String BRIGHT_RED_CODE = BRIGHT_RED;
    public static final String BRIGHT_GREEN_CODE = BRIGHT_GREEN;
    public static final String BRIGHT_YELLOW_CODE = BRIGHT_YELLOW;
    public static final String BRIGHT_BLUE_CODE = BRIGHT_BLUE;
    public static final String BRIGHT_CYAN_CODE = BRIGHT_CYAN;
    public static final String BRIGHT_MAGENTA_CODE = BRIGHT_MAGENTA;
}

