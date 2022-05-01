package yome.fgo.simulator.translation;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static yome.fgo.simulator.utils.FilePathUtils.TRANSLATION_DIRECTORY_PATH;

public class TranslationManager {
    private static final INIConfiguration TRANSLATIONS = new INIConfiguration();
    private static final Map<String, String> TRAIT_REVERSE_MAP = new HashMap<>();

    public static final String APPLICATION_SECTION = "Application";
    public static final String CLASS = "Class";
    public static final String TRAIT = "Trait";

    public static void setTranslations(final String language) {
        TRANSLATIONS.clear();
        try {
            TRANSLATIONS.read(new FileReader(TRANSLATION_DIRECTORY_PATH + "\\" + language + ".ini"));
        } catch (final Exception e) {
            System.out.println("Loading Failed...");
            throw new RuntimeException(e);
        }

        final SubnodeConfiguration traits = TRANSLATIONS.getSection(TRAIT);
        final Iterator<String> keys = traits.getKeys();

        while (keys.hasNext()) {
            final String key = keys.next();
            TRAIT_REVERSE_MAP.put(traits.getString(key, key), key);
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

    public static String getKeyForTrait(final String traitTranslation) {
        return TRAIT_REVERSE_MAP.getOrDefault(traitTranslation, traitTranslation);
    }

    public static boolean hasKeyForTrait(final String traitTranslation) {
        return TRAIT_REVERSE_MAP.containsKey(traitTranslation) || TRANSLATIONS.getSection(TRAIT).containsKey(traitTranslation);
    }
}
