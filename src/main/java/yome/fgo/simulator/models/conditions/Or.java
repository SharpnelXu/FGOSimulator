package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;

import java.util.List;

@AllArgsConstructor
public class Or implements Condition {
    public List<Condition> conditions;

    @Override
    public boolean evaluate(final Simulation simulation) {
        for (final Condition condition : conditions) {
            if (condition.evaluate(simulation)) {
                return true;
            }
        }
        return false;
    }
}
