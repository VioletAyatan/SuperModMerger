package ankol.mod.merger.rules

import ankol.mod.merger.constants.ScrNodeType
import ankol.mod.merger.tools.AntPathMatcher

/**
 * 待判定的被删除节点
 *
 * @param filePath 节点所在文件在 pak 中的相对路径
 * @param nodeType 节点类型
 * @param name 节点名称
 * @param argCount 参数个数（无参数概念的节点为 null）
 */
data class DeletionTarget(
    val filePath: String,
    val nodeType: ScrNodeType,
    val name: String,
    val argCount: Int?,
)

/**
 * 节点删除决策策略：决定 MOD 中缺失的原版节点是真删除、自动补回还是询问用户。
 */
fun interface DeletionPolicy {
    fun decide(target: DeletionTarget): RuleAction
}

/**
 * 基于规则列表的删除策略。规则倒序匹配，即「后面的规则覆盖前面的规则」，都没命中时使用 [defaultAction]。
 */
class RuleBasedDeletionPolicy(
    val defaultAction: RuleAction,
    val rules: List<CompiledDeletionRule>,
) : DeletionPolicy {
    override fun decide(target: DeletionTarget): RuleAction {
        for (i in rules.indices.reversed()) {
            if (rules[i].matches(target)) return rules[i].action
        }
        return defaultAction
    }
}

/**
 * 校验并预编译后的规则
 *
 * @param source 规则来源（内置/外部文件路径），用于日志
 */
class CompiledDeletionRule(
    val filePattern: String,
    val nodeType: ScrNodeType?,
    namePattern: String?,
    val argCount: Int?,
    val action: RuleAction,
    val source: String,
) {
    private val nameRegex: Regex? = namePattern?.let { globToRegex(it) }

    fun matches(target: DeletionTarget): Boolean {
        if (nodeType != null && nodeType != target.nodeType) return false
        if (argCount != null && argCount != target.argCount) return false
        if (nameRegex != null && !nameRegex.matches(target.name)) return false
        return AntPathMatcher.matches(filePattern, target.filePath)
    }

    override fun toString(): String =
        "{file=$filePattern, nodeType=${nodeType?.configName}, name=${nameRegex?.pattern}, argCount=$argCount, action=$action, source=$source}"

    companion object {
        private fun globToRegex(glob: String): Regex {
            val sb = StringBuilder()
            for (ch in glob) {
                when (ch) {
                    '*' -> sb.append(".*")
                    '?' -> sb.append('.')
                    else -> sb.append(Regex.escape(ch.toString()))
                }
            }
            return Regex(sb.toString(), RegexOption.IGNORE_CASE)
        }
    }
}
