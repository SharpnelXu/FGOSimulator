package yome.fgo.simulator.models.effects.buffs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeAdditionalParams;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.simulator.models.conditions.BuffTypeEquals;
import yome.fgo.simulator.models.effects.EffectFactory;
import yome.fgo.simulator.models.effects.GrantBuff;
import yome.fgo.simulator.models.variations.VariationFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static yome.fgo.data.proto.FgoStorageData.BuffTraits.ATTACKER_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.DEFENDER_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.IMMOBILIZE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.MENTAL_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.NEGATIVE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.POSITIVE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode.CLASS_ADV_NO_CHANGE;
import static yome.fgo.data.proto.FgoStorageData.Target.SELF;
import static yome.fgo.data.proto.FgoStorageData.Traits.BLESSED_BY_KUR;
import static yome.fgo.data.proto.FgoStorageData.Traits.BURNING_LOVE;
import static yome.fgo.data.proto.FgoStorageData.Traits.VENGEANCE;
import static yome.fgo.simulator.models.conditions.ConditionFactory.buildCondition;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields.BUFF_FIELD_CARD_TYPE;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields.BUFF_FIELD_CLASS_ADV;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields.BUFF_FIELD_DOUBLE_VALUE;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields.BUFF_FIELD_EFFECTS;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields.BUFF_FIELD_INT_VALUE;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields.BUFF_FIELD_PERCENT_OPTION;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.BuffFields.BUFF_FIELD_STRING_VALUE;

public class BuffFactory {
    public static final Map<String, Set<BuffFields>> BUFF_REQUIRED_FIELDS_MAP = buildRequiredBuffFieldsMap();
    public enum BuffFields {
        BUFF_FIELD_DOUBLE_VALUE,
        BUFF_FIELD_INT_VALUE,
        BUFF_FIELD_STRING_VALUE,
        BUFF_FIELD_EFFECTS,
        BUFF_FIELD_PERCENT_OPTION,
        BUFF_FIELD_CLASS_ADV,
        BUFF_FIELD_CARD_TYPE
    }

    public static Buff buildBuff(final BuffData buffData, final int level) {
        final String type = buffData.getType();

        if (type.equalsIgnoreCase(AttackBuff.class.getSimpleName())) {
            return setValuedBuffParams(AttackBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(AttackBuffDurationExtend.class.getSimpleName())) {
            return setCommonBuffParams(AttackBuffDurationExtend.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(BlessedByKur.class.getSimpleName())) {
            return setCommonBuffParams(BlessedByKur.builder().trait(BLESSED_BY_KUR.name()), buffData, level);

        } else if (type.equalsIgnoreCase(BuffChanceBuff.class.getSimpleName())) {
            return setValuedBuffParams(BuffChanceBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(BuffRemovalResist.class.getSimpleName())) {
            return setValuedBuffParams(BuffRemovalResist.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Burn.class.getSimpleName())) {
            return setValuedBuffParams(Burn.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(BurnEffectivenessUp.class.getSimpleName())) {
            return setValuedBuffParams(BurnEffectivenessUp.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(BurningLove.class.getSimpleName())) {
            return setCommonBuffParams(BurningLove.builder().trait(BURNING_LOVE.name()), buffData, level);

        } else if (type.equalsIgnoreCase(CardTypeChange.class.getSimpleName())) {
            return setCommonBuffParams(
                    CardTypeChange.builder()
                            .commandCardType(CommandCardType.valueOf(buffData.getStringValue().toUpperCase())),
                    buffData,
                    level
            );

        } else if (type.equalsIgnoreCase(Charm.class.getSimpleName())) {
            return setCommonBuffParams(Charm.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CharmResistDown.class.getSimpleName())) {
            return setValuedBuffParams(
                    CharmResistDown.builder(),
                    buffData.toBuilder()
                            .setApplyCondition(
                                    ConditionData.newBuilder()
                                            .setType(BuffTypeEquals.class.getSimpleName())
                                            .setValue(Charm.class.getSimpleName())
                            )
                            .build(),
                    level
            );

        } else if (type.equalsIgnoreCase(ClassAdvantageChangeBuff.class.getSimpleName())) {
            final ClassAdvantageChangeBuff.ClassAdvantageChangeBuffBuilder<?, ?> builder = ClassAdvantageChangeBuff.builder();
            if (!buffData.hasClassAdvChangeAdditionalParams()) {
                throw new IllegalArgumentException("No params provided for class advantage change buff.");
            }
            final ClassAdvantageChangeAdditionalParams additionalParams = buffData.getClassAdvChangeAdditionalParams();
            if (additionalParams.getAttackMode() != CLASS_ADV_NO_CHANGE) {
                builder.attackMode(additionalParams.getAttackMode());
                if (additionalParams.getCustomizeAttackModifier()) {
                    builder.attackAdvantage(additionalParams.getAttackAdv());
                }
                if (additionalParams.getAttackModeAffectedClassesCount() != 0) {
                    builder.attackModeAffectedClasses(additionalParams.getAttackModeAffectedClassesList());
                }
            }
            if (additionalParams.getDefenseMode() != CLASS_ADV_NO_CHANGE) {
                builder.defenseMode(additionalParams.getDefenseMode());
                if (additionalParams.getCustomizeDefenseModifier()) {
                    builder.defenseAdvantage(additionalParams.getDefenseAdv());
                }
                if (additionalParams.getDefenseModeAffectedClassesCount() != 0) {
                    builder.defenseModeAffectedClasses(additionalParams.getDefenseModeAffectedClassesList());
                }
            }

            return setCommonBuffParams(builder, buffData, level);

        } else if (type.equalsIgnoreCase(CommandCardBuff.class.getSimpleName())) {
            return setValuedBuffParams(CommandCardBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(CommandCardResist.class.getSimpleName())) {
            return setValuedBuffParams(CommandCardResist.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Confusion.class.getSimpleName())) {
            return setEffectActivatingBuffParams(
                    Confusion.builder(),
                    buffData.toBuilder()
                            .addSubEffects(
                                    EffectData.newBuilder()
                                            .setType(GrantBuff.class.getSimpleName())
                                            .setTarget(SELF)
                                            .addProbabilities(5)
                                            .addBuffData(
                                                    BuffData.newBuilder()
                                                            .setType(SkillSeal.class.getSimpleName())
                                                            .setNumTurnsActive(1)
                                            )
                            )
                            .build()
                    , level
            );

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

        } else if (type.equalsIgnoreCase(DamageReflect.class.getSimpleName())) {
            return setValuedBuffParams(DamageReflect.builder(), buffData, level);

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

        } else if (type.equalsIgnoreCase(DoNotShuffleIn.class.getSimpleName())) {
            return setCommonBuffParams(DoNotShuffleIn.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(EndOfTurnEffect.class.getSimpleName())) {
            return setEffectActivatingBuffParams(EndOfTurnEffect.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(EnterFieldEffect.class.getSimpleName())) {
            return setEffectActivatingBuffParams(EnterFieldEffect.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Evade.class.getSimpleName())) {
            return setCommonBuffParams(Evade.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(FacelessMoon.class.getSimpleName())) {
            return setCommonBuffParams(FacelessMoon.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(GrantStageTrait.class.getSimpleName())) {
            return setCommonBuffParams(GrantStageTrait.builder().trait(buffData.getStringValue()), buffData, level);

        } else if (type.equalsIgnoreCase(GrantTrait.class.getSimpleName())) {
            return setCommonBuffParams(GrantTrait.builder().trait(buffData.getStringValue()), buffData, level);

        } else if (type.equalsIgnoreCase(Guts.class.getSimpleName())) {
            final double value = getValueFromListForLevel(buffData.getValuesList(), level);
            if (value <= 0) {
                throw new IllegalArgumentException("Guts have non positive value");
            }

            final Guts.GutsBuilder<?, ?> builder = Guts.builder();
            if (buffData.getIsGutsPercentBased()) {
                builder.percent(value);
            } else {
                builder.gutsLeft((int) value);
            }

            return setCommonBuffParams(builder, buffData, level);

        } else if (type.equalsIgnoreCase(HealEffectivenessBuff.class.getSimpleName())) {
            return setValuedBuffParams(HealEffectivenessBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(HitsDoubledBuff.class.getSimpleName())) {
            return setCommonBuffParams(HitsDoubledBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(IgnoreDefenseBuff.class.getSimpleName())) {
            return setCommonBuffParams(IgnoreDefenseBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(IgnoreInvincible.class.getSimpleName())) {
            return setCommonBuffParams(IgnoreInvincible.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Invincible.class.getSimpleName())) {
            return setCommonBuffParams(Invincible.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(LeaveFieldEffect.class.getSimpleName())) {
            return setEffectActivatingBuffParams(LeaveFieldEffect.builder(), buffData, level);

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

        } else if (type.equalsIgnoreCase(NpDamageBuffEffectivenessUp.class.getSimpleName())) {
            return setValuedBuffParams(NpDamageBuffEffectivenessUp.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(NpGenerationBuff.class.getSimpleName())) {
            return setValuedBuffParams(NpGenerationBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(NpSeal.class.getSimpleName())) {
            return setCommonBuffParams(NpSeal.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(OverchargeBuff.class.getSimpleName())) {
            final OverchargeBuff.OverchargeBuffBuilder<?, ?> builder = OverchargeBuff.builder();
            final int value = (int) getValueFromListForLevel(buffData.getValuesList(), level);

            return setCommonBuffParams(builder.value(value), buffData, level);

        } else if (type.equalsIgnoreCase(PercentAttackBuff.class.getSimpleName())) {
            return setValuedBuffParams(PercentAttackBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(PercentDefenseBuff.class.getSimpleName())) {
            return setValuedBuffParams(PercentDefenseBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(PermanentSleep.class.getSimpleName())) {
            return setCommonBuffParams(PermanentSleep.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Pigify.class.getSimpleName())) {
            return setCommonBuffParams(Pigify.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Poison.class.getSimpleName())) {
            return setValuedBuffParams(Poison.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(PoisonEffectivenessUp.class.getSimpleName())) {
            return setValuedBuffParams(PoisonEffectivenessUp.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(PostAttackEffect.class.getSimpleName())) {
            return setEffectActivatingBuffParams(PostAttackEffect.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(PostDefenseEffect.class.getSimpleName())) {
            return setEffectActivatingBuffParams(PostDefenseEffect.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(PreAttackEffect.class.getSimpleName())) {
            return setEffectActivatingBuffParams(PreAttackEffect.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(PreDefenseEffect.class.getSimpleName())) {
            return setEffectActivatingBuffParams(PreDefenseEffect.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(PreventDeathAgainstDoT.class.getSimpleName())) {
            return setCommonBuffParams(PreventDeathAgainstDoT.builder().type(buffData.getStringValue()), buffData, level);

        } else if (type.equalsIgnoreCase(ReceivedBuffChanceBuff.class.getSimpleName())) {
            return setValuedBuffParams(ReceivedBuffChanceBuff.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Sleep.class.getSimpleName())) {
            return setCommonBuffParams(Sleep.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(SkillRankUp.class.getSimpleName())) {
            return setCommonBuffParams(SkillRankUp.builder(), buffData, level);

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
                                                            .setNumTurnsActive(1)
                                            )
                            )
                            .build()
                    , level
            );

        } else if (type.equalsIgnoreCase(TriggerOnGutsEffect.class.getSimpleName())) {
            return setEffectActivatingBuffParams(TriggerOnGutsEffect.builder(), buffData, level);

        } else if (type.equalsIgnoreCase(Vengeance.class.getSimpleName())) {
            return setCommonBuffParams(Vengeance.builder().trait(VENGEANCE.name()), buffData, level);
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

        builder.irremovable(buffData.getIrremovable());
        builder.forceStackable(buffData.getForceStackable());
        builder.iconName(buffData.getBuffIcon());

        final Buff buff =  builder.build();
        if (buffData.getHasCustomTraits()) {
            buff.getBuffTraits().addAll(buffData.getCustomTraitsList());
        } else {
            final List<String> buffTraits = buff.getBuffTraits();
            if (buff instanceof AttackerBuff) {
                buffTraits.add(ATTACKER_BUFF.name());
            }
            if (buff instanceof DefenderBuff) {
                buffTraits.add(DEFENDER_BUFF.name());
            }
            if (buff.commonBuffCondition()) {
                buffTraits.add(POSITIVE_BUFF.name());
            }
            if (buff.commonDebuffCondition()) {
                buffTraits.add(NEGATIVE_BUFF.name());
            }
            if (buff instanceof MentalDebuff) {
                buffTraits.add(MENTAL_BUFF.name());
            }
            if (buff instanceof ImmobilizeDebuff) {
                buffTraits.add(IMMOBILIZE_BUFF.name());
            }
        }

        return buff;
    }

    public static Buff setValuedBuffParams(
            final ValuedBuff.ValuedBuffBuilder<?, ?> builder,
            final BuffData buffData,
            final int level
    ) {
        builder.value(getValueFromListForLevel(buffData.getValuesList(), level));
        if (buffData.hasVariationData()) {
            builder.variation(VariationFactory.buildVariation(buffData.getVariationData()));
            builder.addition(getValueFromListForLevel(buffData.getAdditionsList(), level));
        }
        return setCommonBuffParams(builder, buffData, level);
    }

    // special: maxHp, overcharge, guts
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

    public static Map<String, Set<BuffFields>> buildRequiredBuffFieldsMap() {
        final ImmutableMap.Builder<String, Set<BuffFields>> builder = ImmutableMap.builder();
        builder.put(AttackBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(DefenseBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(CommandCardBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(CommandCardResist.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(DamageAdditionBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(DamageReductionBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(CriticalDamageBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(NpDamageBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(NpDamageBuffEffectivenessUp.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(PercentAttackBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(PercentDefenseBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(SpecificAttackBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(SpecificDefenseBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));

        builder.put(IgnoreDefenseBuff.class.getSimpleName(), ImmutableSet.of());
        builder.put(Evade.class.getSimpleName(), ImmutableSet.of());
        builder.put(SureHit.class.getSimpleName(), ImmutableSet.of());
        builder.put(Invincible.class.getSimpleName(), ImmutableSet.of());
        builder.put(IgnoreInvincible.class.getSimpleName(), ImmutableSet.of());
        builder.put(SpecialInvincible.class.getSimpleName(), ImmutableSet.of());
        builder.put(HitsDoubledBuff.class.getSimpleName(), ImmutableSet.of());

        builder.put(CriticalChanceResist.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(CriticalRateBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(CriticalStarGenerationBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(CriticalStarWeightBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(NpGenerationBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(DefNpGenerationBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(OverchargeBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_INT_VALUE));
        builder.put(Taunt.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));

        builder.put(BuffChanceBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(ReceivedBuffChanceBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(DebuffChanceBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(DebuffResist.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(CharmResistDown.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(BuffRemovalResist.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(DeathChanceBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(DeathResist.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));

        builder.put(MaxHpBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_INT_VALUE));
        builder.put(Guts.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_PERCENT_OPTION));
        builder.put(TriggerOnGutsEffect.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_EFFECTS));

        builder.put(DelayedEffect.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_EFFECTS));
        builder.put(EndOfTurnEffect.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_EFFECTS));
        builder.put(EnterFieldEffect.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_EFFECTS));
        builder.put(LeaveFieldEffect.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_EFFECTS));
        builder.put(PreAttackEffect.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_EFFECTS));
        builder.put(PreDefenseEffect.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_EFFECTS));
        builder.put(PostAttackEffect.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_EFFECTS));
        builder.put(PostDefenseEffect.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_EFFECTS));

        builder.put(GrantStageTrait.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_STRING_VALUE));
        builder.put(GrantTrait.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_STRING_VALUE));
        builder.put(BlessedByKur.class.getSimpleName(), ImmutableSet.of());
        builder.put(BurningLove.class.getSimpleName(), ImmutableSet.of());
        builder.put(Vengeance.class.getSimpleName(), ImmutableSet.of());

        builder.put(HealEffectivenessBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(Burn.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_INT_VALUE));
        builder.put(BurnEffectivenessUp.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(Curse.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_INT_VALUE));
        builder.put(CurseEffectivenessUp.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(Poison.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_INT_VALUE));
        builder.put(PoisonEffectivenessUp.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(PreventDeathAgainstDoT.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_STRING_VALUE));

        builder.put(ClassAdvantageChangeBuff.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_CLASS_ADV));
        builder.put(CardTypeChange.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_CARD_TYPE));
        builder.put(NpCardTypeChange.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_CARD_TYPE));

        builder.put(Stun.class.getSimpleName(), ImmutableSet.of());
        builder.put(Pigify.class.getSimpleName(), ImmutableSet.of());
        builder.put(Charm.class.getSimpleName(), ImmutableSet.of());
        builder.put(Confusion.class.getSimpleName(), ImmutableSet.of());
        builder.put(Terror.class.getSimpleName(), ImmutableSet.of());
        builder.put(Sleep.class.getSimpleName(), ImmutableSet.of());
        builder.put(PermanentSleep.class.getSimpleName(), ImmutableSet.of());
        builder.put(NpSeal.class.getSimpleName(), ImmutableSet.of());
        builder.put(SkillSeal.class.getSimpleName(), ImmutableSet.of());

        builder.put(AttackBuffDurationExtend.class.getSimpleName(), ImmutableSet.of());
        builder.put(DamageReflect.class.getSimpleName(), ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE));
        builder.put(DoNotShuffleIn.class.getSimpleName(), ImmutableSet.of());
        builder.put(FacelessMoon.class.getSimpleName(), ImmutableSet.of());
        builder.put(SkillRankUp.class.getSimpleName(), ImmutableSet.of());

        return builder.build();
    }
}
