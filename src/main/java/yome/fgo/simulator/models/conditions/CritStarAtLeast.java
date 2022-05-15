package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;

@AllArgsConstructor
public class CritStarAtLeast extends Condition {
    private final int value;

    @Override
    public boolean evaluate(final Simulation simulation) {
        return simulation.getCurrentStars() >= value;
    }

    @Override
    public String toString() {
        return super.toString() + ": " + value;
    }
}
