package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.GrantBuffAdditionalParams;
import yome.fgo.data.proto.FgoStorageData.NpDamageAdditionalParams;
import yome.fgo.simulator.models.conditions.ConditionFactory;

import java.util.List;
import java.util.TreeSet;
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
        if (type.equalsIgnoreCase(CardTypeChangeSelect.class.getSimpleName())) {
            final CardTypeChangeSelect.CardTypeChangeSelectBuilder<?, ?> builder = CardTypeChangeSelect.builder()
                    .selections(new TreeSet<>(effectData.getCardTypeSelectionsList()));
            return setCommonGrantBuffEffectValue(builder, effectData, level);

        } else if (type.equalsIgnoreCase(CriticalStarChange.class.getSimpleName())) {
            return setCommonIntValuedEffectValue(CriticalStarChange.builder(), effectData, level);

        } else if (type.equalsIgnoreCase(DecreaseActiveSKillCoolDown.class.getSimpleName())) {
            return setCommonIntValuedEffectValue(DecreaseActiveSKillCoolDown.builder(), effectData, level);

        } else if (type.equalsIgnoreCase(GrantBuff.class.getSimpleName())) {
            return setCommonGrantBuffEffectValue(GrantBuff.builder(), effectData, level);

        } else if (type.equalsIgnoreCase(HpChange.class.getSimpleName())) {
            return setCommonIntValuedEffectValue(HpChange.builder(), effectData, level);

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

            setCommonEffectParams(builder, effectData, level);
            return builder.build();

        } else if (type.equalsIgnoreCase(NpChange.class.getSimpleName())) {
            final NpChange.NpChangeBuilder<?, ?> builder = NpChange.builder()
                    .target(effectData.getTarget());
            setCommonEffectParams(builder, effectData, level);
            if (effectData.getIsOverchargedEffect()) {
                builder.npChanges(effectData.getValuesList());
                builder.isOverchargedEffect(true);
            } else {
                builder.npChanges(getSingletonValueListForLevel(effectData.getValuesList(), level));
            }
            return builder.build();

        } else if (type.equalsIgnoreCase(NpGaugeChange.class.getSimpleName())) {
            return setCommonIntValuedEffectValue(NpGaugeChange.builder(), effectData, level);

        } else if (type.equalsIgnoreCase(OrderChange.class.getSimpleName())) {
            return ORDER_CHANGE;

        } else if (type.equalsIgnoreCase(RemoveBuff.class.getSimpleName())) {
            final RemoveBuff.RemoveBuffBuilder<?, ?> builder = RemoveBuff.builder()
                    .target(effectData.getTarget());
            return setCommonIntValuedEffectValue(builder, effectData, level);

        } else if (type.equalsIgnoreCase(ShuffleCards.class.getSimpleName())) {
            return SHUFFLE_CARDS;
        }

        throw new UnsupportedOperationException("Effect type unsupported: " + type);
    }

    private static void setCommonEffectParams(Effect.EffectBuilder<?, ?> builder, EffectData effectData, final int level) {
        if (effectData.hasApplyCondition()) {
            builder.applyCondition(ConditionFactory.buildCondition(effectData.getApplyCondition()));
        }

        if (effectData.getIsProbabilityOvercharged()) {
            builder.probabilities(effectData.getProbabilitiesList())
                    .probabilities(getSingletonValueListForLevel(effectData.getProbabilitiesList(), level))
                    .isProbabilityOvercharged(true);
        } else {
            builder.probabilities(getSingletonValueListForLevel(effectData.getProbabilitiesList(), level));
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
        setCommonEffectParams(builder, effectData, level);

        return builder.build();
    }

    private static Effect setCommonGrantBuffEffectValue(
            final GrantBuff.GrantBuffBuilder<?, ?> builder,
            final EffectData effectData,
            final int level
    ) {
        builder.target(effectData.getTarget())
                .buffLevel(level);
        if (effectData.hasGrantBuffAdditionalParams()) {
            final GrantBuffAdditionalParams additionalParams = effectData.getGrantBuffAdditionalParams();
            if (additionalParams.getIsRepeatable()) {
                builder.repeatTimes(additionalParams.getRepeatTimes());
            }
        }

        if (effectData.getIsOverchargedEffect()) {
            builder.buffData(effectData.getBuffDataList())
                    .isOverchargedEffect(true)
                    .isBuffOvercharged(true);
        } else {
            builder.buffData(getSingletonValueListForLevel(effectData.getBuffDataList(), level));
        }

        setCommonEffectParams(builder, effectData, level);
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
