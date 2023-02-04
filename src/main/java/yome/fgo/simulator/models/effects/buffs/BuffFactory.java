package yome.fgo.simulator.models.effects.buffs;

import com.google.common.collect.Lists;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeAdditionalParams;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.OnFieldBuffParams;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.conditions.And;
import yome.fgo.simulator.models.conditions.BuffHasTrait;
import yome.fgo.simulator.models.conditions.BuffTypeEquals;
import yome.fgo.simulator.models.conditions.Condition;
import yome.fgo.simulator.models.effects.EffectFactory;
import yome.fgo.simulator.models.effects.ForceGrantBuff;
import yome.fgo.simulator.models.effects.ForceRemoveBuff;
import yome.fgo.simulator.models.effects.GrantBuff;
import yome.fgo.simulator.models.variations.VariationFactory;
import yome.fgo.simulator.utils.BuffUtils;

import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.BuffTraits.ATTACKER_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.DEFENDER_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.IMMOBILIZE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.MENTAL_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.NEGATIVE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.POSITIVE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode.CLASS_ADV_NO_CHANGE;
import static yome.fgo.data.proto.FgoStorageData.Target.SELF;
import static yome.fgo.simulator.models.conditions.Always.ALWAYS;
import static yome.fgo.simulator.models.conditions.ConditionFactory.buildCondition;
import static yome.fgo.simulator.models.effects.buffs.Buff.ON_FIELD_BUFF_MARK;
import static yome.fgo.simulator.models.effects.buffs.BuffType.CHARM;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DEBUFF_RESIST;
import static yome.fgo.simulator.models.effects.buffs.BuffType.END_OF_TURN_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SKILL_SEAL;
import static yome.fgo.simulator.models.effects.buffs.BuffType.STUN;

public class BuffFactory {
    public static Buff buildBuff(
            final BuffData buffData,
            final int level,
            final boolean isPassive,
            final int activatorHash
    ) {
        final BuffType buffType = BuffType.ofType(buffData.getType());

        final Buff.BuffBuilder builder = Buff.builder();
        builder.buffType(buffType);
        builder.iconName(buffData.getBuffIcon());
        final Condition condition = buffData.hasApplyCondition() ?
                buildCondition(buffData.getApplyCondition()) :
                ALWAYS;
        builder.condition(condition);
        if (buffData.getProbabilitiesCount() > 0) {
            builder.probability(getValueFromListForLevel(buffData.getProbabilitiesList(), level));
        }
        builder.forceStackable(buffData.getForceStackable());
        builder.irremovable(buffData.getIrremovable() || isPassive);
        builder.isPassive(isPassive);
        builder.activatorHash(activatorHash);

        if (buffData.getNumTurnsActive() > 0) {
            builder.activeTurns(buffData.getNumTurnsActive());
        }
        if (buffData.getNumTimesActive() > 0) {
            builder.activeTimes(buffData.getNumTimesActive());
        }

        double value = 0, addition = 0;
        if (buffData.getValuesCount() > 0) {
            value = getValueFromListForLevel(buffData.getValuesList(), level);
            builder.value(value);
            if (buffData.hasVariationData()) {
                builder.variation(VariationFactory.buildVariation(buffData.getVariationData()));
                addition = getValueFromListForLevel(buffData.getAdditionsList(), level);
                builder.addition(addition);
            }
        }

        if (buffData.getSubEffectsCount() > 0) {
            builder.effects(EffectFactory.buildEffects(buffData.getSubEffectsList(), level));
        }

        builder.trait(buffData.getStringValue());

        switch (buffType) {
            case CARD_TYPE_CHANGE:
            case NP_CARD_TYPE_CHANGE:
                builder.commandCardType(CommandCardType.valueOf(buffData.getStringValue().toUpperCase()));
                break;
            case CHARM_RESIST_DOWN:
                builder.buffType(DEBUFF_RESIST);
                final Condition charmCondition = new BuffTypeEquals(CHARM);
                final Condition resultCondition = condition == ALWAYS ?
                        charmCondition :
                        new And(Lists.newArrayList(charmCondition, condition));
                builder.condition(resultCondition);
                builder.value(-1 * value);
                builder.addition(-1 * addition);
                break;
            case CLASS_ADVANTAGE_CHANGE_BUFF:
                final ClassAdvantageChangeAdditionalParams classAdvParams = buffData.getClassAdvChangeAdditionalParams();
                if (classAdvParams.getAttackMode() != CLASS_ADV_NO_CHANGE) {
                    builder.attackMode(classAdvParams.getAttackMode());
                    if (classAdvParams.getCustomizeAttackModifier()) {
                        builder.attackAdvantage(classAdvParams.getAttackAdv());
                    }
                    if (classAdvParams.getAttackModeAffectedClassesCount() != 0) {
                        builder.attackModeAffectedClasses(classAdvParams.getAttackModeAffectedClassesList());
                    }
                }
                if (classAdvParams.getDefenseMode() != CLASS_ADV_NO_CHANGE) {
                    builder.defenseMode(classAdvParams.getDefenseMode());
                    if (classAdvParams.getCustomizeDefenseModifier()) {
                        builder.defenseAdvantage(classAdvParams.getDefenseAdv());
                    }
                    if (classAdvParams.getDefenseModeAffectedClassesCount() != 0) {
                        builder.defenseModeAffectedClasses(classAdvParams.getDefenseModeAffectedClassesList());
                    }
                }
                break;
            case CONFUSION:
                builder.buffType(END_OF_TURN_EFFECT);
                final GrantBuff confusionEffect = GrantBuff.builder()
                        .target(SELF)
                        .probabilities(Lists.newArrayList(5.0))
                        .buffData(Lists.newArrayList(
                                BuffData.newBuilder()
                                        .setType(SKILL_SEAL.getType())
                                        .setNumTurnsActive(1)
                                        .setBuffIcon("skillSeal")
                                        .build()
                        ))
                        .build();
                builder.effects(Lists.newArrayList(confusionEffect));
                break;
            case GUTS:
                builder.isPercentageGuts(buffData.getIsGutsPercentBased());
                break;
            case ON_FIELD_EFFECT:
                final OnFieldBuffParams onFieldBuffParams = buffData.getOnFieldBuffParams();
                final Buff baseBuff = buildBuff(onFieldBuffParams.getBuffData(), level);
                builder.activatedBuffBase(baseBuff);

                final Target target = onFieldBuffParams.getTarget();
                builder.target(target);

                final String buffHash = "" + baseBuff.hashCode();
                final BuffData baseBuffData = onFieldBuffParams.getBuffData().toBuilder()
                        .clearCustomTraits()
                        .addAllCustomTraits(baseBuff.buffTraits)
                        .addCustomTraits(ON_FIELD_BUFF_MARK)
                        .addCustomTraits(buffHash)
                        .setHasCustomTraits(true)
                        .setIrremovable(true)
                        .build();
                final ForceGrantBuff forceGrantBuff = ForceGrantBuff.builder()
                        .target(target)
                        .buffData(List.of(baseBuffData))
                        .applyCondition(condition)
                        .buffLevel(level)
                        .build();
                builder.forceGrantBuff(forceGrantBuff);

                final ForceRemoveBuff forceRemoveBuff = ForceRemoveBuff.builder()
                        .target(target)
                        .removeFromStart(false)
                        .values(List.of(1))
                        .applyCondition(new And(List.of(new BuffHasTrait(ON_FIELD_BUFF_MARK), new BuffHasTrait(buffHash))))
                        .build();
                builder.forceRemoveBuff(forceRemoveBuff);
                break;
            case TERROR:
                builder.buffType(END_OF_TURN_EFFECT);
                final GrantBuff terrorEffect = GrantBuff.builder()
                        .target(SELF)
                        .probabilities(Lists.newArrayList(5.0))
                        .buffData(Lists.newArrayList(
                                BuffData.newBuilder()
                                        .setType(STUN.getType())
                                        .setNumTurnsActive(1)
                                        .setBuffIcon("stun")
                                        .build()
                        ))
                        .build();
                builder.effects(Lists.newArrayList(terrorEffect));
                break;
        }

        final Buff buff = builder.build();
        if (buffData.getHasCustomTraits()) {
            buff.getBuffTraits().addAll(buffData.getCustomTraitsList());
        } else {
            final List<String> buffTraits = buff.getBuffTraits();
            if (buff.isPositiveBuffType()) {
                buffTraits.add(POSITIVE_BUFF.name());
            }
            if (buff.isNegativeBuffType()) {
                buffTraits.add(NEGATIVE_BUFF.name());
            }
            if (BuffUtils.isAttackerBuff(buffType)) {
                buffTraits.add(ATTACKER_BUFF.name());
            }
            if (BuffUtils.isDefenderBuff(buffType)) {
                buffTraits.add(DEFENDER_BUFF.name());
            }
            if (BuffUtils.isMentalDebuff(buffType)) {
                buffTraits.add(MENTAL_BUFF.name());
            }
            if (BuffUtils.isImmobilizeDebuff(buffType)) {
                buffTraits.add(IMMOBILIZE_BUFF.name());
            }
        }

        return buff;
    }

    public static Buff buildBuff(final BuffData buffData, final int level) {
        return buildBuff(buffData, level, false, -1);
    }

    public static double getValueFromListForLevel(final List<Double> values, final int level) {
        if (values.size() >= level) {
            return values.get(level - 1);
        } else {
            return values.get(0);
        }
    }
}
