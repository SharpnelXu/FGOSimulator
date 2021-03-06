package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;

@AllArgsConstructor
public class Not extends Condition {
    public Condition condition;

    @Override
    public boolean evaluate(final Simulation simulation) {
        return !condition.evaluate(simulation);
    }

    @Override
    public String toString() {
        return super.toString() +
                ": " +
                condition;
    }
}
