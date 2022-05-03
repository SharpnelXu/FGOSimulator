package yome.fgo.simulator.models.conditions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.simulator.models.effects.buffs.Buff;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static yome.fgo.simulator.models.conditions.Always.ALWAYS;
import static yome.fgo.simulator.models.conditions.BuffRemovable.BUFF_REMOVABLE;
import static yome.fgo.simulator.models.conditions.IsCriticalStrike.IS_CRITICAL_STRIKE;
import static yome.fgo.simulator.models.conditions.Never.NEVER;
import static yome.fgo.simulator.models.conditions.NpCard.NP_CARD;
import static yome.fgo.simulator.models.conditions.ServantsExplodable.EXPLOOOOOOOOOSION;

public class ConditionFactory {

    public static final Map<String, Set<Integer>> CONDITION_REQUIRED_FIELD_MAP = buildRequiredConditionFieldsMap();
    public static final int CONDITION_FIELD_INT_VALUE = 1;
    public static final int CONDITION_FIELD_DOUBLE_VALUE = 2;
    public static final int CONDITION_FIELD_TRAIT_VALUE = 3;
    public static final int CONDITION_FIELD_SERVANT = 10;
    public static final int CONDITION_FIELD_BUFF_TYPE = 4;
    public static final int CONDITION_FIELD_CARD_TYPE = 5;
    public static final int CONDITION_FIELD_CLASS_VALUE = 6;
    public static final int CONDITION_FIELD_TARGET = 7;
    public static final int CONDITION_FIELD_UNLIMITED_SUB_CONDITION = 8;
    public static final int CONDITION_FIELD_LIMITED_SUB_CONDITION = 9;

    public static Condition buildCondition(final ConditionData conditionData) {
        final String type = conditionData.getType();
        if (type.equalsIgnoreCase(Always.class.getSimpleName())) {
            return ALWAYS;

        } else if (type.equalsIgnoreCase(And.class.getSimpleName())) {
            return new And(conditionData.getSubConditionDataList()
                                   .stream()
                                   .map(ConditionFactory::buildCondition)
                                   .collect(Collectors.toList()));

        } else if (type.equalsIgnoreCase(BuffHasTrait.class.getSimpleName())) {
            return new BuffHasTrait(conditionData.getValue());

        } else if (type.equalsIgnoreCase(BuffRemovable.class.getSimpleName())) {
            return BUFF_REMOVABLE;

        } else if (type.equalsIgnoreCase(BuffTypeEquals.class.getSimpleName())) {
            try {
                return new BuffTypeEquals(Class.forName(Buff.class.getPackage()
                                                                .getName() + "." + conditionData.getValue()));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        } else if (type.equalsIgnoreCase(CardTypeEquals.class.getSimpleName())) {
            return CardTypeEquals.get(CommandCardType.valueOf(conditionData.getValue().toUpperCase()));

        } else if (type.equalsIgnoreCase(CritStarAtLeast.class.getSimpleName())) {
            return new CritStarAtLeast((int) conditionData.getDoubleValue());

        } else if (type.equalsIgnoreCase(HpAtLeast.class.getSimpleName())) {
            return new HpAtLeast((int) conditionData.getDoubleValue());

        } else if (type.equalsIgnoreCase(HpPercentAtMost.class.getSimpleName())) {
            return new HpPercentAtMost(conditionData.getDoubleValue());

        } else if (type.equalsIgnoreCase(IsCriticalStrike.class.getSimpleName())) {
            return IS_CRITICAL_STRIKE;

        } else if (type.equalsIgnoreCase(Never.class.getSimpleName())) {
            return NEVER;

        } else if (type.equalsIgnoreCase(Not.class.getSimpleName())) {
            if (conditionData.getSubConditionDataCount() != 1) {
                throw new IllegalArgumentException("Not should only have one argument");
            }
            return new Not(buildCondition(conditionData.getSubConditionData(0)));

        } else if (type.equalsIgnoreCase(NpAtLeast.class.getSimpleName())) {
            return new NpAtLeast(conditionData.getDoubleValue());

        } else if (type.equalsIgnoreCase(NpCard.class.getSimpleName())) {
            return NP_CARD;

        } else if (type.equalsIgnoreCase(Or.class.getSimpleName())) {
            return new Or(conditionData.getSubConditionDataList()
                                  .stream()
                                  .map(ConditionFactory::buildCondition)
                                  .collect(Collectors.toList()));

        } else if (type.equalsIgnoreCase(RarityAtLeast.class.getSimpleName())) {
            return new RarityAtLeast((int) conditionData.getDoubleValue(), conditionData.getTarget());

        } else if (type.equalsIgnoreCase(StageHasTrait.class.getSimpleName())) {
            return new StageHasTrait(conditionData.getValue());

        } else if (type.equalsIgnoreCase(ServantsExplodable.class.getSimpleName())) {
            return EXPLOOOOOOOOOSION;

        } else if (type.equalsIgnoreCase(TargetsContainsSpecificServant.class.getSimpleName())) {
            return TargetsContainsSpecificServant.builder()
                    .target(conditionData.getTarget())
                    .servantId(conditionData.getValue())
                    .build();

        } else if (type.equalsIgnoreCase(TargetsHaveBuff.class.getSimpleName())) {
            if (conditionData.getSubConditionDataCount() != 1) {
                throw new IllegalArgumentException("TargetsHaveBuff should only have one argument");
            }

            return TargetsHaveBuff.builder()
                    .target(conditionData.getTarget())
                    .buffMatchCondition(buildCondition(conditionData.getSubConditionData(0)))
                    .build();

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

    public static Map<String, Set<Integer>> buildRequiredConditionFieldsMap() {
        final ImmutableMap.Builder<String, Set<Integer>> builder = ImmutableSortedMap.naturalOrder();
        builder.put(Always.class.getSimpleName(), ImmutableSet.of());

        builder.put(And.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_UNLIMITED_SUB_CONDITION));

        builder.put(BuffHasTrait.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_TRAIT_VALUE));

        builder.put(BuffRemovable.class.getSimpleName(), ImmutableSet.of());

        builder.put(BuffTypeEquals.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_BUFF_TYPE));

        builder.put(CardTypeEquals.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_CARD_TYPE));

        builder.put(CritStarAtLeast.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_INT_VALUE));

        builder.put(HpAtLeast.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_INT_VALUE));

        builder.put(HpPercentAtMost.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_DOUBLE_VALUE));

        builder.put(IsCriticalStrike.class.getSimpleName(), ImmutableSet.of());

        builder.put(Never.class.getSimpleName(), ImmutableSet.of());

        builder.put(Not.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_LIMITED_SUB_CONDITION));

        builder.put(NpCard.class.getSimpleName(), ImmutableSet.of());

        builder.put(NpAtLeast.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_DOUBLE_VALUE));

        builder.put(Or.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_UNLIMITED_SUB_CONDITION));

        builder.put(RarityAtLeast.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_DOUBLE_VALUE, CONDITION_FIELD_TARGET));

        builder.put(StageHasTrait.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_TRAIT_VALUE));

        builder.put(ServantsExplodable.class.getSimpleName(), ImmutableSet.of());

        builder.put(TargetsContainsSpecificServant.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_TARGET,
                                                                                          CONDITION_FIELD_SERVANT
        ));

        builder.put(TargetsHaveBuff.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_TARGET, CONDITION_FIELD_LIMITED_SUB_CONDITION));

        builder.put(TargetsHaveClass.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_TARGET, CONDITION_FIELD_CLASS_VALUE));

        builder.put(TargetsHaveTrait.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_TARGET, CONDITION_FIELD_TRAIT_VALUE));

        builder.put(TargetsReceivedInstantDeath.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_TARGET));

        return builder.build();
    }
}
