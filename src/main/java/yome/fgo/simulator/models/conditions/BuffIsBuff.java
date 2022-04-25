package yome.fgo.simulator.models.conditions;

import yome.fgo.simulator.models.Simulation;

public class BuffIsBuff implements Condition {
    public static final BuffIsBuff BUFF_IS_BUFF = new BuffIsBuff();

    private BuffIsBuff() {}

    @Override
    public boolean evaluate(final Simulation simulation) {
        return simulation.getCurrentBuff().isBuff();
    }
}
