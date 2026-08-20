package ankol.mod.merger.mergers.scr

import ankol.mod.merger.mergers.scr.node.ScrFunCallNode
import java.util.Locale

/**
 * Internal allowlist for SCR function calls that may be deleted without user confirmation.
 * Mods cannot provide or override these rules.
 */
internal object ScrDeletionWhitelist {
    private const val INVENTORY_DIRECTORY = "scripts/inventory"
    private const val INVENTORY_FILE_PREFIX = "inventory"
    private const val SCR_EXTENSION = ".scr"
    private const val DLC_FUNCTION = "Dlc"

    fun allows(filePath: String, node: ScrFunCallNode): Boolean {
        val normalizedPath = filePath
            .replace('\\', '/')
            .trimStart('/')
            .lowercase(Locale.ROOT)

        val directory = normalizedPath.substringBeforeLast('/', "")
        val fileName = normalizedPath.substringAfterLast('/')
        val isInventoryFile = directory == INVENTORY_DIRECTORY &&
                fileName.startsWith(INVENTORY_FILE_PREFIX) &&
                fileName.endsWith(SCR_EXTENSION)

        if (!isInventoryFile) return false

        val isDirectFunctionCall = node.signature.startsWith("${TechlandScrFileVisitor.FUN_CALL}:")
        return isDirectFunctionCall && node.functionName == DLC_FUNCTION && node.arguments.isEmpty()
    }
}
