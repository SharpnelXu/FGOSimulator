package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class And extends Condition {
    public List<Condition> conditions;

    @Override
    public boolean evaluate(final Simulation simulation) {
        boolean match = true;
        for (final Condition condition : conditions) {
            match &= condition.evaluate(simulation);
        }
        return match;
    }

    @Override
    public String toString() {
        return super.toString() +
                ": " +
                conditions.stream().map(Condition::toString).collect(Collectors.toList());
    }
}
