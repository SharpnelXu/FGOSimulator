package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;

import java.util.List;

@SuperBuilder
public class RandomEffects extends Effect {
    private List<Effect> effectList;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        final Effect effectToActivate = simulation.selectRandomEffects(effectList);

        effectToActivate.apply(simulation, level);
    }
}
