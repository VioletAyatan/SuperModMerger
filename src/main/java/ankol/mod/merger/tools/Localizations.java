package ankol.mod.merger.tools;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

public class Localizations {
    private static Properties defaultProperties;
    private static Properties localProperties;

    private static final Locale locale;
    private static String langCode;

    static {
        locale = Locale.getDefault();
        langCode = locale.getLanguage();
    }

    public static void init() {
        try {
            System.out.println("locale = " + locale.getLanguage());
            localProperties = new Properties();
            if (locale.getLanguage().contains("zh")) {
                localProperties.load(new ClassPathResource("i18n/message.properties").getStream());
            } else {
                localProperties.load(new ClassPathResource("i18n/message_en.properties").getStream());
            }
            ClassPathResource resource = new ClassPathResource("i18n/message.properties");
            defaultProperties = new Properties();
            defaultProperties.load(resource.getStream());
        } catch (IOException e) {
            System.err.println("Load i18n Resources Failed! Place Concat the author! Failed Message: " + e.getMessage());
            System.exit(-1);
        }
    }

    private static Properties getTranslator() {
        if (langCode.equalsIgnoreCase("zh")) {
            return defaultProperties;
        } else {
            return localProperties;
        }
    }

    /**
     * 本地化语言
     *
     * @param key  语言key
     * @param args 参数集
     * @return 本地化后的翻译（没找到对应key值回退到默认值，都没有返回key值）
     */
    public static String t(String key, Object... args) {
        String text = getTranslator().getProperty(key);
        if (StrUtil.isEmpty(text)) {
            String defaultText = defaultProperties.getProperty(key);
            if (!StrUtil.isEmpty(defaultText)) {
                text = defaultText;
            } else {
                return key;
            }
        }
        return StrUtil.format(text, args);
    }

    public static void setLangCode(String langCode) {
        Localizations.langCode = langCode.toLowerCase();
    }
}
