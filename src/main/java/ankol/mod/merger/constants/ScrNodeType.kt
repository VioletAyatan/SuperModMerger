package ankol.mod.merger.constants

/**
 * SCR 语法树节点类型。
 *
 * [configName] 是规则配置文件（merge-rules.json）中 `nodeType` 字段使用的名称。
 */
enum class ScrNodeType(val configName: String) {
    ROOT("root"),
    IMPORT("import"),
    EXPORT("export"),
    SUB("sub"),
    FUN_BLOCK("funBlock"),
    FUN_CALL("funCall"),
    METHOD_REFERENCE("methodReference"),
    DIRECTIVE("directive"),
    MACRO("macro"),
    VARIABLE("variable"),
    VARIABLE_ASSIGN("variableAssign"),
    EXTERN("extern"),
    USE("use"),
    USE_SEMANTIC("useSemantic"),
    IF("if"),
    ELSE_IF("elseIf"),
    ELSE("else");

    companion object {
        /** 按配置名称查找（忽略大小写），找不到返回 null */
        fun fromConfigName(name: String): ScrNodeType? =
            entries.firstOrNull { it.configName.equals(name, ignoreCase = true) }
    }
}
