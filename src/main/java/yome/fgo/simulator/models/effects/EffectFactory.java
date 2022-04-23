package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.simulator.models.conditions.ConditionFactory;
import java.util.stream.Collectors;

public class EffectFactory {
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

            if (effectData.getIsOverchargedEffect()) {
                builder.isOverchargedEffect(true);
            }

            setApplyConditionIfExists(builder, effectData);

            return builder.build();
        } else if (type.equalsIgnoreCase(NpChange.class.getSimpleName())) {
            final NpChange.NpChangeBuilder<?, ?> builder = NpChange.builder()
                    .target(effectData.getTarget())
                    .percentNpChanges(effectData.getValuesList());

            setApplyConditionIfExists(builder, effectData);

            if (effectData.getIsOverchargedEffect()) {
                builder.percentNpChanges(effectData.getValuesList());
                builder.isOverchargedEffect(true);
            } else {
                builder.percentNpChanges(ImmutableList.of(effectData.getValues(level - 1)));
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
