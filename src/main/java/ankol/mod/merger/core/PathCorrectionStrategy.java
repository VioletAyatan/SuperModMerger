package ankol.mod.merger.core;

import ankol.mod.merger.tools.Localizations;
import lombok.Getter;

/**
 * 路径修正策略 - 定义如何处理文件路径不匹配的情况
 *
 * @author Ankol
 */
public class PathCorrectionStrategy {

    /**
     * 修正策略枚举
     */
    public enum Strategy {
        /**
         * 智能修正：使用基准MOD中的正确路径，忽略待合并MOD中的错误路径
         */
        SMART_CORRECT(1, Localizations.t("PATH_CORRECTION_STRATEGY_SMART")),

        /**
         * 保留原始路径：保留待合并MOD中的原始路径，不进行修正
         */
        KEEP_ORIGINAL(2, Localizations.t("PATH_CORRECTION_STRATEGY_KEEP"));

        @Getter
        private final int code;

        @Getter
        private final String description;

        Strategy(int code, String description) {
            this.code = code;
            this.description = description;
        }
    }

    /**
     * 当前选择的策略
     */
    @Getter
    private Strategy selectedStrategy;

    /**
     * 构造函数 - 初始化为未选择状态
     */
    public PathCorrectionStrategy() {
        this.selectedStrategy = Strategy.SMART_CORRECT; //todo 这里先写死必须进行路径修正，后面再添加保留路径支持
    }

    /**
     * 根据策略码选择策略
     *
     * @param code 策略码（1为智能修正，2为保留原始路径）
     * @return 如果设置成功返回true，否则返回false
     */
    public boolean selectByCode(int code) {
        for (Strategy strategy : Strategy.values()) {
            if (strategy.getCode() == code) {
                this.selectedStrategy = strategy;
                return true;
            }
        }
        return false;
    }
}

