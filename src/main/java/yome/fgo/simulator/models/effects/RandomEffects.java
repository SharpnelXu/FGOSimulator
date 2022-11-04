package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.SpecialActivationParams;
import yome.fgo.simulator.models.Simulation;

import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.SpecialActivationTarget.RANDOM_EFFECT;

@SuperBuilder
public class RandomEffects extends Effect {
    private int skillLevel;
    private List<EffectData> effectData;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        if (!shouldApply(simulation)) {
            return;
        }

        final SpecialActivationParams specialActivationParams = SpecialActivationParams.newBuilder()
                .setSpecialTarget(RANDOM_EFFECT)
                .addAllRandomEffectSelections(effectData)
                .build();
        simulation.requestSpecialActivationTarget(specialActivationParams);
        final Effect effectToActivate = EffectFactory.buildEffect(simulation.selectRandomEffects(), skillLevel);

        effectToActivate.apply(simulation, level);
    }
}
