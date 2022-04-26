package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;

@AllArgsConstructor
public class StageHasTrait implements Condition {
    private final String trait;

    @Override
    public boolean evaluate(final Simulation simulation) {
        return simulation.getLevel().getStage(simulation.getCurrentStage()).getTraits().contains(trait);
    }
}
