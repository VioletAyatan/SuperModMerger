package ankol.mod.merger.core

import ankol.mod.merger.merger.ConflictRecord
import ankol.mod.merger.tools.ColorPrinter
import ankol.mod.merger.tools.Localizations
import ankol.mod.merger.tools.logger

/**
 * 冲突解决器
 * 
 * @author Ankol
 */
object ConflictResolver {
    private val log = logger()

    /**
     * 交互式解决冲突
     * 
     * @param conflicts 冲突项目
     */
    fun resolveConflict(conflicts: MutableList<ConflictRecord>) {
        //筛选出智能合并的节点
        val automaticMerge = conflicts.filter { it.userChoice != null }
        if (!automaticMerge.isEmpty()) {
            for (item in automaticMerge) {
                ColorPrinter.print(
                    "Auto merging code-line: {}: {} -> {}: {}",
                    item.baseModName,
                    item.baseNode.getSourceText(),
                    item.mergeModName,
                    item.modNode.getSourceText()
                )
            }
            ColorPrinter.success(Localizations.t("CRESOLVER_AUTO_MERGE_COUNT", automaticMerge.size))
            conflicts.removeAll(automaticMerge) //暂时移除，主要是为了不出现冲突提示.
        }
        //对于真正的冲突项，提示用户选择使用哪一个版本解决
        if (!conflicts.isEmpty()) {
            println() //换行
            ColorPrinter.warning(Localizations.t("CRESOLVER_CONFLICT_DETECTED", conflicts.size))
            var chose = 0
            for (i in conflicts.indices) {
                val record = conflicts[i]
                if (chose == 3) {
                    record.userChoice = 1 //3表示用户全部选择baseMod的配置来处理
                } else if (chose == 4) {
                    record.userChoice = 2 //4表示用户全部选择mergeMod的配置来处理
                } else {
                    val baseNodeSource = record.baseNode.getSourceText().trim()
                    val modNodeSource = record.modNode.getSourceText().trim()
                    //打印代码提示框
                    ColorPrinter.info("=".repeat(75))
                    ColorPrinter.info(Localizations.t("CRESOLVER_FILE_INFO", i + 1, conflicts.size, record.fileName))
                    ColorPrinter.warning(Localizations.t("CRESOLVER_MOD_VERSION_1", record.baseModName))
                    ColorPrinter.bold(
                        Localizations.t(
                            "CRESOLVER_LINE_INFO",
                            record.baseNode.getLineNumber(),
                            baseNodeSource
                        )
                    )
                    ColorPrinter.warning(Localizations.t("CRESOLVER_MOD_VERSION_2", record.mergeModName))
                    ColorPrinter.bold(
                        Localizations.t(
                            "CRESOLVER_LINE_INFO",
                            record.modNode.getLineNumber(),
                            modNodeSource
                        )
                    )
                    ColorPrinter.info("=".repeat(75))
                    //选择对话框
                    ColorPrinter.info(Localizations.t("CRESOLVER_CHOOSE_PROMPT"))
                    ColorPrinter.info(Localizations.t("CRESOLVER_USE_OPTION_1", baseNodeSource))
                    ColorPrinter.info(Localizations.t("CRESOLVER_USE_OPTION_2", modNodeSource))
                    ColorPrinter.info(Localizations.t("CRESOLVER_USE_ALL_FROM_MOD_1", record.baseModName))
                    ColorPrinter.info(Localizations.t("CRESOLVER_USE_ALL_FROM_MOD_2", record.mergeModName))

                    while (true) {
                        val input = readln()
                        if (input == "1" || input == "2") {
                            record.userChoice = input.toInt()
                            break
                        }
                        if (input == "3" || input == "4") {
                            chose = input.toInt()
                            record.userChoice = if (chose == 3) 1 else 2
                            break
                        }
                        ColorPrinter.warning(Localizations.t("CRESOLVER_INVALID_INPUT"))
                    }
                }
            }
            ColorPrinter.success(Localizations.t("CRESOLVER_CONFLICT_RESOLVED"))
        }
        //最后把自动合并的节点加回去，让后续处理合并的逻辑使用同一个容器
        conflicts.addAll(automaticMerge)
    }
}
