package yome.fgo.simulator.translation;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static yome.fgo.simulator.ResourceManager.readFile;
import static yome.fgo.simulator.utils.FilePathUtils.TRANSLATION_DIRECTORY_PATH;

public class TranslationManager {
    private static final INIConfiguration TRANSLATIONS = new INIConfiguration();
    private static final Map<String, String> TRAIT_REVERSE_MAP = new HashMap<>();

    public static final String APPLICATION_SECTION = "Application";
    public static final String CONDITION_SECTION = "Condition";
    public static final String CLASS_SECTION = "Trait";
    public static final String TARGET_SECTION = "Target";
    public static final String TRAIT_SECTION = "Trait";
    public static final String COMMAND_CARD_TYPE_SECTION = "CommandCardType";
    public static final String BUFF_SECTION = "Buff";
    public static final String ENTITY_NAME_SECTION = "Name";
    public static final String VARIATION_SECTION = "Variation";
    public static final String CLASS_ADV_SECTION = "ClassAdv";
    public static final String EFFECT_SECTION = "Effect";
    public static final String ENEMY_NAME_SECTION = "Name";
    public static final String SPECIAL_ACTIVATION_SECTION = "SpecialActivationTarget";
    public static final String MOONCELL_SECTION = "Mooncell";

    public static void setTranslations(final String language) {
        TRANSLATIONS.clear();
        TRAIT_REVERSE_MAP.clear();
        TRANSLATIONS.setSeparatorUsedInInput("=");
        try {
            final String fileName = TRANSLATION_DIRECTORY_PATH + "/" + language + ".ini";
            TRANSLATIONS.read(readFile(fileName));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        final SubnodeConfiguration traits = TRANSLATIONS.getSection(TRAIT_SECTION);
        final Iterator<String> keys = TRANSLATIONS.getSection(TRAIT_SECTION).getKeys();

        while (keys.hasNext()) {
            final String key = keys.next();
            TRAIT_REVERSE_MAP.put(traits.getString(key, key), key);
        }
    }

    public static String getTranslation(final String sectionName, final String key) {
        final String translation = TRANSLATIONS.getSection(sectionName).getString(key, key);
        if (translation.isEmpty()) {
            return key;
        } else {
            return translation;
        }
    }

    public static boolean hasTranslation(final String sectionName, final String key) {
        return !TRANSLATIONS.configurationAt(sectionName).isEmpty() && TRANSLATIONS.getSection(sectionName).containsKey(key);
    }

    public static String getKeyForTrait(final String traitTranslation) {
        return TRAIT_REVERSE_MAP.getOrDefault(traitTranslation, traitTranslation);
    }

    public static boolean hasKeyForTrait(final String traitTranslation) {
        return TRAIT_REVERSE_MAP.containsKey(traitTranslation) || TRANSLATIONS.getSection(TRAIT_SECTION).containsKey(traitTranslation);
    }
}
