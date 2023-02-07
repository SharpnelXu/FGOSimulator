package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;

import java.util.List;

@SuperBuilder
public class RandomEffectOption extends Effect {
    private List<Effect> effects;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        if (shouldApply(simulation)) {
            for (final Effect effect : effects) {
                effect.apply(simulation, level);
            }
        }
    }
}
