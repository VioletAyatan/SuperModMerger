import ankol.mod.merger.tools.Localizations
import org.junit.Test
import java.util.Locale

class SimTest {
    @Test
    fun test0() {
        val availableLocales = Locale.getAvailableLocales()
        availableLocales.forEach {
            println("language = ${it.language} -> ${it.country}")
        }
    }

    @Test
    fun test1() {
        println(Localizations.t("APP_MAIN_BASE_MOD_NOT_FOUND"))
        Localizations.setLocale(Locale.of("en"))
        println(Localizations.t("APP_MAIN_BASE_MOD_NOT_FOUND"))
        Localizations.setLocale(Locale.of("ru"))
        println(Localizations.t("APP_MAIN_BASE_MOD_NOT_FOUND"))
    }
}
