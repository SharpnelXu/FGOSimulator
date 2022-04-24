package yome.fgo.simulator.models.conditions;

import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.simulator.models.effects.buffs.Buff;

import java.util.stream.Collectors;

import static yome.fgo.simulator.models.conditions.Always.ALWAYS;
import static yome.fgo.simulator.models.conditions.Never.NEVER;
import static yome.fgo.simulator.models.conditions.NpCard.NP_CARD;

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

        } else if (type.equalsIgnoreCase(CardTypeEquals.class.getSimpleName())) {
            return CardTypeEquals.get(CommandCardType.valueOf(conditionData.getValue().toUpperCase()));

        } else if (type.equalsIgnoreCase(Never.class.getSimpleName())) {
            return NEVER;

        } else if (type.equalsIgnoreCase(Not.class.getSimpleName())) {
            if (conditionData.getSubConditionDataCount() != 1) {
                throw new IllegalArgumentException("Not should only have one argument");
            }
            return new Not(buildCondition(conditionData.getSubConditionData(0)));

        } else if (type.equalsIgnoreCase(NpCard.class.getSimpleName())) {
            return NP_CARD;

        } else if (type.equalsIgnoreCase(Or.class.getSimpleName())) {
            return new Or(conditionData.getSubConditionDataList()
                                  .stream()
                                  .map(ConditionFactory::buildCondition)
                                  .collect(Collectors.toList()));

        } else if (type.equalsIgnoreCase(TargetsHaveBuff.class.getSimpleName())) {
            try {
                return TargetsHaveBuff.builder()
                        .target(conditionData.getTarget())
                        .targetBuff(Class.forName(Buff.class.getPackage().getName() + "." + conditionData.getValue()))
                        .build();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        } else if (type.equalsIgnoreCase(TargetsHaveTrait.class.getSimpleName())) {
            return TargetsHaveTrait.builder()
                    .target(conditionData.getTarget())
                    .trait(conditionData.getValue())
                    .build();
        }

        throw new UnsupportedOperationException("Unsupported condition: " + type);
    }
}
