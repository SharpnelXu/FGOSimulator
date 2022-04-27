package yome.fgo.simulator.models.conditions;

import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.simulator.models.effects.buffs.Buff;

import java.util.stream.Collectors;

import static yome.fgo.simulator.models.conditions.Always.ALWAYS;
import static yome.fgo.simulator.models.conditions.BuffIsBuff.BUFF_IS_BUFF;
import static yome.fgo.simulator.models.conditions.BuffIsDebuff.BUFF_IS_DEBUFF;
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

        } else if (type.equalsIgnoreCase(BuffIsBuff.class.getSimpleName())) {
            return BUFF_IS_BUFF;

        } else if (type.equalsIgnoreCase(BuffIsDebuff.class.getSimpleName())) {
            return BUFF_IS_DEBUFF;

        } else if (type.equalsIgnoreCase(BuffTypeEquals.class.getSimpleName())) {
            try {
                return new BuffTypeEquals(Class.forName(Buff.class.getPackage().getName() + "." + conditionData.getValue()));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

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
        } else if (type.equalsIgnoreCase(StageHasTrait.class.getSimpleName())) {
            return new StageHasTrait(conditionData.getValue());

        } else if (type.equalsIgnoreCase(TargetsHaveBuff.class.getSimpleName())) {
            try {
                return TargetsHaveBuff.builder()
                        .target(conditionData.getTarget())
                        .targetBuffType(Class.forName(Buff.class.getPackage().getName() + "." + conditionData.getValue()))
                        .build();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        } else if (type.equalsIgnoreCase(TargetsHaveClass.class.getSimpleName())) {
            return TargetsHaveClass.builder()
                    .target(conditionData.getTarget())
                    .targetClass(FateClass.valueOf(conditionData.getValue()))
                    .build();

        } else if (type.equalsIgnoreCase(TargetsHaveTrait.class.getSimpleName())) {
            return TargetsHaveTrait.builder()
                    .target(conditionData.getTarget())
                    .trait(conditionData.getValue())
                    .build();

        } else if (type.equalsIgnoreCase(TargetsReceivedInstantDeath.class.getSimpleName())) {
            return new TargetsReceivedInstantDeath(conditionData.getTarget());
        }

        throw new UnsupportedOperationException("Unsupported condition: " + type);
    }
}
