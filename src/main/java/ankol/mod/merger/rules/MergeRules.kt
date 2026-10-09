package ankol.mod.merger.rules

import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.constants.ScrNodeType
import ankol.mod.merger.tools.Localizations.t
import ankol.mod.merger.tools.Tools
import ankol.mod.merger.tools.logger
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists
import kotlin.io.path.readText

/**
 * 合并规则加载器。
 *
 * 规则来源（按优先级从低到高）：
 * 1. 内置规则：classpath 下的 [BUILTIN_RESOURCE]，随程序打包；
 * 2. 外部规则：命令行 `-r` 指定的文件，未指定时为工作目录下的 [EXTERNAL_FILE_NAME]（不存在则忽略）。
 *
 * 规则列表按「内置在前、外部在后」拼接，匹配时后面的规则覆盖前面的规则；
 * 外部配置中的 defaultAction 覆盖内置配置。
 */
object MergeRules {
    private val log = logger<MergeRules>()

    const val BUILTIN_RESOURCE = "rules/merge-rules.json"
    const val EXTERNAL_FILE_NAME = "merge-rules.json"
    private const val BUILTIN_SOURCE = "builtin"

    /** 两层配置都没写 defaultAction 时的兜底值 */
    private val FALLBACK_DEFAULT_ACTION = RuleAction.RESTORE

    private val json = Json {
        allowComments = true
        allowTrailingComma = true
        decodeEnumsCaseInsensitive = true
    }

    /** 只包含内置规则的策略（单元测试或未调用 [load] 时使用） */
    val builtinPolicy: RuleBasedDeletionPolicy by lazy { buildPolicy(listOf(BUILTIN_SOURCE to loadBuiltinConfig())) }

    /**
     * 加载内置规则并叠加外部规则。
     *
     * @param explicitPath 命令行显式指定的外部规则文件；指定了但不存在时报错
     */
    fun load(explicitPath: Path? = null): RuleBasedDeletionPolicy {
        val layers = mutableListOf(BUILTIN_SOURCE to loadBuiltinConfig())
        val externalPath = explicitPath ?: Path(Tools.userDir, EXTERNAL_FILE_NAME)
        if (externalPath.notExists() || !externalPath.isRegularFile()) {
            if (explicitPath != null) {
                throw BusinessException(t("RULES_FILE_NOT_FOUND", explicitPath))
            }
        } else {
            log.info("Loading external merge rules: {}", externalPath)
            layers += externalPath.toString() to parse(externalPath.readText(), externalPath.toString())
        }
        val policy = buildPolicy(layers)
        log.info(
            "Merge rules loaded. defaultAction={}, rules={}", policy.defaultAction, policy.rules.size
        )
        return policy
    }

    /** 解析一份配置文本，[source] 用于错误提示 */
    fun parse(content: String, source: String): MergeRulesConfig {
        try {
            return json.decodeFromString(MergeRulesConfig.serializer(), content)
        } catch (e: SerializationException) {
            throw BusinessException(t("RULES_PARSE_FAILED", source, e.message))
        } catch (e: IllegalArgumentException) {
            throw BusinessException(t("RULES_PARSE_FAILED", source, e.message))
        }
    }

    /**
     * 按顺序合并多层配置并校验规则。
     */
    fun buildPolicy(layers: List<Pair<String, MergeRulesConfig>>): RuleBasedDeletionPolicy {
        var defaultAction: RuleAction? = null
        val rules = mutableListOf<CompiledDeletionRule>()
        for ((source, config) in layers) {
            val deletion = config.scr.deletion
            deletion.defaultAction?.let { defaultAction = it }
            deletion.rules.forEachIndexed { index, def -> rules += compile(def, source, index) }
        }
        return RuleBasedDeletionPolicy(defaultAction ?: FALLBACK_DEFAULT_ACTION, rules)
    }

    private fun compile(def: DeletionRuleDef, source: String, index: Int): CompiledDeletionRule {
        val position = "$source#${index + 1}"
        if (def.file.isBlank()) {
            throw BusinessException(t("RULES_INVALID_RULE", position, "file must not be blank"))
        }
        val nodeType = def.nodeType?.let { raw ->
            ScrNodeType.fromConfigName(raw) ?: throw BusinessException(
                t(
                    "RULES_INVALID_RULE", position,
                    "unknown nodeType '$raw', available: ${ScrNodeType.entries.joinToString { it.configName }}"
                )
            )
        }
        if (def.argCount != null && def.argCount < 0) {
            throw BusinessException(t("RULES_INVALID_RULE", position, "argCount must be >= 0"))
        }
        return CompiledDeletionRule(
            filePattern = def.file.trim(),
            nodeType = nodeType,
            namePattern = def.name?.takeIf { it.isNotBlank() },
            argCount = def.argCount,
            action = def.action,
            source = position,
        )
    }

    private fun loadBuiltinConfig(): MergeRulesConfig {
        val stream = MergeRules::class.java.classLoader.getResourceAsStream(BUILTIN_RESOURCE)
            ?: throw IllegalStateException("Builtin merge rules not found: $BUILTIN_RESOURCE")
        val content = stream.use { it.readBytes().toString(Charsets.UTF_8) }
        return parse(content, BUILTIN_SOURCE)
    }
}
