package ankol.mod.merger.mergers.scr

import ankol.mod.merger.mergers.scr.node.ScrFunCallNode
import ankol.mod.merger.tools.AntPathMatcher

/**
 * Internal allowlist for SCR function calls that may be deleted without user confirmation.
 * Mods cannot provide or override these rules.
 */
internal object ScrDeletionWhitelist {
    private data class Rule(
        val pathPattern: String,
        val functionName: String,
        val arguments: List<String>
    )

    private val rules = listOf(
        Rule(
            pathPattern = "scripts/inventory/inventory*.scr",
            functionName = "Dlc",
            arguments = emptyList()
        )
    )

    fun allows(filePath: String, node: ScrFunCallNode): Boolean {
        val isDirectFunctionCall = node.signature.startsWith("${TechlandScrFileVisitor.FUN_CALL}:")
        if (!isDirectFunctionCall) return false

        return rules.any { rule ->
            AntPathMatcher.matches(rule.pathPattern, filePath) &&
                    node.functionName == rule.functionName &&
                    node.arguments == rule.arguments
        }
    }
}
