package ankol.mod.merger.core

import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.constants.UserChoice.Companion.findByOrder
import ankol.mod.merger.domain.MergerContext
import ankol.mod.merger.merger.ConflictRecord
import ankol.mod.merger.tools.ColorPrinter
import ankol.mod.merger.tools.Localizations.t

/**
 * Conflict resolver
 *
 * @author Ankol
 */
object ConflictResolver {
    /**
     * Interactive conflict resolution
     *
     * @param conflicts Conflict items
     */
    fun resolveConflict(conflicts: MutableList<ConflictRecord>, context: MergerContext) {
        // Filter out nodes that can be auto-merged
        val automaticMerge = handleAutoMergingCode(conflicts)
        // For real conflicts, prompt user to choose which version to use
        if (!conflicts.isEmpty()) {
            println() // New line
            ColorPrinter.warning(t("CRESOLVER_CONFLICT_DETECTED", conflicts.size))

            var userChose: UserChoice? = null // User's choice
            for (i in conflicts.indices) {
                val record = conflicts[i]

                if (userChose == UserChoice.USE_ALL_BASE) {
                    record.userChoice = UserChoice.BASE_MOD // 3 means user chooses all baseMod configs
                } else if (userChose == UserChoice.USE_ALL_MERGE) {
                    record.userChoice = UserChoice.MERGE_MOD // 4 means user chooses all mergeMod configs
                } else {
                    // Get the text of the conflict nodes
                    val baseNodeSource = record.baseNode.sourceText.trim()
                    val modNodeSource = record.modNode.sourceText.trim()

                    ColorPrinter.blue("=".repeat(75))
                    ColorPrinter.cyan(t("CRESOLVER_FILE_INFO", i + 1, conflicts.size, record.fileName))
                    ColorPrinter.warning(t("CRESOLVER_MOD_VERSION_1", record.baseModName))
                    ColorPrinter.bold(t("CRESOLVER_LINE_INFO", record.baseNode.lineNumber, baseNodeSource))
                    ColorPrinter.warning(t("CRESOLVER_MOD_VERSION_2", record.mergeModName))
                    ColorPrinter.bold(t("CRESOLVER_LINE_INFO", record.modNode.lineNumber, modNodeSource))
                    ColorPrinter.blue("=".repeat(75))
                    // Selection dialog
                    ColorPrinter.bold(t("CRESOLVER_CHOOSE_PROMPT"))
                    ColorPrinter.cyan(t("CRESOLVER_USE_OPTION_1", baseNodeSource))
                    ColorPrinter.cyan(t("CRESOLVER_USE_OPTION_2", modNodeSource))
                    ColorPrinter.cyan(t("CRESOLVER_USE_ALL_FROM_MOD_1", record.baseModName))
                    ColorPrinter.cyan(t("CRESOLVER_USE_ALL_FROM_MOD_2", record.mergeModName))

                    while (true) {
                        val input = readln()
                        val choice = findByOrder(input.toIntOrNull())
                        if (choice == null) {
                            ColorPrinter.warning(t("CRESOLVER_INVALID_INPUT"))
                        } else {
                            userChose = choice
                            record.userChoice = choice
                            break
                        }
                    }
                }
            }
            ColorPrinter.success(t("CRESOLVER_CONFLICT_RESOLVED"))
        }
        // Finally, add the auto-merged nodes back for subsequent logic to use the same container
        conflicts.addAll(automaticMerge)
    }

    /**
     * Handle auto-merging code
     */
    private fun handleAutoMergingCode(conflicts: MutableList<ConflictRecord>): List<ConflictRecord> {
        val automaticMerge = conflicts.filter { it.userChoice != null }
        if (!automaticMerge.isEmpty()) {
            for (item in automaticMerge) {
                val modNodeText = item.modNode.sourceText
                ColorPrinter.print(
                    t(
                        "CRESOLVER_AUTO_MERGE_CODELINE",
                        "Vanilla",
                        item.baseNode.sourceText,
                        item.mergeModName,
                        modNodeText
                    )
                )
            }
            ColorPrinter.success(t("CRESOLVER_AUTO_MERGE_COUNT", automaticMerge.size))
            conflicts.removeAll(automaticMerge) // Temporarily remove, mainly to avoid conflict prompts.
        }
        return automaticMerge
    }
}
