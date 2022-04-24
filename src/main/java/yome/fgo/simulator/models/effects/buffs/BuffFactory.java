package yome.fgo.simulator.models.effects.buffs;

import yome.fgo.data.proto.FgoStorageData.BuffData;

import static yome.fgo.simulator.models.conditions.ConditionFactory.buildCondition;

public class BuffFactory {
    public static Buff buildBuff(final BuffData buffData, final int level) {
        final String type = buffData.getType();

        if (type.equalsIgnoreCase(AttackBuff.class.getSimpleName())) {
            return setCommonBuffParams(AttackBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Charm.class.getSimpleName())) {
            return setCommonBuffParams(Charm.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CommandCardBuff.class.getSimpleName())) {
            return setCommonBuffParams(CommandCardBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CommandCardResist.class.getSimpleName())) {
            return setCommonBuffParams(CommandCardResist.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CriticalDamageBuff.class.getSimpleName())) {
            return setCommonBuffParams(CriticalDamageBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CriticalStarGenerationBuff.class.getSimpleName())) {
            return setCommonBuffParams(CriticalStarGenerationBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(DamageAdditionBuff.class.getSimpleName())) {
            return setCommonBuffParams(DamageAdditionBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(DamageReductionBuff.class.getSimpleName())) {
            return setCommonBuffParams(DamageReductionBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(DefenseBuff.class.getSimpleName())) {
            return setCommonBuffParams(DefenseBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Evade.class.getSimpleName())) {
            return setCommonBuffParams(Evade.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(GrantTrait.class.getSimpleName())) {
            return setCommonBuffParams(GrantTrait.builder().trait(buffData.getStringValue()), buffData, level);

        } else if (type.equalsIgnoreCase(NpGenerationBuff.class.getSimpleName())) {
            return setCommonBuffParams(NpGenerationBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(NpDamageBuff.class.getSimpleName())) {
            return setCommonBuffParams(NpDamageBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(PercentAttackBuff.class.getSimpleName())) {
            return setCommonBuffParams(PercentAttackBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(PercentDefenseBuff.class.getSimpleName())) {
            return setCommonBuffParams(PercentDefenseBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(SpecificAttackBuff.class.getSimpleName())) {
            return setCommonBuffParams(SpecificAttackBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(SpecificDefenseBuff.class.getSimpleName())) {
            return setCommonBuffParams(SpecificDefenseBuff.builder(), buffData, level);
        }

        throw new UnsupportedOperationException("Unsupported buff type: " + type);
    }

    public static Buff setCommonBuffParams(
            final Buff.BuffBuilder<?, ?> builder,
            final BuffData buffData,
            final int level
    ) {
        if (buffData.getNumTurnsActive() > 0) {
            builder.numTurnsActive(buffData.getNumTurnsActive());
        }
        if (buffData.getNumTimesActive() > 0) {
            builder.numTimesActive(buffData.getNumTimesActive());
        }
        if (buffData.hasApplyCondition()) {
            builder.condition(buildCondition(buffData.getApplyCondition()));
        }
        if (buffData.getValuesCount() >= level) {
            builder.value(buffData.getValues(level - 1));
        } else if (buffData.getValuesCount() == 1) {
            builder.value(buffData.getValues(0));
        }

        return builder.build();
    }
}
