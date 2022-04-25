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

            if (effectData.getIsOverchargedEffect()) {
                builder.numStarsGains(effectData.getValuesList()
                                              .stream()
                                              .map(Double::intValue)
                                              .collect(Collectors.toList()));
                builder.isOverchargedEffect(true);
            } else if (effectData.getValuesCount() >= level) {
                builder.numStarsGains(ImmutableList.of((int) effectData.getValues(level - 1)));
            } else {
                builder.numStarsGains(ImmutableList.of((int) effectData.getValues(0)));
            }

            return builder.build();
        } else if (type.equalsIgnoreCase(GrantBuff.class.getSimpleName())) {
            final GrantBuff.GrantBuffBuilder<?, ?> builder = GrantBuff.builder()
                    .target(effectData.getTarget())
                    .buffLevel(level)
                    .buffData(effectData.getBuffDataList());

            if (effectData.getProbabilitiesCount() == 1) {
                builder.probability(effectData.getProbabilities(0));
            } else if (effectData.getProbabilitiesCount() != 0) {
                builder.probability(effectData.getProbabilities(level - 1));
            }

            if (effectData.getIsOverchargedEffect()) {
                builder.isOverchargedEffect(true);
            }

            setApplyConditionIfExists(builder, effectData);

            return builder.build();
        } else if (type.equalsIgnoreCase(HpChange.class.getSimpleName())) {
            final HpChange.HpChangeBuilder<?, ?> builder = HpChange.builder();

            setApplyConditionIfExists(builder, effectData);

            if (effectData.getIsOverchargedEffect()) {
                builder.values(effectData.getValuesList()
                                              .stream()
                                              .map(Double::intValue)
                                              .collect(Collectors.toList()));
                builder.isOverchargedEffect(true);
            } else if (effectData.getValuesCount() >= level) {
                builder.values(ImmutableList.of((int) effectData.getValues(level - 1)));
            } else {
                builder.values(ImmutableList.of((int) effectData.getValues(0)));
            }

            return builder.build();
        } else if (type.equalsIgnoreCase(NoblePhantasmDamage.class.getSimpleName())) {
            final NoblePhantasmDamage.NoblePhantasmDamageBuilder<?, ?> builder = NoblePhantasmDamage.builder()
                    .target(effectData.getTarget());

            if (effectData.getIsNpSpecificDamageOverchargedEffect()) {
                builder.npSpecificDamageRates(effectData.getNpSpecificDamageRateList())
                        .isNpSpecificDamageOverchargedEffect(true)
                        .isOverchargedEffect(true);
            } else if (effectData.getNpSpecificDamageRateCount() == 0) {
                builder.npSpecificDamageRates(ImmutableList.of(1.0));
            } else if (effectData.getNpSpecificDamageRateCount() == 1) {
                builder.npSpecificDamageRates(effectData.getNpSpecificDamageRateList());
            } else {
                builder.npSpecificDamageRates(ImmutableList.of(effectData.getNpSpecificDamageRate(level - 1)));
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
        } else if (type.equalsIgnoreCase(OrderChange.class.getSimpleName())) {
            return ORDER_CHANGE;
        } else if (type.equalsIgnoreCase(RemoveBuff.class.getSimpleName())) {
            final RemoveBuff.RemoveBuffBuilder<?, ?> builder = RemoveBuff.builder()
                    .target(effectData.getTarget());

            setApplyConditionIfExists(builder, effectData);

            if (effectData.getIsOverchargedEffect()) {
                builder.isOverchargedEffect(true);
                builder.numToRemove(effectData.getValuesList()
                                            .stream()
                                            .map(Double::intValue)
                                            .collect(Collectors.toList()));
            } else if (effectData.getValuesCount() >= level) {
                builder.numToRemove(ImmutableList.of((int) effectData.getValues(level - 1)));
            } else if (effectData.getValuesCount() == 1) {
                builder.numToRemove(ImmutableList.of((int) effectData.getValues(0)));
            }

            return builder.build();
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
}
