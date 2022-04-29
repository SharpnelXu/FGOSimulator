package yome.fgo.simulator.models.conditions;

import yome.fgo.simulator.models.Simulation;

public class IsCriticalStrike implements Condition {
    @Override
    public boolean evaluate(final Simulation simulation) {
        return simulation.isCriticalStrike();
    }
}
