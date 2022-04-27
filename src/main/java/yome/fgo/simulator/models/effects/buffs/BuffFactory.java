package yome.fgo.simulator.models.effects.buffs;

import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.HpVariedBuffAdditionalParams;
import yome.fgo.simulator.models.effects.EffectFactory;
import yome.fgo.simulator.models.effects.GrantBuff;

import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.Target.SELF;
import static yome.fgo.simulator.models.conditions.ConditionFactory.buildCondition;

public class BuffFactory {
    public static Buff buildBuff(final BuffData buffData, final int level) {
        final String type = buffData.getType();

        if (type.equalsIgnoreCase(AttackBuff.class.getSimpleName())) {
            return setValuedBuffParams(AttackBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(BuffChanceBuff.class.getSimpleName())) {
            return setValuedBuffParams(BuffChanceBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(BuffRemovalResist.class.getSimpleName())) {
            return setValuedBuffParams(BuffRemovalResist.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(BuffSpecificAttackBuff.class.getSimpleName())) {
            final BuffSpecificAttackBuff.BuffSpecificAttackBuffBuilder<?, ?> builder;
            try {
                builder = BuffSpecificAttackBuff.builder()
                        .targetBuff(Class.forName(Buff.class.getPackage().getName() + "." + buffData.getStringValue()))
                        .target(buffData.getTarget());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            return setValuedBuffParams(builder, buffData, level);

        } else if (type.equalsIgnoreCase(Burn.class.getSimpleName())) {
            return setValuedBuffParams(Burn.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(BurnEffectivenessUp.class.getSimpleName())) {
            return setValuedBuffParams(BurnEffectivenessUp.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(BurningLove.class.getSimpleName())) {
            return setCommonBuffParams(BurningLove.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CardTypeChange.class.getSimpleName())) {
            return setCommonBuffParams(
                    CardTypeChange.builder()
                            .commandCardType(CommandCardType.valueOf(buffData.getStringValue().toUpperCase())),
                    buffData,
                    level
            );

        } else if (type.equalsIgnoreCase(Charm.class.getSimpleName())) {
            return setCommonBuffParams(Charm.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CommandCardBuff.class.getSimpleName())) {
            return setValuedBuffParams(CommandCardBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CommandCardResist.class.getSimpleName())) {
            return setValuedBuffParams(CommandCardResist.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CriticalChanceResist.class.getSimpleName())) {
            return setValuedBuffParams(CriticalChanceResist.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CriticalDamageBuff.class.getSimpleName())) {
            return setValuedBuffParams(CriticalDamageBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CriticalRateBuff.class.getSimpleName())) {
            return setValuedBuffParams(CriticalRateBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CriticalStarGenerationBuff.class.getSimpleName())) {
            return setValuedBuffParams(CriticalStarGenerationBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CriticalStarWeightBuff.class.getSimpleName())) {
            return setValuedBuffParams(CriticalStarWeightBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Curse.class.getSimpleName())) {
            return setValuedBuffParams(Curse.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CurseEffectivenessUp.class.getSimpleName())) {
            return setValuedBuffParams(CurseEffectivenessUp.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(DamageAdditionBuff.class.getSimpleName())) {
            return setValuedBuffParams(DamageAdditionBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(DamageReductionBuff.class.getSimpleName())) {
            return setValuedBuffParams(DamageReductionBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(DeathChanceBuff.class.getSimpleName())) {
            return setValuedBuffParams(DeathChanceBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(DeathResist.class.getSimpleName())) {
            return setValuedBuffParams(DeathResist.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(DebuffChanceBuff.class.getSimpleName())) {
            return setValuedBuffParams(DebuffChanceBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(DebuffResist.class.getSimpleName())) {
            return setValuedBuffParams(DebuffResist.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(DefenseBuff.class.getSimpleName())) {
            return setValuedBuffParams(DefenseBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(DefNpGenerationBuff.class.getSimpleName())) {
            return setValuedBuffParams(DefNpGenerationBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(DelayedEffect.class.getSimpleName())) {
            return setEffectActivatingBuffParams(DelayedEffect.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(EndOfTurnEffect.class.getSimpleName())) {
            return setEffectActivatingBuffParams(EndOfTurnEffect.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Evade.class.getSimpleName())) {
            return setCommonBuffParams(Evade.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(GrantTrait.class.getSimpleName())) {
            return setCommonBuffParams(GrantTrait.builder().trait(buffData.getStringValue()), buffData, level);

        } else if (type.equalsIgnoreCase(Guts.class.getSimpleName())) {
            final int guts = (int) getValueFromListForLevel(buffData.getValuesList(), level);
            if (guts <= 0) {
                throw new IllegalArgumentException("Guts have non positive value");
            }

            return setCommonBuffParams(Guts.builder().gutsLeft(guts), buffData, level);

        } else if (type.equalsIgnoreCase(HealEffectivenessBuff.class.getSimpleName())) {
            return setValuedBuffParams(HealEffectivenessBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(HpVariedAttackBuff.class.getSimpleName())) {
            if (!buffData.hasHpVariedBuffAdditionalParams()) {
                throw new IllegalArgumentException("No available params to work with");
            }

            final HpVariedBuffAdditionalParams additionalParams = buffData.getHpVariedBuffAdditionalParams();

            return setValuedBuffParams(
                    HpVariedAttackBuff.builder()
                            .maxHpPercent(getValueFromListForLevel(additionalParams.getMaxHpPercentList(), level))
                            .minHpPercent(getValueFromListForLevel(additionalParams.getMinHpPercentList(), level))
                            .baseValue(getValueFromListForLevel(additionalParams.getBaseValueList(), level)),
                    buffData,
                    level
            );

        } else if (type.equalsIgnoreCase(HpVariedBuffChanceBuff.class.getSimpleName())) {
            if (!buffData.hasHpVariedBuffAdditionalParams()) {
                throw new IllegalArgumentException("No available params to work with");
            }

            final HpVariedBuffAdditionalParams additionalParams = buffData.getHpVariedBuffAdditionalParams();

            return setValuedBuffParams(
                    HpVariedBuffChanceBuff.builder()
                            .maxHpPercent(getValueFromListForLevel(additionalParams.getMaxHpPercentList(), level))
                            .minHpPercent(getValueFromListForLevel(additionalParams.getMinHpPercentList(), level))
                            .baseValue(getValueFromListForLevel(additionalParams.getBaseValueList(), level)),
                    buffData,
                    level
            );

        } else if (type.equalsIgnoreCase(IgnoreDefenceBuff.class.getSimpleName())) {
            return setCommonBuffParams(IgnoreDefenceBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(IgnoreInvincible.class.getSimpleName())) {
            return setCommonBuffParams(IgnoreInvincible.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Invincible.class.getSimpleName())) {
            return setCommonBuffParams(Invincible.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(MaxHpBuff.class.getSimpleName())) {
            final MaxHpBuff.MaxHpBuffBuilder<?, ?> builder = MaxHpBuff.builder();
            final int change = (int) getValueFromListForLevel(buffData.getValuesList(), level);

            return setCommonBuffParams(builder.change(change), buffData, level);

        } else if (type.equalsIgnoreCase(NpCardTypeChange.class.getSimpleName())) {
            return setCommonBuffParams(
                    NpCardTypeChange.builder()
                            .commandCardType(CommandCardType.valueOf(buffData.getStringValue().toUpperCase())),
                    buffData,
                    level
            );

        } else if (type.equalsIgnoreCase(NpDamageBuff.class.getSimpleName())) {
            return setValuedBuffParams(NpDamageBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(NpGenerationBuff.class.getSimpleName())) {
            return setValuedBuffParams(NpGenerationBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(NpSeal.class.getSimpleName())) {
            return setCommonBuffParams(NpSeal.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(PercentAttackBuff.class.getSimpleName())) {
            return setValuedBuffParams(PercentAttackBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(PercentDefenseBuff.class.getSimpleName())) {
            return setValuedBuffParams(PercentDefenseBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Poison.class.getSimpleName())) {
            return setValuedBuffParams(Poison.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(PoisonEffectivenessUp.class.getSimpleName())) {
            return setValuedBuffParams(PoisonEffectivenessUp.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(PostAttackEffect.class.getSimpleName())) {
            return setEffectActivatingBuffParams(PostAttackEffect.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(ReceivedBuffChanceBuff.class.getSimpleName())) {
            return setValuedBuffParams(ReceivedBuffChanceBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(SkillSeal.class.getSimpleName())) {
            return setCommonBuffParams(SkillSeal.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(SpecialInvincible.class.getSimpleName())) {
            return setCommonBuffParams(SpecialInvincible.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(SpecificAttackBuff.class.getSimpleName())) {
            return setValuedBuffParams(SpecificAttackBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(SpecificDefenseBuff.class.getSimpleName())) {
            return setValuedBuffParams(SpecificDefenseBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Stun.class.getSimpleName())) {
            return setCommonBuffParams(Stun.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(SureHit.class.getSimpleName())) {
            return setCommonBuffParams(SureHit.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Taunt.class.getSimpleName())) {
            return setValuedBuffParams(Taunt.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Terror.class.getSimpleName())) {
            return setEffectActivatingBuffParams(
                    Terror.builder(),
                    buffData.toBuilder()
                            .addSubEffects(
                                    EffectData.newBuilder()
                                            .setType(GrantBuff.class.getSimpleName())
                                            .setTarget(SELF)
                                            .addProbabilities(5)
                                            .addBuffData(
                                                    BuffData.newBuilder()
                                                            .setType(Stun.class.getSimpleName())
                                                            .setNumTurnsActive(2)
                                            )
                            )
                            .build()
                    , level
            );

        } else if (type.equalsIgnoreCase(TriggerOnGutsEffect.class.getSimpleName())) {
            return setEffectActivatingBuffParams(TriggerOnGutsEffect.builder(), buffData, level);
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
        if (buffData.getProbabilitiesCount() >= level) {
            builder.probability(buffData.getProbabilities(level - 1));
        } else if (buffData.getProbabilitiesCount() == 1) {
            builder.probability(buffData.getProbabilities(0));
        }

        builder.forceBuff(buffData.getForceBuff());
        builder.irremovable(buffData.getIrremovable());
        builder.forceStackable(buffData.getForceStackable());

        return builder.build();
    }

    public static Buff setValuedBuffParams(
            final ValuedBuff.ValuedBuffBuilder<?, ?> builder,
            final BuffData buffData,
            final int level
    ) {
        builder.value(getValueFromListForLevel(buffData.getValuesList(), level));
        return setCommonBuffParams(builder, buffData, level);
    }

    public static double getValueFromListForLevel(final List<Double> values, final int level) {
        if (values.size() >= level) {
            return values.get(level - 1);
        } else {
            return values.get(0);
        }
    }

    public static Buff setEffectActivatingBuffParams(
            final EffectActivatingBuff.EffectActivatingBuffBuilder<?, ?> builder,
            final BuffData buffData,
            final int level
    ) {
        builder.effects(EffectFactory.buildEffects(buffData.getSubEffectsList(), level));
        return setCommonBuffParams(builder, buffData, level);
    }
}
