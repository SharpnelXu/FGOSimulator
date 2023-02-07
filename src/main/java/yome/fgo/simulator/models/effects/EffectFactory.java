package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.NpDamageAdditionalParams;
import yome.fgo.simulator.models.conditions.ConditionFactory;
import yome.fgo.simulator.models.effects.Effect.EffectType;
import yome.fgo.simulator.models.variations.VariationFactory;

import java.util.List;
import java.util.stream.Collectors;

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
        return switch (EffectType.ofType(effectData.getType())) {
            case GRANT_BUFF -> setGrantBuffEffectValue(GrantBuff.builder(), effectData, level);
            case FORCE_GRANT_BUFF -> setGrantBuffEffectValue(ForceGrantBuff.builder(), effectData, level);
            case REMOVE_BUFF -> setIntValuedEffectValue(
                    RemoveBuff.builder()
                            .target(effectData.getTarget())
                            .removeFromStart(effectData.getRemoveFromStart()),
                    effectData,
                    level
            );
            case FORCE_REMOVE_BUFF -> setIntValuedEffectValue(
                    ForceRemoveBuff.builder()
                            .target(effectData.getTarget())
                            .removeFromStart(effectData.getRemoveFromStart()),
                    effectData,
                    level
            );
            case CRITICAL_STAR_CHANGE -> setIntValuedEffectValue(CriticalStarChange.builder(), effectData, level);
            case NP_CHANGE ->
                    setValuedEffectValue(NpChange.builder().target(effectData.getTarget()), effectData, level);
            case NP_GAUGE_CHANGE -> setIntValuedEffectValue(
                    NpGaugeChange.builder().target(effectData.getTarget()),
                    effectData,
                    level
            );
            case HP_CHANGE -> effectData.getIsHpChangePercentBased() ?
                    setValuedEffectValue(
                            PercentHpChange.builder()
                                    .isLethal(effectData.getIsLethal())
                                    .target(effectData.getTarget()), effectData, level) :
                    setIntValuedEffectValue(
                            HpChange.builder()
                                    .isLethal(effectData.getIsLethal())
                                    .target(effectData.getTarget()), effectData, level);
            case NOBLE_PHANTASM_DAMAGE -> setNpDamageParams(NoblePhantasmDamage.builder(), effectData, level);
            case DECREASE_ACTIVE_SKILL_COOL_DOWN -> setIntValuedEffectValue(
                    DecreaseActiveSkillCoolDown.builder().target(effectData.getTarget()),
                    effectData,
                    level
            );
            case INSTANT_DEATH ->
                    setEffectParams(InstantDeath.builder().target(effectData.getTarget()), effectData, level);
            case FORCE_INSTANT_DEATH -> setEffectParams(
                    ForceInstantDeath.builder().target(effectData.getTarget()),
                    effectData,
                    level
            );
            case ASCENSION_CHANGE -> setIntValuedEffectValue(
                    AscensionChange.builder().target(effectData.getTarget()),
                    effectData,
                    level
            );
            case BUFF_ABSORPTION ->
                    setEffectParams(BuffAbsorption.builder().target(effectData.getTarget()), effectData, level);
            case CARD_TYPE_CHANGE_SELECT -> setGrantBuffEffectValue(
                    CardTypeChangeSelect.builder().allowedCardTypes(effectData.getCardTypeSelectionsList()),
                    effectData,
                    level
            );
            case ORDER_CHANGE -> setEffectParams(OrderChange.builder(), effectData, level);
            case MOVE_TO_LAST_BACKUP -> setEffectParams(MoveToLastBackup.builder(), effectData, level);
            case SHUFFLE_CARDS -> setEffectParams(ShuffleCards.builder(), effectData, level);
            case RANDOM_EFFECTS -> setEffectParams(
                    RandomEffects.builder().skillLevel(level).effectData(effectData.getEffectDataList()),
                    effectData,
                    level
            );
            case RANDOM_EFFECT_OPTION -> setEffectParams(
                    RandomEffectOption.builder().effects(buildEffects(effectData.getEffectDataList(), level)),
                    effectData,
                    level
            );
            case RANDOM_EFFECT_EMPTY_OPTION -> setEffectParams(
                    RandomEffectEmptyOption.builder(),
                    effectData,
                    level
            );
        };
    }

    private static Effect setEffectParams(
            Effect.EffectBuilder<?, ?> builder,
            EffectData effectData,
            final int level
    ) {
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

    private static Effect setValuedEffectValue(
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

        return setEffectParams(builder, effectData, level);
    }

    private static Effect setIntValuedEffectValue(
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

        return setEffectParams(builder, effectData, level);
    }

    private static Effect setGrantBuffEffectValue(
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

        return setEffectParams(builder, effectData, level);
    }

    private static Effect setNpDamageParams(
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
                    builder.npSpecificDamageRates(getSingletonValueListForLevel(
                            additionalParams.getNpSpecificDamageRateList(),
                            level
                    ));
                }

                // specific damage variation
                if (additionalParams.hasNpSpecificDamageVariation()) {
                    final List<Double> additions = additionalParams.getNpSpecificDamageAdditionsList();
                    if (additions.isEmpty()) {
                        throw new IllegalArgumentException("Variation is specified, but value is not provided.");
                    }
                    builder.specificDamageRateVariation(VariationFactory.buildVariation(additionalParams.getNpSpecificDamageVariation()));
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

        return setEffectParams(builder, effectData, level);
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
