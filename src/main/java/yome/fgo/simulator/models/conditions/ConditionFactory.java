package yome.fgo.simulator.models.conditions;

import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;

import java.util.stream.Collectors;

import static yome.fgo.simulator.models.conditions.Always.ALWAYS;
import static yome.fgo.simulator.models.conditions.Never.NEVER;

public class ConditionFactory {
    public static Condition buildCondition(final ConditionData conditionData) {
        final String type = conditionData.getType();
        if (type.equalsIgnoreCase(Always.class.getSimpleName())) {
            return ALWAYS;

        } else if (type.equalsIgnoreCase(And.class.getSimpleName())) {
            return new And(conditionData.getSubConditionDataList()
                                   .stream()
                                   .map(ConditionFactory::buildCondition)
                                   .collect(Collectors.toList()));

        } else if (type.equalsIgnoreCase(CommandCardTypeEquals.class.getSimpleName())) {
            return CommandCardTypeEquals.get(CommandCardType.valueOf(conditionData.getValue().toUpperCase()));

        } else if (type.equalsIgnoreCase(Never.class.getSimpleName())) {
            return NEVER;

        } else if (type.equalsIgnoreCase(Or.class.getSimpleName())) {
            return new Or(conditionData.getSubConditionDataList()
                                  .stream()
                                  .map(ConditionFactory::buildCondition)
                                  .collect(Collectors.toList()));

        } else if (type.equalsIgnoreCase(TargetsHaveTrait.class.getSimpleName())) {
            return TargetsHaveTrait.builder()
                    .target(conditionData.getTarget())
                    .trait(conditionData.getValue())
                    .build();
        }

        throw new UnsupportedOperationException("Unsupported condition: " + type);
    }
}
