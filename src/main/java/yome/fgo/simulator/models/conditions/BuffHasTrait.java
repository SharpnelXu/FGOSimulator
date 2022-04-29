package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;

@AllArgsConstructor
public class BuffHasTrait implements Condition {
    private final String trait;

    @Override
    public boolean evaluate(final Simulation simulation) {
        return simulation.getCurrentBuff().getBuffTraits().stream().anyMatch(trait::equalsIgnoreCase);
    }
}
