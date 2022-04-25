package yome.fgo.simulator.models.conditions;

import yome.fgo.simulator.models.Simulation;

public class BuffIsDebuff implements Condition {
    public static final BuffIsDebuff BUFF_IS_DEBUFF = new BuffIsDebuff();

    private BuffIsDebuff() {}

    @Override
    public boolean evaluate(final Simulation simulation) {
        return simulation.getCurrentBuff().isDebuff();
    }
}
