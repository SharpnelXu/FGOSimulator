package yome.fgo.simulator.models.variations;

import com.google.common.collect.ImmutableSet;
import yome.fgo.simulator.models.Simulation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static yome.fgo.simulator.models.variations.Variation.VariationFields.VARIATION_FIELD_BUFF;
import static yome.fgo.simulator.models.variations.Variation.VariationFields.VARIATION_FIELD_HP;
import static yome.fgo.simulator.models.variations.Variation.VariationFields.VARIATION_FIELD_MAX_COUNT;
import static yome.fgo.simulator.models.variations.Variation.VariationFields.VARIATION_FIELD_TARGET;
import static yome.fgo.simulator.models.variations.Variation.VariationFields.VARIATION_FIELD_TRAIT;
import static yome.fgo.simulator.translation.TranslationManager.VARIATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public abstract class Variation {
    public enum VariationFields {
        VARIATION_FIELD_MAX_COUNT,
        VARIATION_FIELD_TRAIT,
        VARIATION_FIELD_BUFF,
        VARIATION_FIELD_HP,
        VARIATION_FIELD_TARGET
    }

    /**
     * Display order of GUI is the same as Enum order.
     */
    public enum VariationType {
        NO_VARIATION(NoVariation.class.getSimpleName(), ImmutableSet.of()),
        BUFF_COUNT_VARIATION(
                BuffCountVariation.class.getSimpleName(),
                ImmutableSet.of(VARIATION_FIELD_MAX_COUNT, VARIATION_FIELD_BUFF, VARIATION_FIELD_TARGET)
        ),
        TRAIT_COUNT_VARIATION(
                TraitCountVariation.class.getSimpleName(),
                ImmutableSet.of(VARIATION_FIELD_MAX_COUNT, VARIATION_FIELD_TRAIT, VARIATION_FIELD_TARGET)
        ),
        HP_ABSORPTION_VARIATION(HpAbsorptionVariation.class.getSimpleName(), ImmutableSet.of(VARIATION_FIELD_TARGET)),
        NP_ABSORPTION_VARIATION(NpAbsorptionVariation.class.getSimpleName(), ImmutableSet.of(VARIATION_FIELD_TARGET)),
        TARGET_COUNT_VARIATION(TargetCountVariation.class.getSimpleName(), ImmutableSet.of(VARIATION_FIELD_TARGET)),
        TURN_PASS_VARIATION(TurnPassVariation.class.getSimpleName(), ImmutableSet.of()),
        HP_VARIATION(HpVariation.class.getSimpleName(), ImmutableSet.of(VARIATION_FIELD_HP, VARIATION_FIELD_TARGET));


        private final String type;
        private final Set<VariationFields> requiredFields;

        VariationType(final String type, final Set<VariationFields> requiredFields) {
            this.type = type;
            this.requiredFields = requiredFields;
        }

        private static final Map<String, VariationType> BY_TYPE_STRING = new LinkedHashMap<>();

        static {
            for (final VariationType variationType : values()) {
                BY_TYPE_STRING.put(variationType.type, variationType);
            }
        }

        public Set<VariationFields> getRequiredFields() {
            return requiredFields;
        }

        public static VariationType ofType(final String type) {
            return BY_TYPE_STRING.get(type);
        }

        public static Set<String> getOrder() {
            return BY_TYPE_STRING.keySet();
        }
    }

    public abstract double evaluate(final Simulation simulation, final double baseValue, final double additionValue);

    @Override
    public String toString() {
        return getTranslation(VARIATION_SECTION, getClass().getSimpleName());
    }
}
