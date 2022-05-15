package yome.fgo.simulator.models.conditions;

import yome.fgo.simulator.models.Simulation;

public class BuffRemovable extends Condition {
    public static final BuffRemovable BUFF_REMOVABLE = new BuffRemovable();

    public BuffRemovable() {}

    @Override
    public boolean evaluate(final Simulation simulation) {
        return !simulation.getCurrentBuff().isIrremovable();
    }
}
