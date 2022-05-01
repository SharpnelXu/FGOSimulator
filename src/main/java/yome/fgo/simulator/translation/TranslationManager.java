package yome.fgo.simulator.translation;

import org.apache.commons.configuration2.INIConfiguration;

import java.io.FileReader;

import static yome.fgo.simulator.utils.FilePathUtils.TRANSLATION_DIRECTORY_PATH;

public class TranslationManager {
    private static final INIConfiguration TRANSLATIONS = new INIConfiguration();

    public static final String APPLICATION_SECTION = "Application";
    public static final String ALIGNMENT = "Alignment";
    public static final String ATTRIBUTE = "Attribute";
    public static final String CLASS = "Class";
    public static final String GENDER = "Gender";

    public static void setTranslations(final String language) {
        TRANSLATIONS.clear();
        try {
            TRANSLATIONS.read(new FileReader(TRANSLATION_DIRECTORY_PATH + "\\" + language + ".ini"));
        } catch (final Exception e) {
            System.out.println("Loading Failed...");
            throw new RuntimeException(e);
        }
    }

    public static String getTranslations(final String sectionName, final String key) {
        final String translation = TRANSLATIONS.getSection(sectionName).getString(key, key);
        if (translation.isEmpty()) {
            return key;
        } else {
            return translation;
        }
    }
}
