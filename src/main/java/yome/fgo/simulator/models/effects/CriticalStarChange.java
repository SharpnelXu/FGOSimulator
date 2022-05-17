package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;

@SuperBuilder
public class CriticalStarChange extends IntValuedEffect {
    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        simulation.gainStar(getValue(simulation, level));
    }
}
