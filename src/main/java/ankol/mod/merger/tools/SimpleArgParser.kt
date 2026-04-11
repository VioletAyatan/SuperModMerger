package ankol.mod.merger.tools

import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.tools.Localizations.t

/**
 * 简单的命令行参数解析工具
 *
 * @author Ankol
 */
class SimpleArgParser {
    // --- 定义存储结构 ---
    private val options = LinkedHashMap<String, Option>()
    // 关键映射：短名 -> 长名 (例如 "p" -> "port")
    private val shortToLongMap = HashMap<String, String>()
    // 解析结果 (统一使用长名作为 Key)
    private val parsedValues = HashMap<String, String>()
    private val parsedFlags = HashSet<String>()
    private val _positionalArgs = ArrayList<String>()

    /**
     * 添加命令行配置项
     *
     * @param shortName   短名称（-x）
     * @param longName    长名称（--xxx）
     * @param hasValue    是否需要值（支持不带值的命令行参数）
     * @param description 命令行描述
     */
    fun addOption(shortName: String?, longName: String, hasValue: Boolean, description: String) {
        val option = Option(shortName, longName, hasValue, description)
        options[longName] = option
        // 添加到长短名映射中
        if (!shortName.isNullOrEmpty()) {
            shortToLongMap[shortName] = longName
        }
    }

    /**
     * 解析命令行参数
     *
     * @param args 命令行参数数组
     */
    fun parse(args: Array<String>) {
        var i = 0
        while (i < args.size) {
            val arg = args[i]
            if (arg.startsWith("--")) {
                var rawName = arg.substring(2)
                var value: String? = null
                if (rawName.contains("=")) {
                    val parts = rawName.split("=", limit = 2)
                    rawName = parts[0]
                    value = parts[1]
                }
                val opt = options[rawName]
                    ?: throw BusinessException(t("ARG_PARSER_UNKNOWN_OPTION", arg))
                if (opt.hasValue) {
                    if (value == null && i + 1 < args.size && !args[i + 1].startsWith("-")) {
                        value = args[++i]
                    }
                    if (value.isNullOrEmpty()) {
                        throw BusinessException(t("ARG_PARSER_MISSING_VALUE", arg))
                    }
                    parsedValues[rawName] = value
                } else {
                    parsedFlags.add(rawName)
                }
            } else if (arg.startsWith("-")) {
                val shortName = arg.substring(1)
                val longName = shortToLongMap[shortName]
                    ?: throw BusinessException(t("ARG_PARSER_UNKNOWN_OPTION", arg))
                val opt = options[longName]!!
                if (opt.hasValue) {
                    if (i + 1 < args.size && !args[i + 1].startsWith("-")) {
                        parsedValues[longName] = args[++i]
                    } else {
                        throw BusinessException(t("ARG_PARSER_MISSING_VALUE", arg))
                    }
                } else {
                    parsedFlags.add(longName)
                }
            } else {
                // 位置参数
                _positionalArgs.add(arg)
            }
            i++
        }
    }

    /**
     * 将传入的 key (可能是短名，可能是长名) 统一转换为长名
     *
     * @param key key
     * @return 返回转换后的长名
     */
    fun resolveKey(key: String): String {
        // 如果这个 key 是已知的短名，返回对应的长名
        return shortToLongMap[key] ?: key
    }

    /**
     * 判断是否存在对应的选项
     *
     * @param key key (支持 -v 或 --verbose)
     * @return 对应选项是否存在
     */
    fun hasOption(key: String): Boolean {
        val longKey = resolveKey(key)
        return longKey in parsedFlags || longKey in parsedValues
    }

    /**
     * 获取选项值
     *
     * @param key (支持 -p 或 --port)
     */
    fun getOptionValue(key: String): String? {
        return parsedValues[resolveKey(key)]
    }

    /**
     * 获取选项值，带默认值
     *
     * @param key          支持 -p 或 --port
     * @param defaultValue 默认值
     */
    fun getOptionValue(key: String, defaultValue: String): String {
        return parsedValues.getOrDefault(resolveKey(key), defaultValue)
    }

    /**
     * 获取位置参数
     *
     * @return 位置参数列表
     */
    fun getPositionalArgs(): List<String> = _positionalArgs

    /**
     * 打印帮助信息
     */
    fun printHelp() {
        ColorPrinter.cyan(t("ARG_PARSER_USAGE"))
        for (opt in options.values) {
            val sName = if (opt.shortName != null) "-${opt.shortName}" else "  "
            val lName = "--${opt.longName}"
            val valParams = if (opt.hasValue) " <value>" else ""
            ColorPrinter.cyan("  {}, {} {}", sName, lName + valParams, opt.description)
        }
    }

    /**
     * 命令行指令
     *
     * @param shortName   短名称（-x）
     * @param longName    长名称（--xxx）
     * @param hasValue    是否需要值（支持不带值的命令行参数）
     * @param description 命令行描述
     */
    data class Option(
        val shortName: String?,
        val longName: String,
        val hasValue: Boolean,
        val description: String
    )
}

