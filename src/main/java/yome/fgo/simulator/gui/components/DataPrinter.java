package yome.fgo.simulator.gui.components;

import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeAdditionalParams;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.NpDamageAdditionalParams;
import yome.fgo.data.proto.FgoStorageData.VariationData;
import yome.fgo.simulator.models.conditions.ConditionFactory.ConditionFields;
import yome.fgo.simulator.models.effects.EffectFactory.EffectFields;
import yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields;
import yome.fgo.simulator.models.variations.VariationFactory.VariationFields;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode.CLASS_ADV_NO_CHANGE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_REQUIRED_FIELD_MAP;
import static yome.fgo.simulator.models.conditions.ConditionFactory.ConditionFields.CONDITION_FIELD_BUFF_TYPE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.ConditionFields.CONDITION_FIELD_CARD_TYPE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.ConditionFields.CONDITION_FIELD_CLASS_VALUE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.ConditionFields.CONDITION_FIELD_DOUBLE_VALUE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.ConditionFields.CONDITION_FIELD_INT_VALUE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.ConditionFields.CONDITION_FIELD_LIMITED_SUB_CONDITION;
import static yome.fgo.simulator.models.conditions.ConditionFactory.ConditionFields.CONDITION_FIELD_SERVANT;
import static yome.fgo.simulator.models.conditions.ConditionFactory.ConditionFields.CONDITION_FIELD_TARGET;
import static yome.fgo.simulator.models.conditions.ConditionFactory.ConditionFields.CONDITION_FIELD_TRAIT_VALUE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.ConditionFields.CONDITION_FIELD_UNLIMITED_SUB_CONDITION;
import static yome.fgo.simulator.models.effects.EffectFactory.EFFECT_REQUIRED_FIELDS_MAP;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_CARD_TYPE_SELECT;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_DOUBLE_VALUE;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_GRANT_BUFF;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_HP_CHANGE;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_INT_VALUE;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_NP_DAMAGE;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_RANDOM_SELECTION;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_REMOVE_BUFF;
import static yome.fgo.simulator.models.effects.EffectFactory.EffectFields.EFFECT_FIELD_TARGET;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BUFF_REQUIRED_FIELDS_MAP;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields.BUFF_FIELD_CARD_TYPE;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields.BUFF_FIELD_CLASS_ADV;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields.BUFF_FIELD_DOUBLE_VALUE;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields.BUFF_FIELD_EFFECTS;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields.BUFF_FIELD_INT_VALUE;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields.BUFF_FIELD_PERCENT_OPTION;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields.BUFF_FIELD_STRING_VALUE;
import static yome.fgo.simulator.models.variations.VariationFactory.VARIATION_REQUIRED_FIELDS_MAP;
import static yome.fgo.simulator.models.variations.VariationFactory.VariationFields.VARIATION_FIELD_BUFF;
import static yome.fgo.simulator.models.variations.VariationFactory.VariationFields.VARIATION_FIELD_HP;
import static yome.fgo.simulator.models.variations.VariationFactory.VariationFields.VARIATION_FIELD_MAX_COUNT;
import static yome.fgo.simulator.models.variations.VariationFactory.VariationFields.VARIATION_FIELD_TARGET;
import static yome.fgo.simulator.models.variations.VariationFactory.VariationFields.VARIATION_FIELD_TRAIT;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.BUFF_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_ADV_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CONDITION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.EFFECT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.SERVANT_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.VARIATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class DataPrinter {
    public static String printConditionData(final ConditionData conditionData) {
        if (conditionData == null || conditionData.getType().isEmpty()) {
            return "";
        }

        final String type = conditionData.getType();
        final StringBuilder builder = new StringBuilder();

        builder.append(getTranslation(CONDITION_SECTION, type));

        final Set<ConditionFields> requiredFields = CONDITION_REQUIRED_FIELD_MAP.get(type);
        if (requiredFields == null || requiredFields.isEmpty()) {
            return builder.toString();
        }

        if (requiredFields.contains(CONDITION_FIELD_INT_VALUE)) {
            builder.append(" : ");
            builder.append(intToString(conditionData.getDoubleValue()));
        } else if (requiredFields.contains(CONDITION_FIELD_DOUBLE_VALUE)) {
            builder.append(" : ");
            builder.append(doubleToString(conditionData.getDoubleValue()));
        }
        if (requiredFields.contains(CONDITION_FIELD_TRAIT_VALUE)) {
            builder.append(" : ");
            builder.append(getTranslation(TRAIT_SECTION, conditionData.getValue()));
        } else if (requiredFields.contains(CONDITION_FIELD_SERVANT)) {
            builder.append(" : ");
            builder.append(getTranslation(SERVANT_NAME_SECTION, conditionData.getValue()));
        } else if (requiredFields.contains(CONDITION_FIELD_BUFF_TYPE)) {
            builder.append(" : ");
            builder.append(getTranslation(BUFF_SECTION, conditionData.getValue()));
        } else if (requiredFields.contains(CONDITION_FIELD_CARD_TYPE)) {
            builder.append(" : ");
            builder.append(getTranslation(COMMAND_CARD_TYPE_SECTION, conditionData.getValue()));
        } else if (requiredFields.contains(CONDITION_FIELD_CLASS_VALUE)) {
            builder.append(" : ");
            builder.append(getTranslation(CLASS_SECTION, conditionData.getValue()));
        }
        if (requiredFields.contains(CONDITION_FIELD_TARGET)) {
            builder.append(" : ");
            builder.append(getTranslation(TARGET_SECTION, conditionData.getTarget().name()));
        }
        if (requiredFields.contains(CONDITION_FIELD_LIMITED_SUB_CONDITION) || requiredFields.contains(
                CONDITION_FIELD_UNLIMITED_SUB_CONDITION)) {
            builder.append(" : ");
            final List<String> subConditionStrings = conditionData.getSubConditionDataList()
                    .stream()
                    .map(DataPrinter::printConditionData)
                    .collect(Collectors.toList());
            builder.append(subConditionStrings);
        }

        return builder.toString();
    }

    public static String printVariationData(final VariationData variationData) {
        if (variationData == null || variationData.getType().isEmpty()) {
            return "";
        }

        final String type = variationData.getType();
        final StringBuilder builder = new StringBuilder();

        builder.append(getTranslation(VARIATION_SECTION, type));

        final Set<VariationFields> requiredFields = VARIATION_REQUIRED_FIELDS_MAP.get(type);
        if (requiredFields == null || requiredFields.isEmpty()) {
            return builder.toString();
        }

        if (requiredFields.contains(VARIATION_FIELD_MAX_COUNT)) {
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Max Count"));
            builder.append(" ");
            builder.append(variationData.getMaxCount());
        }

        if (requiredFields.contains(VARIATION_FIELD_TARGET)) {
            builder.append(" : ");
            builder.append(getTranslation(TARGET_SECTION, variationData.getTarget().name()));
        }

        if (requiredFields.contains(VARIATION_FIELD_BUFF)) {
            builder.append(" : ");
            builder.append(printConditionData(variationData.getConditionData()));
        }

        if (requiredFields.contains(VARIATION_FIELD_TRAIT)) {
            builder.append(" : ");
            builder.append(getTranslation(TRAIT_SECTION, variationData.getTrait()));
        }

        if (requiredFields.contains(VARIATION_FIELD_HP)) {
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Max HP (%)"));
            builder.append(" ");
            builder.append(doubleToString(variationData.getMaxHp()));
            builder.append(" ");
            builder.append(getTranslation(APPLICATION_SECTION, "Min HP (%)"));
            builder.append(" ");
            builder.append(doubleToString(variationData.getMinHp()));
        }

        return builder.toString();
    }

    public static String doubleToString(final double rawDouble) {
        return String.format("%.2f", rawDouble * 100);
    }

    public static String doubleListToString(final List<Double> doubles) {
        return doubles.stream().map(DataPrinter::doubleToString).collect(Collectors.toList()).toString();
    }

    public static String intToString(final double rawInt) {
        return String.format("%d", (int) rawInt);
    }

    public static String intListToString(final List<Double> doubles) {
        return doubles.stream().map(DataPrinter::intToString).collect(Collectors.toList()).toString();
    }

    public static String printTraits(final List<String> traits) {
        return traits.stream().map(s -> getTranslation(TRAIT_SECTION, s)).collect(Collectors.toList()).toString();
    }

    public static String printBuffData(final BuffData buffData) {
        if (buffData == null || buffData.getType().isEmpty()) {
            return "";
        }
        final String type = buffData.getType();
        final StringBuilder builder = new StringBuilder();

        builder.append(getTranslation(BUFF_SECTION, type));

        if (buffData.getNumTurnsActive() > 0) {
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Num Turns Active"));
            builder.append(" ");
            builder.append(buffData.getNumTurnsActive());
        }

        if (buffData.getNumTimesActive() > 0) {
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Num Times Active"));
            builder.append(" ");
            builder.append(buffData.getNumTimesActive());
        }

        if (buffData.getIrremovable()) {
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Irremovable"));
        }

        if (buffData.getForceStackable()) {
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Force Stackable"));
        }

        if (buffData.getProbabilitiesCount() > 0) {
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Custom Probability (%)"));
            builder.append(" ");
            builder.append(doubleListToString(buffData.getProbabilitiesList()));
        }

        if (buffData.getHasCustomTraits()) {
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Custom Buff Trait"));
            builder.append(" ");
            builder.append(printTraits(buffData.getCustomTraitsList()));
        }

        if (buffData.hasApplyCondition()) {
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Apply Condition"));
            builder.append(" ");
            builder.append(printConditionData(buffData.getApplyCondition()));
        }

        final Set<BuffFields> requiredFields = BUFF_REQUIRED_FIELDS_MAP.get(type);
        if (requiredFields == null || requiredFields.isEmpty()) {
            return builder.toString();
        }
        if (requiredFields.contains(BUFF_FIELD_DOUBLE_VALUE) ||
                (requiredFields.contains(BUFF_FIELD_PERCENT_OPTION) && buffData.getIsGutsPercentBased())) {
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Value (%)"));
            builder.append(" ");
            builder.append(doubleListToString(buffData.getValuesList()));
            if (buffData.hasVariationData()) {
                builder.append(" ");
                builder.append(printVariationData(buffData.getVariationData()));
                builder.append(" ");
                builder.append(getTranslation(APPLICATION_SECTION, "Value (%)"));
                builder.append(" ");
                builder.append(doubleListToString(buffData.getAdditionsList()));
            }
        }
        if (requiredFields.contains(BUFF_FIELD_INT_VALUE) ||
                (requiredFields.contains(BUFF_FIELD_PERCENT_OPTION) && !buffData.getIsGutsPercentBased())) {
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Value"));
            builder.append(" ");
            builder.append(intListToString(buffData.getValuesList()));
            if (buffData.hasVariationData()) {
                builder.append(" ");
                builder.append(printVariationData(buffData.getVariationData()));
                builder.append(" ");
                builder.append(getTranslation(APPLICATION_SECTION, "Value"));
                builder.append(" ");
                builder.append(intListToString(buffData.getAdditionsList()));
            }
        }
        if (requiredFields.contains(BUFF_FIELD_STRING_VALUE)) {
            builder.append(" : ");
            builder.append(getTranslation(TRAIT_SECTION, buffData.getStringValue()));
        }
        if (requiredFields.contains(BUFF_FIELD_EFFECTS)) {
            builder.append(" : ");
            final List<String> effectStrings = buffData.getSubEffectsList()
                    .stream()
                    .map(DataPrinter::printEffectData)
                    .collect(Collectors.toList());
            builder.append(effectStrings);
        }
        if (requiredFields.contains(BUFF_FIELD_CLASS_ADV)) {
            final ClassAdvantageChangeAdditionalParams additionalParams = buffData.getClassAdvChangeAdditionalParams();
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Attack Affinity"));
            builder.append(" ");
            builder.append(getTranslation(CLASS_ADV_SECTION, additionalParams.getAttackMode().name()));
            if (additionalParams.getAttackMode() != CLASS_ADV_NO_CHANGE) {
                if (additionalParams.getCustomizeAttackModifier()) {
                    builder.append(" ");
                    builder.append(getTranslation(APPLICATION_SECTION, "Custom Affinity Value"));
                    builder.append(" ");
                    builder.append(additionalParams.getAttackAdv());
                }
                builder.append(" ");
                builder.append(getTranslation(APPLICATION_SECTION, "Target Class"));
                builder.append(" ");
                final List<String> affectedAtkClasses = additionalParams.getAttackModeAffectedClassesList()
                        .stream()
                        .map(s -> getTranslation(TRAIT_SECTION, s.name()))
                        .collect(Collectors.toList());
                builder.append(affectedAtkClasses);
            }
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Defense Affinity"));
            builder.append(" ");
            builder.append(getTranslation(CLASS_ADV_SECTION, additionalParams.getDefenseMode().name()));
            if (additionalParams.getDefenseMode() != CLASS_ADV_NO_CHANGE) {
                if (additionalParams.getCustomizeDefenseModifier()) {
                    builder.append(" ");
                    builder.append(getTranslation(APPLICATION_SECTION, "Custom Affinity Value"));
                    builder.append(" ");
                    builder.append(additionalParams.getDefenseAdv());
                }
                builder.append(" ");
                builder.append(getTranslation(APPLICATION_SECTION, "Target Class"));
                builder.append(" ");
                final List<String> affectedDefClasses = additionalParams.getDefenseModeAffectedClassesList()
                        .stream()
                        .map(s -> getTranslation(TRAIT_SECTION, s.name()))
                        .collect(Collectors.toList());
                builder.append(affectedDefClasses);
            }
        }
        if (requiredFields.contains(BUFF_FIELD_CARD_TYPE)) {
            builder.append(" : ");
            builder.append(getTranslation(COMMAND_CARD_TYPE_SECTION, buffData.getStringValue()));
        }

        return builder.toString();
    }

    public static String printEffectData(final EffectData effectData) {
        if (effectData == null || effectData.getType().isEmpty()) {
            return "";
        }
        final String type = effectData.getType();
        final StringBuilder builder = new StringBuilder();

        builder.append(getTranslation(EFFECT_SECTION, type));


        if (effectData.getProbabilitiesCount() > 0) {
            builder.append(" : ");
            if (effectData.getIsProbabilityOvercharged()) {
                builder.append("(OC) ");
            }
            builder.append(getTranslation(APPLICATION_SECTION, "Custom Probability (%)"));
            builder.append(" ");
            builder.append(doubleListToString(effectData.getProbabilitiesList()));
        }

        if (effectData.hasApplyCondition()) {
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Apply Condition"));
            builder.append(" ");
            builder.append(printConditionData(effectData.getApplyCondition()));
        }

        final Set<EffectFields> requiredFields = EFFECT_REQUIRED_FIELDS_MAP.get(effectData.getType());
        if (requiredFields == null || requiredFields.isEmpty()) {
            return builder.toString();
        }

        if (requiredFields.contains(EFFECT_FIELD_TARGET)) {
            builder.append(" : ");
            builder.append(getTranslation(TARGET_SECTION, effectData.getTarget().name()));
        }
        if (requiredFields.contains(EFFECT_FIELD_DOUBLE_VALUE) ||
                (requiredFields.contains(EFFECT_FIELD_HP_CHANGE) && effectData.getIsHpChangePercentBased())) {
            builder.append(" : ");
            if (effectData.getIsOverchargedEffect()) {
                builder.append("(OC) ");
            }
            builder.append(getTranslation(APPLICATION_SECTION, "Value (%)"));
            builder.append(" ");
            builder.append(doubleListToString(effectData.getValuesList()));
            if (effectData.hasVariationData()) {
                builder.append(" ");
                builder.append(printVariationData(effectData.getVariationData()));
                builder.append(" ");
                if (effectData.getIsAdditionOvercharged()) {
                    builder.append("(OC) ");
                }
                builder.append(getTranslation(APPLICATION_SECTION, "Value (%)"));
                builder.append(" ");
                builder.append(doubleListToString(effectData.getAdditionsList()));
            }
        }
        if (requiredFields.contains(EFFECT_FIELD_INT_VALUE) ||
                (requiredFields.contains(EFFECT_FIELD_HP_CHANGE) && !effectData.getIsHpChangePercentBased())) {
            builder.append(" : ");
            if (effectData.getIsOverchargedEffect()) {
                builder.append("(OC) ");
            }
            builder.append(getTranslation(APPLICATION_SECTION, "Value"));
            builder.append(" ");
            builder.append(intListToString(effectData.getValuesList()));
            if (effectData.hasVariationData()) {
                builder.append(" ");
                builder.append(printVariationData(effectData.getVariationData()));
                builder.append(" ");
                if (effectData.getIsAdditionOvercharged()) {
                    builder.append("(OC) ");
                }
                builder.append(getTranslation(APPLICATION_SECTION, "Value"));
                builder.append(" ");
                builder.append(intListToString(effectData.getAdditionsList()));
            }
        }
        if (requiredFields.contains(EFFECT_FIELD_NP_DAMAGE) && effectData.hasNpDamageAdditionalParams()) {
            final NpDamageAdditionalParams additionalParams = effectData.getNpDamageAdditionalParams();
            if (additionalParams.getIsNpDamageOverchargedEffect()) {
                builder.append(" ");
                builder.append(getTranslation(APPLICATION_SECTION, "Is NP Damage OC"));
                builder.append(" ");
                builder.append(getTranslation(APPLICATION_SECTION, "Value (%)"));
                builder.append(" ");
                builder.append(doubleListToString(additionalParams.getNpOverchargeDamageRateList()));
            }

            if (additionalParams.getIsNpIgnoreDefense()) {
                builder.append(" ");
                builder.append(getTranslation(APPLICATION_SECTION, "Ignore Defense"));
            }

            if (additionalParams.hasNpSpecificDamageCondition()) {
                builder.append(" ");
                builder.append(getTranslation(APPLICATION_SECTION, "Np Specific Damage"));
                builder.append(" ");
                builder.append(printConditionData(additionalParams.getNpSpecificDamageCondition()));
                builder.append(" ");
                if (additionalParams.getIsNpSpecificDamageOverchargedEffect()) {
                    builder.append("(OC) ");
                }
                builder.append(getTranslation(APPLICATION_SECTION, "Value (%)"));
                builder.append(" ");
                builder.append(doubleListToString(additionalParams.getNpSpecificDamageRateList()));
                if (additionalParams.hasNpSpecificDamageVariation()) {
                    builder.append(" ");
                    builder.append(printVariationData(additionalParams.getNpSpecificDamageVariation()));
                    builder.append(" ");
                    if (additionalParams.getIsNpSpecificDamageAdditionOvercharged()) {
                        builder.append("(OC) ");
                    }
                    builder.append(getTranslation(APPLICATION_SECTION, "Value (%)"));
                    builder.append(" ");
                    builder.append(doubleListToString(additionalParams.getNpSpecificDamageAdditionsList()));
                }
            }
        }
        if (requiredFields.contains(EFFECT_FIELD_GRANT_BUFF)) {
            builder.append(" : ");
            if (effectData.getIsOverchargedEffect()) {
                builder.append("(OC) ");
            }
            final List<String> buffs = effectData.getBuffDataList()
                    .stream()
                    .map(DataPrinter::printBuffData)
                    .collect(Collectors.toList());
            builder.append(buffs);
        }
        if (requiredFields.contains(EFFECT_FIELD_CARD_TYPE_SELECT)) {
            builder.append(" :");
            for (final CommandCardType cardType : effectData.getCardTypeSelectionsList()) {
                builder.append(" ");
                builder.append(getTranslation(COMMAND_CARD_TYPE_SECTION, cardType.name()));
            }
        }
        if (requiredFields.contains(EFFECT_FIELD_HP_CHANGE) && effectData.getIsLethal()) {
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Is lethal on HP drain"));
        }
        if (requiredFields.contains(EFFECT_FIELD_REMOVE_BUFF) && effectData.getRemoveFromStart()) {
            builder.append(" : ");
            builder.append(getTranslation(APPLICATION_SECTION, "Remove from earliest"));
        }
        if (requiredFields.contains(EFFECT_FIELD_RANDOM_SELECTION)) {
            builder.append(" : ");
            final List<String> effectStrings = effectData.getRandomEffectSelectionsList()
                    .stream()
                    .map(DataPrinter::printEffectData)
                    .collect(Collectors.toList());
            builder.append(effectStrings);
        }

        return builder.toString();
    }
}
