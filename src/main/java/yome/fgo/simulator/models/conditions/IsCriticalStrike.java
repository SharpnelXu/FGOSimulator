package yome.fgo.simulator.models.conditions;

import yome.fgo.simulator.models.Simulation;

public class IsCriticalStrike extends Condition {
    public static final IsCriticalStrike IS_CRITICAL_STRIKE = new IsCriticalStrike();

    private IsCriticalStrike() {}

    @Override
    public boolean evaluate(final Simulation simulation) {
        return simulation.isCriticalStrike();
    }
}
