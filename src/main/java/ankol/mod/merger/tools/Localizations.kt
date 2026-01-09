package ankol.mod.merger.tools

import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.system.exitProcess

/**
 * 国际化工具类
 * @author Ankol
 */
object Localizations {
    private lateinit var defaultProperties: Properties
    private lateinit var localProperties: Properties

    private val locale: Locale = Locale.getDefault()
    private var langCode: String = locale.language

    private val translator: Properties
        get() {
            return if (langCode.equals("zh", ignoreCase = true)) {
                defaultProperties
            } else {
                localProperties
            }
        }

    fun init() {
        try {
            localProperties = Properties()
            val localResourcePath =
                if (locale.language.contains("zh")) "i18n/message.properties" else "i18n/message_en.properties"
            getClassLoader().getResourceAsStream(localResourcePath).use { stream: InputStream? ->
                if (stream == null) {
                    throw IOException("Resource not found: $localResourcePath")
                }
                localProperties.load(stream)
            }
            defaultProperties = Properties()
            getClassLoader().getResourceAsStream("i18n/message.properties")
                .use { stream: InputStream? ->
                    if (stream == null) {
                        throw IOException("Resource not found: i18n/message.properties")
                    }
                    defaultProperties.load(stream)
                }
        } catch (e: IOException) {
            ColorPrinter.error("Failed to load i18n resources! Please contact the author! Error message: {}", e.message)
            exitProcess(-1)
        }
    }

    /**
     * 本地化语言
     * 
     * @param key  语言key
     * @param args 参数集
     * @return 本地化后的翻译（没找到对应key值回退到默认值，都没有返回key值）
     */
    @JvmStatic
    fun t(key: String, vararg args: Any?): String {
        var text: String? = translator.getProperty(key)
        if (text.isNullOrEmpty()) {
            val defaultText = defaultProperties.getProperty(key)
            if (!defaultText.isNullOrEmpty()) {
                text = defaultText
            } else {
                return key
            }
        }
        return Tools.format(text, *args)
    }

    fun setLangCode(langCode: String) {
        Localizations.langCode = langCode.lowercase(Locale.getDefault())
    }

    fun getClassLoader(): ClassLoader {
        return Localizations::class.java.classLoader
    }
}
