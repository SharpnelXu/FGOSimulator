package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.simulator.models.conditions.ConditionFactory;

import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.simulator.models.effects.OrderChange.ORDER_CHANGE;
import static yome.fgo.simulator.models.effects.ShuffleCards.SHUFFLE_CARDS;

public class EffectFactory {
    public static List<Effect> buildEffects(final List<EffectData> effectData) {
        return buildEffects(effectData, 1);
    }

    public static List<Effect> buildEffects(final List<EffectData> effectDataList, final int level) {
        return effectDataList.stream()
                .map(effectData -> buildEffect(effectData, level))
                .collect(Collectors.toList());
    }

    public static Effect buildEffect(final EffectData effectData, final int level) {
        final String type = effectData.getType();
        if (type.equalsIgnoreCase(CriticalStarChange.class.getSimpleName())) {
            final CriticalStarChange.CriticalStarChangeBuilder<?, ?> builder = CriticalStarChange.builder();
            setApplyConditionIfExists(builder, effectData);
            return setCommonIntValuedEffectValue(builder, effectData, level);

        } else if (type.equalsIgnoreCase(DecreaseActiveSKillCoolDown.class.getSimpleName())) {
            final DecreaseActiveSKillCoolDown.DecreaseActiveSKillCoolDownBuilder<?, ?> builder = DecreaseActiveSKillCoolDown.builder();
            setApplyConditionIfExists(builder, effectData);
            return setCommonIntValuedEffectValue(builder, effectData, level);

        } else if (type.equalsIgnoreCase(GrantBuff.class.getSimpleName())) {
            return setCommonGrantBuffEffectValue(GrantBuff.builder(), effectData, level);

        } else if (type.equalsIgnoreCase(HpChange.class.getSimpleName())) {
            final HpChange.HpChangeBuilder<?, ?> builder = HpChange.builder();
            setApplyConditionIfExists(builder, effectData);
            return setCommonIntValuedEffectValue(builder, effectData, level);

        } else if (type.equalsIgnoreCase(MaxHpChange.class.getSimpleName())) {
            return setCommonGrantBuffEffectValue(MaxHpChange.builder(), effectData, level);

        } else if (type.equalsIgnoreCase(NoblePhantasmDamage.class.getSimpleName())) {
            final NoblePhantasmDamage.NoblePhantasmDamageBuilder<?, ?> builder = NoblePhantasmDamage.builder()
                    .target(effectData.getTarget())
                    .isNpIgnoreDefense(effectData.getIsNpIgnoreDefense());

            if (effectData.getIsNpSpecificDamageOverchargedEffect()) {
                builder.npSpecificDamageRates(effectData.getNpSpecificDamageRateList())
                        .isNpSpecificDamageOverchargedEffect(true)
                        .isOverchargedEffect(true);
            } else if (effectData.getNpSpecificDamageRateCount() >= level) {
                builder.npSpecificDamageRates(ImmutableList.of(effectData.getNpSpecificDamageRate(level - 1)));
            } else if (effectData.getNpSpecificDamageRateCount() == 1) {
                builder.npSpecificDamageRates(ImmutableList.of(effectData.getNpSpecificDamageRate(0)));
            }

            setApplyConditionIfExists(builder, effectData);

            if (effectData.getIsOverchargedEffect()) {
                final double baseDamageRate;
                if (effectData.getValuesCount() >= level) {
                    baseDamageRate = effectData.getValues(level - 1);
                } else {
                    baseDamageRate = effectData.getValues(0);
                }
                builder.damageRates(
                        effectData.getNpOverchargeDamageRateList()
                                .stream()
                                .map(rate -> rate + baseDamageRate)
                                .collect(Collectors.toList())
                );
                builder.isOverchargedEffect(true);
            } else if (effectData.getValuesCount() >= level) {
                builder.damageRates(ImmutableList.of(effectData.getValues(level - 1)));
            } else {
                builder.damageRates(ImmutableList.of(effectData.getValues(0)));
            }
            return builder.build();

        } else if (type.equalsIgnoreCase(NpChange.class.getSimpleName())) {
            final NpChange.NpChangeBuilder<?, ?> builder = NpChange.builder()
                    .target(effectData.getTarget());
            setApplyConditionIfExists(builder, effectData);
            if (effectData.getIsOverchargedEffect()) {
                builder.npChanges(effectData.getValuesList());
                builder.isOverchargedEffect(true);
            } else if (effectData.getValuesCount() >= level) {
                builder.npChanges(ImmutableList.of(effectData.getValues(level - 1)));
            } else {
                builder.npChanges(ImmutableList.of(effectData.getValues(0)));
            }
            return builder.build();

        } else if (type.equalsIgnoreCase(NpGaugeChange.class.getSimpleName())) {
            final NpGaugeChange.NpGaugeChangeBuilder<?, ?> builder = NpGaugeChange.builder();
            setApplyConditionIfExists(builder, effectData);
            return setCommonIntValuedEffectValue(builder, effectData, level);

        } else if (type.equalsIgnoreCase(OrderChange.class.getSimpleName())) {
            return ORDER_CHANGE;

        } else if (type.equalsIgnoreCase(RemoveBuff.class.getSimpleName())) {
            final RemoveBuff.RemoveBuffBuilder<?, ?> builder = RemoveBuff.builder()
                    .target(effectData.getTarget());
            setApplyConditionIfExists(builder, effectData);
            return setCommonIntValuedEffectValue(builder, effectData, level);

        } else if (type.equalsIgnoreCase(ShuffleCards.class.getSimpleName())) {
            return SHUFFLE_CARDS;
        }

        throw new UnsupportedOperationException("Effect type unsupported: " + type);
    }

    private static void setApplyConditionIfExists(Effect.EffectBuilder<?, ?> builder, EffectData effectData) {
        if (effectData.hasApplyCondition()) {
            builder.applyCondition(ConditionFactory.buildCondition(effectData.getApplyCondition()));
        }
    }

    private static Effect setCommonIntValuedEffectValue(
            final IntValuedEffect.IntValuedEffectBuilder<?, ?> builder,
            final EffectData effectData,
            final int level
    ) {
        if (effectData.getIsOverchargedEffect()) {
            builder.values(effectData.getIntValuesList())
                    .isOverchargedEffect(true);
        } else if (effectData.getIntValuesCount() >= level) {
            builder.values(ImmutableList.of(effectData.getIntValues(level - 1)));
        } else if (effectData.getIntValuesCount() == 1) {
            builder.values(ImmutableList.of(effectData.getIntValues(0)));
        }

        return builder.build();
    }

    private static Effect setCommonGrantBuffEffectValue(
            final GrantBuff.GrantBuffBuilder<?, ?> builder,
            final EffectData effectData,
            final int level
    ) {
        builder.target(effectData.getTarget())
                .buffLevel(level);
        if (effectData.getProbabilitiesCount() == 1) {
            builder.probability(effectData.getProbabilities(0));
        } else if (effectData.getProbabilitiesCount() != 0) {
            builder.probability(effectData.getProbabilities(level - 1));
        }
        if (effectData.getIsOverchargedEffect()) {
            builder.isOverchargedEffect(true).buffData(effectData.getBuffDataList());
        } else if (effectData.getBuffDataCount() >= level) {
            builder.buffData(ImmutableList.of(effectData.getBuffData(level - 1)));
        } else {
            builder.buffData(ImmutableList.of(effectData.getBuffData(0)));
        }
        setApplyConditionIfExists(builder, effectData);
        return builder.build();
    }
}
