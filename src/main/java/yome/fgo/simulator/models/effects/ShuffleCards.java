package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;

@SuperBuilder
public class ShuffleCards extends Effect {

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        // fancy shuffle card effect
        // should remove FacelessMoon but meh too much trouble for a buff that don't work here
    }
}
