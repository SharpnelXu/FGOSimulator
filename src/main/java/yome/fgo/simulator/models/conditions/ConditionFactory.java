package yome.fgo.simulator.models.conditions;

import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.simulator.models.conditions.Condition.ConditionType;
import yome.fgo.simulator.models.effects.buffs.BuffType;

import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.simulator.models.conditions.Always.ALWAYS;
import static yome.fgo.simulator.models.conditions.BackupServantsExist.BACKUP_SERVANTS_EXIST;
import static yome.fgo.simulator.models.conditions.BuffRemovable.BUFF_REMOVABLE;
import static yome.fgo.simulator.models.conditions.IsCriticalStrike.IS_CRITICAL_STRIKE;
import static yome.fgo.simulator.models.conditions.Never.NEVER;
import static yome.fgo.simulator.models.conditions.NpCard.NP_CARD;
import static yome.fgo.simulator.models.conditions.ServantsExplodable.EXPLOOOOOOOOOSION;

public class ConditionFactory {
    public static Condition buildCondition(final ConditionData conditionData) {
        return switch (ConditionType.ofType(conditionData.getType())) {
            case ALWAYS -> ALWAYS;
            case NEVER -> NEVER;
            case AND -> new And(buildSubConditions(conditionData));
            case OR -> new Or(buildSubConditions(conditionData));
            case NOT -> new Not(buildCondition(conditionData.getSubConditionData(0)));
            case CARD_TYPE_EQUALS -> CardTypeEquals.get(CommandCardType.valueOf(conditionData.getValue().toUpperCase()));
            case IS_CRITICAL_STRIKE -> IS_CRITICAL_STRIKE;
            case NP_CARD -> NP_CARD;
            case BUFF_TYPE_EQUALS -> new BuffTypeEquals(BuffType.ofType(conditionData.getValue()));
            case BUFF_HAS_TRAIT -> new BuffHasTrait(conditionData.getValue());
            case BUFF_REMOVABLE -> BUFF_REMOVABLE;
            case TARGETS_HAVE_CLASS -> TargetsHaveClass.builder()
                    .target(conditionData.getTarget())
                    .targetClass(FateClass.valueOf(conditionData.getValue()))
                    .build();
            case TARGETS_HAVE_BUFF -> TargetsHaveBuff.builder()
                    .target(conditionData.getTarget())
                    .buffMatchCondition(buildCondition(conditionData.getSubConditionData(0)))
                    .build();
            case TARGETS_HAVE_TRAIT -> TargetsHaveTrait.builder()
                    .target(conditionData.getTarget())
                    .trait(conditionData.getValue())
                    .build();
            case TARGETS_CONTAINS_SPECIFIC_SERVANT -> TargetsContainsSpecificServant.builder()
                    .target(conditionData.getTarget())
                    .servantId(conditionData.getValue())
                    .build();
            case RARITY_AT_LEAST -> new RarityAtLeast((int) conditionData.getDoubleValue(), conditionData.getTarget());
            case SERVANTS_WITH_CE -> ServantsWithCE.builder()
                    .target(conditionData.getTarget())
                    .ceId(conditionData.getValue())
                    .build();
            case TARGETS_RECEIVED_INSTANT_DEATH -> new TargetsReceivedInstantDeath(conditionData.getTarget());
            case STAGE_HAS_TRAIT -> new StageHasTrait(conditionData.getValue());
            case CRIT_STAR_AT_LEAST -> new CritStarAtLeast((int) conditionData.getDoubleValue());
            case NP_AT_LEAST -> new NpAtLeast(conditionData.getDoubleValue());
            case NP_GAUGE_AT_LEAST -> new NpGaugeAtLeast((int) conditionData.getDoubleValue());
            case HP_AT_LEAST -> new HpAtLeast((int) conditionData.getDoubleValue());
            case HP_PERCENT_AT_MOST -> new HpPercentAtMost(conditionData.getDoubleValue());

            case SERVANTS_EXPLODABLE -> EXPLOOOOOOOOOSION;
            case BACKUP_SERVANTS_EXIST -> BACKUP_SERVANTS_EXIST;
        };
    }

    private static List<Condition> buildSubConditions(final ConditionData conditionData) {
        return conditionData.getSubConditionDataList()
                .stream()
                .map(ConditionFactory::buildCondition)
                .collect(Collectors.toList());
    }
}
