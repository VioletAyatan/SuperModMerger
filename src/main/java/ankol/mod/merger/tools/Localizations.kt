package ankol.mod.merger.tools

import java.util.*

/**
 * Internationalization utility class
 * @author Ankol
 */
object Localizations {
    private val log = logger()

    private var locale: Locale = Locale.getDefault()
    private var bundle = ResourceBundle.getBundle("i18n/message", locale)

    /**
     * Localization
     *
     * @param key  Language key
     * @param args Arguments
     * @return Translated string (if not found, returns default value, if none, returns key)
     */
    fun t(key: String, vararg args: Any?): String {
        try {
            val text: String? = bundle.getString(key)
            return if (text != null) {
                Tools.format(text, *args)
            } else {
                key
            }
        } catch (e: MissingResourceException) {
            log.debug("Missing localization resource. Reason: ${e.message}", e)
            return key
        }
    }

    /**
     * 设置本地化区域
     */
    fun setLocale(locale: Locale) {
        this.locale = locale
        this.bundle = ResourceBundle.getBundle("i18n/message", locale)
    }

}
