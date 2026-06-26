package ankol.mod.merger.core

import ankol.mod.merger.api.ConflictResolutionStrategy
import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.constants.UserChoice.Companion.findByOrder
import ankol.mod.merger.mergers.ConflictRecord
import ankol.mod.merger.mergers.DeletionRecord
import ankol.mod.merger.tools.ConsoleColorPrinter
import ankol.mod.merger.tools.Localizations.t

/**
 * Conflict resolver (console implementation)
 *
 * @author Ankol
 */
object ConflictResolver : ConflictResolutionStrategy {
    /**
     * Interactive conflict resolution
     *
     * @param conflicts Conflict items
     */
    override fun resolveConflict(conflicts: MutableList<ConflictRecord>) {
        // Filter out nodes that can be auto-merged
        val automaticMerge = handleAutoMergingCode(conflicts)
        // For real conflicts, prompt user to choose which version to use
        if (!conflicts.isEmpty()) {
            println() // New line
            ConsoleColorPrinter.warning(t("CRESOLVER_CONFLICT_DETECTED", conflicts.size))

            var userChoice: UserChoice? = null // User's choice
            for (i in conflicts.indices) {
                val record = conflicts[i]

                if (userChoice == UserChoice.USE_ALL_BASE) {
                    record.userChoice = UserChoice.BASE_MOD
                } else if (userChoice == UserChoice.USE_ALL_MERGE) {
                    record.userChoice = UserChoice.MERGE_MOD
                } else {
                    // Get the text of the conflict nodes
                    val baseNodeSource = record.baseNode.sourceText.trim()
                    val modNodeSource = record.modNode.sourceText.trim()

                    ConsoleColorPrinter.blue("=".repeat(75))
                    ConsoleColorPrinter.cyan(t("CRESOLVER_FILE_INFO", i + 1, conflicts.size, record.fileName))
                    ConsoleColorPrinter.warning(t("CRESOLVER_MOD_VERSION_1", record.baseModName))
                    ConsoleColorPrinter.bold(t("CRESOLVER_LINE_INFO", record.baseNode.lineNumber, baseNodeSource))
                    ConsoleColorPrinter.warning(t("CRESOLVER_MOD_VERSION_2", record.mergeModName))
                    ConsoleColorPrinter.bold(t("CRESOLVER_LINE_INFO", record.modNode.lineNumber, modNodeSource))
                    ConsoleColorPrinter.blue("=".repeat(75))
                    // Selection dialog
                    ConsoleColorPrinter.bold(t("CRESOLVER_CHOOSE_PROMPT"))
                    ConsoleColorPrinter.cyan(t("CRESOLVER_USE_OPTION_1", baseNodeSource))
                    ConsoleColorPrinter.cyan(t("CRESOLVER_USE_OPTION_2", modNodeSource))
                    ConsoleColorPrinter.cyan(t("CRESOLVER_USE_ALL_FROM_MOD_1", record.baseModName))
                    ConsoleColorPrinter.cyan(t("CRESOLVER_USE_ALL_FROM_MOD_2", record.mergeModName))

                    while (true) {
                        val input = readln()
                        val choice = findByOrder(input.toIntOrNull())
                        if (choice == null) {
                            ConsoleColorPrinter.warning(t("CRESOLVER_INVALID_INPUT"))
                        } else {
                            userChoice = choice
                            record.userChoice = when (choice) {
                                UserChoice.USE_ALL_BASE -> UserChoice.BASE_MOD
                                UserChoice.USE_ALL_MERGE -> UserChoice.MERGE_MOD
                                else -> choice
                            }
                            break
                        }
                    }
                }
            }
            ConsoleColorPrinter.success(t("CRESOLVER_CONFLICT_RESOLVED"))
        }
        // Finally, add the auto-merged nodes back for subsequent logic to use the same container
        conflicts.addAll(automaticMerge)
    }

    /**
     * 删除冲突交互解决
     *
     * 针对 incoming mod 中缺失但 accumulated 中存在的节点，询问用户是保留还是删除。
     */
    override fun resolveDeletionConflicts(deletions: MutableList<DeletionRecord>) {
        if (deletions.isEmpty()) return

        println()
        ConsoleColorPrinter.warning(t("DELETION_DETECTED", deletions.size))

        var globalChoice: UserChoice? = null

        for (i in deletions.indices) {
            val record = deletions[i]

            if (globalChoice == UserChoice.USE_ALL_BASE) {
                record.userChoice = UserChoice.BASE_MOD
                continue
            } else if (globalChoice == UserChoice.USE_ALL_MERGE) {
                record.userChoice = UserChoice.MERGE_MOD
                continue
            }

            val nodeText = record.accumulatedNode.sourceText.trim()

            ConsoleColorPrinter.blue("=".repeat(75))
            ConsoleColorPrinter.cyan(t("DELETION_FILE_INFO", i + 1, deletions.size, record.fileName, record.deletingModName))
            if (record.isModifyDeleteConflict) {
                ConsoleColorPrinter.warning(t("DELETION_MODIFY_CONFLICT_WARNING", record.previousModName))
            }
            ConsoleColorPrinter.bold(t("DELETION_NODE_INFO", record.accumulatedNode.lineNumber, nodeText))
            ConsoleColorPrinter.blue("=".repeat(75))
            ConsoleColorPrinter.bold(t("DELETION_CHOOSE_PROMPT"))
            ConsoleColorPrinter.cyan(t("DELETION_KEEP_NODE"))
            ConsoleColorPrinter.cyan(t("DELETION_DELETE_NODE", record.deletingModName))
            ConsoleColorPrinter.cyan(t("DELETION_KEEP_ALL"))
            ConsoleColorPrinter.cyan(t("DELETION_DELETE_ALL"))

            while (true) {
                val input = readln()
                val choice = findByOrder(input.toIntOrNull())
                if (choice == null) {
                    ConsoleColorPrinter.warning(t("CRESOLVER_INVALID_INPUT"))
                } else {
                    globalChoice = choice
                    record.userChoice = when (choice) {
                        UserChoice.USE_ALL_BASE -> UserChoice.BASE_MOD
                        UserChoice.USE_ALL_MERGE -> UserChoice.MERGE_MOD
                        else -> choice
                    }
                    break
                }
            }
        }

        ConsoleColorPrinter.success(t("DELETION_RESOLVED"))
    }

    /**
     * Handle auto-merging code
     */
    private fun handleAutoMergingCode(conflicts: MutableList<ConflictRecord>): List<ConflictRecord> {
        val automaticMerge = conflicts.filter { it.userChoice != null }
        if (!automaticMerge.isEmpty()) {
            for (item in automaticMerge) {
                val modNodeText = item.modNode.sourceText
                ConsoleColorPrinter.print(
                    t(
                        "CRESOLVER_AUTO_MERGE_CODELINE",
                        "Vanilla",
                        item.baseNode.sourceText,
                        item.mergeModName,
                        modNodeText
                    )
                )
            }
            ConsoleColorPrinter.success(t("CRESOLVER_AUTO_MERGE_COUNT", automaticMerge.size))
            conflicts.removeAll(automaticMerge) // Temporarily remove, mainly to avoid conflict prompts.
        }
        return automaticMerge
    }
}
