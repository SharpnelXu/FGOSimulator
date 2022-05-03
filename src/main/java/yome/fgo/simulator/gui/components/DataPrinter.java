package yome.fgo.simulator.gui.components;

import yome.fgo.data.proto.FgoStorageData.ConditionData;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_BUFF_TYPE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_CARD_TYPE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_CLASS_VALUE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_DOUBLE_VALUE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_INT_VALUE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_LIMITED_SUB_CONDITION;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_SERVANT;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_TARGET;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_TRAIT_VALUE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_FIELD_UNLIMITED_SUB_CONDITION;
import static yome.fgo.simulator.models.conditions.ConditionFactory.CONDITION_REQUIRED_FIELD_MAP;
import static yome.fgo.simulator.translation.TranslationManager.BUFF_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CONDITION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.SERVANT_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class DataPrinter {
    public static String printConditionData(final ConditionData conditionData) {
        if (conditionData == null || conditionData.getType().isEmpty()) {
            return "";
        }

        final String type = conditionData.getType();
        final StringBuilder builder = new StringBuilder();

        builder.append(getTranslation(CONDITION_SECTION, type));

        final Set<Integer> requiredFields = CONDITION_REQUIRED_FIELD_MAP.get(type);
        if (requiredFields == null || requiredFields.isEmpty()) {
            return builder.toString();
        }

        if (requiredFields.contains(CONDITION_FIELD_INT_VALUE)) {
            builder.append(" : ");
            builder.append(String.format("%d", (int) conditionData.getDoubleValue()));
        } else if (requiredFields.contains(CONDITION_FIELD_DOUBLE_VALUE)) {
            builder.append(" : ");
            builder.append(String.format("%.2f", conditionData.getDoubleValue() * 100));
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
        if (requiredFields.contains(CONDITION_FIELD_LIMITED_SUB_CONDITION) || requiredFields.contains(CONDITION_FIELD_UNLIMITED_SUB_CONDITION)) {
            builder.append(" : [");
            final List<String> subConditionStrings = conditionData.getSubConditionDataList()
                    .stream()
                    .map(DataPrinter::printConditionData)
                    .collect(Collectors.toList());

            if (!subConditionStrings.isEmpty()) {
                builder.append(String.join(", ", subConditionStrings));
            }
            builder.append("]");
        }

        return builder.toString();
    }
}