package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;

import java.util.List;

@AllArgsConstructor
public class And implements Condition {
    public List<Condition> conditions;

    @Override
    public boolean evaluate(final Simulation simulation) {
        boolean match = true;
        for (final Condition condition : conditions) {
            match &= condition.evaluate(simulation);
        }
        return match;
    }
}
