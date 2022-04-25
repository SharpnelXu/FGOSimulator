package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;

@AllArgsConstructor
public class BuffTypeEquals implements Condition {
    private final Class<?> targetBuffType;

    @Override
    public boolean evaluate(final Simulation simulation) {
        return targetBuffType.isInstance(simulation.getCurrentBuff());
    }
}
