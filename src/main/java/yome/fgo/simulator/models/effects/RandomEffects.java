package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;

@SuperBuilder
public class RandomEffects extends Effect {
    private int skillLevel;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        if (!shouldApply(simulation)) {
            return;
        }
        final Effect effectToActivate = EffectFactory.buildEffect(simulation.selectRandomEffects(), skillLevel);

        effectToActivate.apply(simulation, level);
    }
}
