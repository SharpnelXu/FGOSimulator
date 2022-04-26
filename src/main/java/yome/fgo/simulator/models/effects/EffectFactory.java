package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.NpDamageAdditionalParams;
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
                    .target(effectData.getTarget());

            if (effectData.hasNpDamageAdditionalParams()) {
                final NpDamageAdditionalParams additionalParams = effectData.getNpDamageAdditionalParams();
                builder.isNpIgnoreDefense(additionalParams.getIsNpIgnoreDefense());
                if (additionalParams.hasNpSpecificDamageCondition()) {
                    builder.npSpecificDamageCondition(ConditionFactory.buildCondition(additionalParams.getNpSpecificDamageCondition()));
                }

                if (additionalParams.getIsNpSpecificDamageOverchargedEffect()) {
                    builder.npSpecificDamageRates(additionalParams.getNpSpecificDamageRateList())
                            .isNpSpecificDamageOverchargedEffect(true)
                            .isOverchargedEffect(true);
                } else {
                    builder.npSpecificDamageRates(getSingletonValueListForLevel(additionalParams.getNpSpecificDamageRateList(), level));
                }

                if (additionalParams.getIsNpDamageOverchargedEffect()) {
                    final double baseDamageRate = getSingletonValueListForLevel(effectData.getValuesList(), level).get(0);

                    builder.damageRates(
                            additionalParams.getNpOverchargeDamageRateList()
                                    .stream()
                                    .map(rate -> rate + baseDamageRate)
                                    .collect(Collectors.toList())
                    );
                    builder.isOverchargedEffect(true).isNpDamageOverchargedEffect(true);
                } else {
                    builder.damageRates(getSingletonValueListForLevel(effectData.getValuesList(), level));
                }
            } else {
                builder.damageRates(getSingletonValueListForLevel(effectData.getValuesList(), level));
            }

            setApplyConditionIfExists(builder, effectData);
            return builder.build();

        } else if (type.equalsIgnoreCase(NpChange.class.getSimpleName())) {
            final NpChange.NpChangeBuilder<?, ?> builder = NpChange.builder()
                    .target(effectData.getTarget());
            setApplyConditionIfExists(builder, effectData);
            if (effectData.getIsOverchargedEffect()) {
                builder.npChanges(effectData.getValuesList());
                builder.isOverchargedEffect(true);
            } else {
                builder.npChanges(getSingletonValueListForLevel(effectData.getValuesList(), level));
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
        } else {
            builder.values(getSingletonValueListForLevel(effectData.getIntValuesList(), level));
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
        } else {
            builder.buffData(getSingletonValueListForLevel(effectData.getBuffDataList(), level));
        }
        setApplyConditionIfExists(builder, effectData);
        return builder.build();
    }

    private static <E> List<E> getSingletonValueListForLevel(final List<E> values, final int level) {
        if (values.size() >= level) {
            return ImmutableList.of(values.get(level - 1));
        } else if (!values.isEmpty()) {
            return ImmutableList.of(values.get(0));
        } else {
            return ImmutableList.of();
        }
    }
}
