package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;

import java.util.List;

@SuperBuilder
public class CriticalStarChange extends Effect {
    public final List<Integer> numStarsGains;

    @Override
    public void internalApply(final Simulation simulation, final int level) {
        simulation.gainStar(numStarsGains.get(level - 1));
    }
}
