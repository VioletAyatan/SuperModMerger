package ankol.mod.merger.tools

import java.util.*

/**
 * Internationalization utility class
 * @author Ankol
 */
object Localizations {
    private lateinit var defaultProperties: Properties

    private var locale: Locale = Locale.getDefault()
    private val bundle = ResourceBundle.getBundle("i18n/message", locale)

    /**
     * Localization
     *
     * @param key  Language key
     * @param args Arguments
     * @return Translated string (if not found, returns default value, if none, returns key)
     */
    fun t(key: String, vararg args: Any?): String {
        var text: String? = bundle.getString(key)
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

}
