package yome.fgo.simulator.models.effects.buffs;

import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.HpVariedBuffAdditionalParams;
import yome.fgo.simulator.models.effects.EffectFactory;

import java.util.List;

import static yome.fgo.simulator.models.conditions.ConditionFactory.buildCondition;

public class BuffFactory {
    public static Buff buildBuff(final BuffData buffData, final int level) {
        final String type = buffData.getType();

        if (type.equalsIgnoreCase(AttackBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(AttackBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(BuffChanceBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(BuffChanceBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(BuffRemovalResist.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(BuffRemovalResist.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(BuffSpecificAttackBuff.class.getSimpleName())) {
            final BuffSpecificAttackBuff.BuffSpecificAttackBuffBuilder<?, ?> builder;
            try {
                builder = BuffSpecificAttackBuff.builder()
                        .targetBuff(Class.forName(Buff.class.getPackage().getName() + "." + buffData.getStringValue()));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            return setCommonBuffParams(setValuedBuffParams(builder, buffData, level), buffData);

        } else if (type.equalsIgnoreCase(BurningLove.class.getSimpleName())) {
                return setCommonBuffParams(BurningLove.builder(), buffData);

        } else if (type.equalsIgnoreCase(CardTypeChange.class.getSimpleName())) {
            return setCommonBuffParams(
                    CardTypeChange.builder().commandCardType(CommandCardType.valueOf(buffData.getStringValue().toUpperCase())),
                    buffData
            );

        } else if (type.equalsIgnoreCase(Charm.class.getSimpleName())) {
            return setCommonBuffParams(Charm.builder(), buffData);

        } else if (type.equalsIgnoreCase(CommandCardBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(CommandCardBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(CommandCardResist.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(CommandCardResist.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(CriticalChanceResist.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(CriticalChanceResist.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(CriticalDamageBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(CriticalDamageBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(CriticalStarGenerationBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(CriticalStarGenerationBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(CriticalStarWeightBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(CriticalStarWeightBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(DamageAdditionBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(DamageAdditionBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(DamageReductionBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(DamageReductionBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(DeathResist.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(DeathResist.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(DebuffChanceBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(DebuffChanceBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(DebuffResist.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(DebuffResist.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(DefenseBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(DefenseBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(DefNpGenerationBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(DefNpGenerationBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(EndOfTurnEffect.class.getSimpleName())) {
            return setCommonBuffParams(setEffectActivatingBuffParams(EndOfTurnEffect.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(Evade.class.getSimpleName())) {
            return setCommonBuffParams(Evade.builder(), buffData);

        } else if (type.equalsIgnoreCase(GrantTrait.class.getSimpleName())) {
            return setCommonBuffParams(GrantTrait.builder().trait(buffData.getStringValue()), buffData);

        } else if (type.equalsIgnoreCase(Guts.class.getSimpleName())) {
            final int guts = (int) getValueFromListForLevel(buffData.getValuesList(), level);
            if (guts <= 0) {
                throw new IllegalArgumentException("Guts have non positive value");
            }

            return setCommonBuffParams(Guts.builder().gutsLeft(guts), buffData);

        } else if (type.equalsIgnoreCase(HpVariedBuffChanceBuff.class.getSimpleName())) {
            if (!buffData.hasHpVariedBuffAdditionalParams()) {
                throw new IllegalArgumentException("No available params to work with");
            }

            final HpVariedBuffAdditionalParams additionalParams = buffData.getHpVariedBuffAdditionalParams();

            return setCommonBuffParams(
                    setValuedBuffParams(
                            HpVariedBuffChanceBuff.builder()
                                    .maxHpPercent(getValueFromListForLevel(additionalParams.getMaxHpPercentList(), level))
                                    .minHpPercent(getValueFromListForLevel(additionalParams.getMinHpPercentList(), level))
                                    .baseValue(getValueFromListForLevel(additionalParams.getBaseValueList(), level)),
                            buffData,
                            level
                    ),
                    buffData
            );

        } else if (type.equalsIgnoreCase(IgnoreDefenceBuff.class.getSimpleName())) {
            return setCommonBuffParams(IgnoreDefenceBuff.builder(), buffData);

        } else if (type.equalsIgnoreCase(IgnoreInvincible.class.getSimpleName())) {
            return setCommonBuffParams(IgnoreInvincible.builder(), buffData);

        } else if (type.equalsIgnoreCase(Invincible.class.getSimpleName())) {
            return setCommonBuffParams(Invincible.builder(), buffData);

        } else if (type.equalsIgnoreCase(MaxHpBuff.class.getSimpleName())) {
            final MaxHpBuff.MaxHpBuffBuilder<?, ?> builder = MaxHpBuff.builder();
            final int change;
            if (buffData.getValuesCount() >= level) {
                change = (int) buffData.getValues(level - 1);
            } else {
                change = (int) buffData.getValues(0);
            }

            return setCommonBuffParams(builder.change(change), buffData);

        } else if (type.equalsIgnoreCase(NpCardTypeChange.class.getSimpleName())) {
            return setCommonBuffParams(
                    NpCardTypeChange.builder().commandCardType(CommandCardType.valueOf(buffData.getStringValue().toUpperCase())),
                    buffData
            );

        } else if (type.equalsIgnoreCase(NpDamageBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(NpDamageBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(NpGenerationBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(NpGenerationBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(NpSeal.class.getSimpleName())) {
            return setCommonBuffParams(NpSeal.builder(), buffData);

        } else if (type.equalsIgnoreCase(PercentAttackBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(PercentAttackBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(PercentDefenseBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(PercentDefenseBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(PostAttackEffect.class.getSimpleName())) {
            return setCommonBuffParams(setEffectActivatingBuffParams(PostAttackEffect.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(ReceivedBuffChanceBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(ReceivedBuffChanceBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(SkillSeal.class.getSimpleName())) {
            return setCommonBuffParams(SkillSeal.builder(), buffData);

        } else if (type.equalsIgnoreCase(SpecialInvincible.class.getSimpleName())) {
            return setCommonBuffParams(SpecialInvincible.builder(), buffData);

        } else if (type.equalsIgnoreCase(SpecificAttackBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(SpecificAttackBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(SpecificDefenseBuff.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(SpecificDefenseBuff.builder(), buffData, level), buffData);

        } else if (type.equalsIgnoreCase(Stun.class.getSimpleName())) {
            return setCommonBuffParams(Stun.builder(), buffData);

        } else if (type.equalsIgnoreCase(SureHit.class.getSimpleName())) {
            return setCommonBuffParams(SureHit.builder(), buffData);

        } else if (type.equalsIgnoreCase(Taunt.class.getSimpleName())) {
            return setCommonBuffParams(setValuedBuffParams(Taunt.builder(), buffData, level), buffData);
        }

        throw new UnsupportedOperationException("Unsupported buff type: " + type);
    }

    public static Buff setCommonBuffParams(
            final Buff.BuffBuilder<?, ?> builder,
            final BuffData buffData
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

        builder.forceBuff(buffData.getForceBuff());
        builder.irremovable(buffData.getIrremovable());
        builder.forceStackable(buffData.getForceStackable());

        return builder.build();
    }

    public static ValuedBuff.ValuedBuffBuilder<?, ?> setValuedBuffParams(
            final ValuedBuff.ValuedBuffBuilder<?, ?> builder,
            final BuffData buffData,
            final int level
    ) {
        return  builder.value(getValueFromListForLevel(buffData.getValuesList(), level));
    }

    public static double getValueFromListForLevel(final List<Double> values, final int level) {
        if (values.size() >= level) {
            return values.get(level - 1);
        } else {
            return values.get(0);
        }
    }

    public static EffectActivatingBuff.EffectActivatingBuffBuilder<?, ?> setEffectActivatingBuffParams(
            final EffectActivatingBuff.EffectActivatingBuffBuilder<?, ?> builder,
            final BuffData buffData,
            final int level
    ) {
        return builder.effects(EffectFactory.buildEffects(buffData.getSubEffectsList(), level));
    }
}
