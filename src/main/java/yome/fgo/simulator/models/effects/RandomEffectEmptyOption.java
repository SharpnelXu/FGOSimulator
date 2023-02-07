package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;

@SuperBuilder
public class RandomEffectEmptyOption extends Effect {
    @Override
    protected void internalApply(final Simulation simulation, final int level) {}
}
