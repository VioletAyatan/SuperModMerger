package ankol.mod.merger.rules

import kotlinx.serialization.Serializable

/**
 * 合并规则动作
 */
@Serializable
enum class RuleAction {
    /** 采纳 MOD 的删除意图，真正删除该节点 */
    DELETE,

    /** 视为 MOD 版本过旧导致的缺失，自动补回该节点 */
    RESTORE,

    /** 交给用户交互确认 */
    ASK,
}

/**
 * merge-rules.json 的根结构
 */
@Serializable
data class MergeRulesConfig(
    val scr: ScrRulesConfig = ScrRulesConfig(),
)

@Serializable
data class ScrRulesConfig(
    val deletion: DeletionRulesConfig = DeletionRulesConfig(),
)

/**
 * 节点删除规则配置
 *
 * @param defaultAction 没有任何规则命中时的动作；为 null 表示沿用上一层（内置配置）的值
 * @param rules 规则列表，按顺序排列，后面的规则覆盖前面的规则
 */
@Serializable
data class DeletionRulesConfig(
    val defaultAction: RuleAction? = null,
    val rules: List<DeletionRuleDef> = emptyList(),
)

/**
 * 单条删除规则。除 [file] 和 [action] 外均为可选，未填写表示不限制。
 *
 * @param file 文件路径（Ant 风格：`?`、`*`、`**`），忽略大小写
 * @param nodeType 节点类型，取值见 ScrNodeType.configName（funCall、funBlock、sub ...）
 * @param name 节点名称（函数名、变量名等），支持 `*`、`?` 通配，忽略大小写
 * @param argCount 参数个数，只对带参数的节点（funCall、methodReference、funBlock）生效
 * @param action 命中后的动作
 */
@Serializable
data class DeletionRuleDef(
    val file: String,
    val nodeType: String? = null,
    val name: String? = null,
    val argCount: Int? = null,
    val action: RuleAction,
)
