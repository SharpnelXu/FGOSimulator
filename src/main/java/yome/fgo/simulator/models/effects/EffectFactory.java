package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.NpDamageAdditionalParams;
import yome.fgo.simulator.models.conditions.ConditionFactory;
import yome.fgo.simulator.models.variations.VariationFactory;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static yome.fgo.simulator.models.effects.MoveToLastBackup.MOVE_TO_LAST_BACKUP;
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
        if (type.equalsIgnoreCase(AscensionChange.class.getSimpleName())) {
            return setCommonIntValuedEffectValue(AscensionChange.builder(), effectData, level);

        } else if (type.equalsIgnoreCase(BuffAbsorption.class.getSimpleName())) {
            return setCommonEffectParams(BuffAbsorption.builder().target(effectData.getTarget()), effectData, level);

        } else if (type.equalsIgnoreCase(CardTypeChangeSelect.class.getSimpleName())) {
            final CardTypeChangeSelect.CardTypeChangeSelectBuilder<?, ?> builder = CardTypeChangeSelect.builder()
                    .selections(new TreeSet<>(effectData.getCardTypeSelectionsList()));
            return setCommonGrantBuffEffectValue(builder, effectData, level);

        } else if (type.equalsIgnoreCase(CriticalStarChange.class.getSimpleName())) {
            return setCommonIntValuedEffectValue(CriticalStarChange.builder(), effectData, level);

        } else if (type.equalsIgnoreCase(DecreaseActiveSKillCoolDown.class.getSimpleName())) {
            return setCommonIntValuedEffectValue(DecreaseActiveSKillCoolDown.builder(), effectData, level);

        } else if (type.equalsIgnoreCase(GrantBuff.class.getSimpleName())) {
            return setCommonGrantBuffEffectValue(GrantBuff.builder(), effectData, level);

        } else if (type.equalsIgnoreCase(ForceInstantDeath.class.getSimpleName())) {
            return setCommonEffectParams(ForceInstantDeath.builder().target(effectData.getTarget()), effectData, level);

        } else if (type.equalsIgnoreCase(HpChange.class.getSimpleName())) {
            final HpChange.HpChangeBuilder<?, ?> builder = HpChange.builder()
                    .isLethal(effectData.getIsLethal());
            if (effectData.getIsOverchargedEffect()) {
                builder.isOverchargedEffect(true);
                if (effectData.getIsHpChangePercentBased()) {
                    builder.isPercentBased(true).percents(effectData.getValuesList());
                } else {
                    builder.values(effectData.getIntValuesList());
                }
            } else {
                if (effectData.getIsHpChangePercentBased()) {
                    builder.isPercentBased(true)
                            .percents(getSingletonValueListForLevel(effectData.getValuesList(), level));
                } else {
                    builder.values(getSingletonValueListForLevel(effectData.getIntValuesList(), level));
                }
            }

            return setCommonEffectParams(builder, effectData, level);

        } else if (type.equalsIgnoreCase(MaxHpChange.class.getSimpleName())) {
            return setCommonGrantBuffEffectValue(MaxHpChange.builder(), effectData, level);

        } else if (type.equalsIgnoreCase(MoveToLastBackup.class.getSimpleName())) {
            return MOVE_TO_LAST_BACKUP;

        } else if (type.equalsIgnoreCase(NoblePhantasmDamage.class.getSimpleName())) {
            return setCommonNpDamageParams(NoblePhantasmDamage.builder(), effectData, level);

        } else if (type.equalsIgnoreCase(NpChange.class.getSimpleName())) {
            return setCommonValuedEffectValue(NpChange.builder().target(effectData.getTarget()), effectData, level);

        } else if (type.equalsIgnoreCase(NpGaugeChange.class.getSimpleName())) {
            return setCommonIntValuedEffectValue(NpGaugeChange.builder(), effectData, level);

        } else if (type.equalsIgnoreCase(OrderChange.class.getSimpleName())) {
            return ORDER_CHANGE;

        } else if (type.equalsIgnoreCase(RandomEffects.class.getSimpleName())) {
            return setCommonEffectParams(
                    RandomEffects.builder().effectList(buildEffects(effectData.getRandomEffectSelectionsList(), level)),
                    effectData,
                    level
            );

        } else if (type.equalsIgnoreCase(RemoveBuff.class.getSimpleName())) {
            final RemoveBuff.RemoveBuffBuilder<?, ?> builder = RemoveBuff.builder()
                    .target(effectData.getTarget())
                    .removeFromStart(effectData.getRemoveFromStart());
            return setCommonIntValuedEffectValue(builder, effectData, level);

        } else if (type.equalsIgnoreCase(ShuffleCards.class.getSimpleName())) {
            return SHUFFLE_CARDS;

        }

        throw new UnsupportedOperationException("Effect type unsupported: " + type);
    }

    private static Effect setCommonEffectParams(Effect.EffectBuilder<?, ?> builder, EffectData effectData, final int level) {
        if (effectData.hasApplyCondition()) {
            builder.applyCondition(ConditionFactory.buildCondition(effectData.getApplyCondition()));
        }

        final List<Double> probabilities = effectData.getProbabilitiesList();
        if (effectData.getIsProbabilityOvercharged()) {
            builder.probabilities(probabilities)
                    .isProbabilityOvercharged(true);
        } else if (!probabilities.isEmpty()) {
            builder.probabilities(getSingletonValueListForLevel(probabilities, level));
        }

        return builder.build();
    }

    private static Effect setCommonValuedEffectValue(
            final ValuedEffect.ValuedEffectBuilder<?, ?> builder,
            final EffectData effectData,
            final int level
    ) {
        final List<Double> values = effectData.getValuesList();
        if (effectData.getIsOverchargedEffect()) {
            builder.values(values).isOverchargedEffect(true).isValueOvercharged(true);
        } else if (!values.isEmpty()) {
            builder.values(getSingletonValueListForLevel(values, level));
        }

        if (effectData.hasVariationData()) {
            final List<Double> additions = effectData.getAdditionsList();
            if (additions.isEmpty()) {
                throw new IllegalArgumentException("Variation is specified, but value is not provided.");
            }
            builder.variation(VariationFactory.buildVariation(effectData.getVariationData()));
            if (effectData.getIsAdditionOvercharged()) {
                builder.additions(additions).isOverchargedEffect(true).isAdditionsOvercharged(true);
            } else {
                builder.additions(getSingletonValueListForLevel(additions, level));
            }
        }

        return setCommonEffectParams(builder, effectData, level);
    }

    private static Effect setCommonIntValuedEffectValue(
            final IntValuedEffect.IntValuedEffectBuilder<?, ?> builder,
            final EffectData effectData,
            final int level
    ) {
        final List<Integer> intValues = effectData.getIntValuesList();
        if (effectData.getIsOverchargedEffect()) {
            builder.values(intValues).isOverchargedEffect(true).isValueOvercharged(true);
        } else if (!intValues.isEmpty()) {
            builder.values(getSingletonValueListForLevel(intValues, level));
        }

        if (effectData.hasVariationData()) {
            final List<Integer> additions = effectData.getAdditionsList()
                    .stream()
                    .map(Double::intValue)
                    .collect(Collectors.toList());
            if (additions.isEmpty()) {
                throw new IllegalArgumentException("Variation is specified, but value is not provided.");
            }
            builder.variation(VariationFactory.buildVariation(effectData.getVariationData()));
            if (effectData.getIsAdditionOvercharged()) {
                builder.additions(additions).isOverchargedEffect(true).isAdditionsOvercharged(true);
            } else {
                builder.additions(getSingletonValueListForLevel(additions, level));
            }
        }

        return setCommonEffectParams(builder, effectData, level);
    }

    private static Effect setCommonGrantBuffEffectValue(
            final GrantBuff.GrantBuffBuilder<?, ?> builder,
            final EffectData effectData,
            final int level
    ) {
        builder.target(effectData.getTarget())
                .buffLevel(level);

        if (effectData.getIsOverchargedEffect()) {
            builder.buffData(effectData.getBuffDataList())
                    .isOverchargedEffect(true)
                    .isBuffOvercharged(true);
        } else {
            builder.buffData(getSingletonValueListForLevel(effectData.getBuffDataList(), level));
        }

        return setCommonEffectParams(builder, effectData, level);
    }

    private static Effect setCommonNpDamageParams(
            final NoblePhantasmDamage.NoblePhantasmDamageBuilder<?, ?> builder,
            final EffectData effectData,
            final int level
    ) {
        builder.target(effectData.getTarget());
        final NpDamageAdditionalParams additionalParams = effectData.getNpDamageAdditionalParams();

        // base damage rate
        if (effectData.hasNpDamageAdditionalParams() && additionalParams.getIsNpDamageOverchargedEffect()) {
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

        // damage rate addition
        if (effectData.hasVariationData()) {
            final List<Double> additions = effectData.getAdditionsList();
            if (additions.isEmpty()) {
                throw new IllegalArgumentException("Variation is specified, but value is not provided.");
            }
            builder.damageRateVariation(VariationFactory.buildVariation(effectData.getVariationData()));
            if (effectData.getIsAdditionOvercharged()) {
                builder.isNpDamageAdditionOvercharged(true).isOverchargedEffect(true).damageRateAdditions(additions);
            } else {
                builder.damageRateAdditions(getSingletonValueListForLevel(additions, level));
            }
        }

        // other params
        if (effectData.hasNpDamageAdditionalParams()) {
            // ignore defense
            builder.isNpIgnoreDefense(additionalParams.getIsNpIgnoreDefense());

            // specific damage
            if (additionalParams.hasNpSpecificDamageCondition()) {
                builder.npSpecificDamageCondition(ConditionFactory.buildCondition(additionalParams.getNpSpecificDamageCondition()));

                // base specific damage
                if (additionalParams.getIsNpSpecificDamageOverchargedEffect()) {
                    builder.npSpecificDamageRates(additionalParams.getNpSpecificDamageRateList())
                            .isNpSpecificDamageOverchargedEffect(true)
                            .isOverchargedEffect(true);
                } else {
                    builder.npSpecificDamageRates(getSingletonValueListForLevel(additionalParams.getNpSpecificDamageRateList(), level));
                }

                // specific damage variation
                if (additionalParams.hasNpSpecificDamageVariation()) {
                    final List<Double> additions = additionalParams.getNpSpecificDamageAdditionsList();
                    if (additions.isEmpty()) {
                        throw new IllegalArgumentException("Variation is specified, but value is not provided.");
                    }
                    builder.specificDamageRateVariation(VariationFactory.buildVariation(effectData.getVariationData()));
                    if (additionalParams.getIsNpSpecificDamageAdditionOvercharged()) {
                        builder.isNpSpecificDamageAdditionOvercharged(true)
                                .isOverchargedEffect(true)
                                .npSpecificDamageRateAdditions(additions);
                    } else {
                        builder.npSpecificDamageRateAdditions(getSingletonValueListForLevel(additions, level));
                    }
                }
            }
        }

        return setCommonEffectParams(builder, effectData, level);
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
