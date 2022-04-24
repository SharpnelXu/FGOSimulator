package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.simulator.models.conditions.ConditionFactory;

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
            } else {
                builder.numStarsGains(ImmutableList.of((int) effectData.getValues(level - 1)));
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
                builder.probability(effectData.getProbabilities(level));
            }

            if (effectData.getIsOverchargedEffect()) {
                builder.isOverchargedEffect(true);
            }

            setApplyConditionIfExists(builder, effectData);

            return builder.build();
        } else if (type.equalsIgnoreCase(NpChange.class.getSimpleName())) {
            final NpChange.NpChangeBuilder<?, ?> builder = NpChange.builder()
                    .target(effectData.getTarget())
                    .npChanges(effectData.getValuesList());

            setApplyConditionIfExists(builder, effectData);

            if (effectData.getIsOverchargedEffect()) {
                builder.npChanges(effectData.getValuesList());
                builder.isOverchargedEffect(true);
            } else {
                builder.npChanges(ImmutableList.of(effectData.getValues(level - 1)));
            }

            return builder.build();
        } else if (type.equalsIgnoreCase(NoblePhantasmDamage.class.getSimpleName())) {
            final NoblePhantasmDamage.NoblePhantasmDamageBuilder<?, ?> builder = NoblePhantasmDamage.builder()
                    .target(effectData.getTarget());

            if (effectData.getIsNpSpecificDamageOverchargedEffect()) {
                builder.npSpecificDamageRates(effectData.getNpSpecificDamageRateList())
                        .isNpSpecificDamageOverchargedEffect(effectData.getIsNpSpecificDamageOverchargedEffect());
            } else if (effectData.getNpSpecificDamageRateCount() == 0) {
                builder.npSpecificDamageRates(ImmutableList.of(1.0));
            } else if (effectData.getNpSpecificDamageRateCount() == 1) {
                builder.npSpecificDamageRates(effectData.getNpSpecificDamageRateList());
            } else {
                builder.npSpecificDamageRates(ImmutableList.of(effectData.getNpSpecificDamageRate(level - 1)));
            }

            setApplyConditionIfExists(builder, effectData);

            if (effectData.getIsOverchargedEffect()) {
                builder.damageRates(effectData.getValuesList());
                builder.isOverchargedEffect(true);
            } else {
                builder.damageRates(ImmutableList.of(effectData.getValues(level - 1)));
            }

            return builder.build();
        }

        throw new UnsupportedOperationException("Effect type unsupported: " + type);
    }

    private static void setApplyConditionIfExists(Effect.EffectBuilder<?, ?> builder, EffectData effectData) {
        if (effectData.hasApplyCondition()) {
            builder.applyCondition(ConditionFactory.buildCondition(effectData.getApplyCondition()));
        }
    }
}
