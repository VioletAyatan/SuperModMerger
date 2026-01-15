package ankol.mod.merger.core

import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.constants.UserChoice.Companion.findByOrder
import ankol.mod.merger.merger.ConflictRecord
import ankol.mod.merger.merger.ConflictType
import ankol.mod.merger.tools.ColorPrinter
import ankol.mod.merger.tools.Localizations

/**
 * 冲突解决器
 * 
 * @author Ankol
 */
object ConflictResolver {
    /**
     * 交互式解决冲突
     * 
     * @param conflicts 冲突项目
     */
    fun resolveConflict(conflicts: MutableList<ConflictRecord>) {
        //筛选出智能合并的节点
        val automaticMerge = handleAutoMergingCode(conflicts)
        //对于真正的冲突项，提示用户选择使用哪一个版本解决
        if (!conflicts.isEmpty()) {
            println() //换行
            ColorPrinter.warning(Localizations.t("CRESOLVER_CONFLICT_DETECTED", conflicts.size))

            var userChose: UserChoice? = null //用户选择项
            for (i in conflicts.indices) {
                val record = conflicts[i]

                if (userChose == UserChoice.USE_ALL_BASE) {
                    record.userChoice = UserChoice.BASE_MOD //3表示用户全部选择baseMod的配置来处理
                } else if (userChose == UserChoice.USE_ALL_MERGE) {
                    record.userChoice = UserChoice.MERGE_MOD //4表示用户全部选择mergeMod的配置来处理
                } else {
                    val baseNodeSource = record.baseNode.sourceText.trim()

                    // 根据冲突类型显示不同的提示
                    if (record.conflictType == ConflictType.REMOVAL) {
                        // 删除类型冲突的特殊显示
                        ColorPrinter.info("=".repeat(75))
                        ColorPrinter.info(Localizations.t("CRESOLVER_FILE_INFO", i + 1, conflicts.size, record.fileName))
                        ColorPrinter.warning(Localizations.t("CRESOLVER_REMOVAL_DETECTED"))
                        ColorPrinter.warning(Localizations.t("CRESOLVER_MOD_VERSION_1", record.baseModName))
                        ColorPrinter.bold(
                            Localizations.t("CRESOLVER_LINE_INFO", record.baseNode.lineNumber, baseNodeSource)
                        )
                        ColorPrinter.warning(Localizations.t("CRESOLVER_REMOVAL_MOD_VERSION_2", record.mergeModName))
                        ColorPrinter.info("=".repeat(75))
                        // 删除类型的选择对话框
                        ColorPrinter.info(Localizations.t("CRESOLVER_CHOOSE_PROMPT"))
                        ColorPrinter.info(Localizations.t("CRESOLVER_REMOVAL_OPTION_1", baseNodeSource))
                        ColorPrinter.info(Localizations.t("CRESOLVER_REMOVAL_OPTION_2"))
                        ColorPrinter.info(Localizations.t("CRESOLVER_USE_ALL_FROM_MOD_1", record.baseModName))
                        ColorPrinter.info(Localizations.t("CRESOLVER_USE_ALL_FROM_MOD_2", record.mergeModName))
                    } else {
                        // 普通修改冲突的显示
                        val modNodeSource = record.modNode?.sourceText?.trim() ?: ""
                        ColorPrinter.info("=".repeat(75))
                        ColorPrinter.info(Localizations.t("CRESOLVER_FILE_INFO", i + 1, conflicts.size, record.fileName))
                        ColorPrinter.warning(Localizations.t("CRESOLVER_MOD_VERSION_1", record.baseModName))
                        ColorPrinter.bold(
                            Localizations.t("CRESOLVER_LINE_INFO", record.baseNode.lineNumber, baseNodeSource)
                        )
                        ColorPrinter.warning(Localizations.t("CRESOLVER_MOD_VERSION_2", record.mergeModName))
                        ColorPrinter.bold(
                            Localizations.t("CRESOLVER_LINE_INFO", record.modNode?.lineNumber ?: 0, modNodeSource)
                        )
                        ColorPrinter.info("=".repeat(75))
                        //选择对话框
                        ColorPrinter.info(Localizations.t("CRESOLVER_CHOOSE_PROMPT"))
                        ColorPrinter.info(Localizations.t("CRESOLVER_USE_OPTION_1", baseNodeSource))
                        ColorPrinter.info(Localizations.t("CRESOLVER_USE_OPTION_2", modNodeSource))
                        ColorPrinter.info(Localizations.t("CRESOLVER_USE_ALL_FROM_MOD_1", record.baseModName))
                        ColorPrinter.info(Localizations.t("CRESOLVER_USE_ALL_FROM_MOD_2", record.mergeModName))
                    }

                    while (true) {
                        val input = readln()
                        val choice = findByOrder(input.toIntOrNull())
                        if (choice == null) {
                            ColorPrinter.warning(Localizations.t("CRESOLVER_INVALID_INPUT"))
                            break
                        } else {
                            userChose = choice
                            record.userChoice = choice
                            break
                        }
                    }
                }
            }
            ColorPrinter.success(Localizations.t("CRESOLVER_CONFLICT_RESOLVED"))
        }
        //最后把自动合并的节点加回去，让后续处理合并的逻辑使用同一个容器
        conflicts.addAll(automaticMerge)
    }

    /**
     * 处理自动合并的代码
     */
    private fun handleAutoMergingCode(conflicts: MutableList<ConflictRecord>): List<ConflictRecord> {
        val automaticMerge = conflicts.filter { it.userChoice != null }
        if (!automaticMerge.isEmpty()) {
            for (item in automaticMerge) {
                val modNodeText = item.modNode?.sourceText ?: "[REMOVED]"
                ColorPrinter.print(
                    Localizations.t(
                        "CRESOLVER_AUTO_MERGE_CODELINE",
                        item.baseModName,
                        item.baseNode.sourceText,
                        item.mergeModName,
                        modNodeText
                    )
                )
            }
            ColorPrinter.success(Localizations.t("CRESOLVER_AUTO_MERGE_COUNT", automaticMerge.size))
            conflicts.removeAll(automaticMerge) //暂时移除，主要是为了不出现冲突提示.
        }
        return automaticMerge
    }
}
