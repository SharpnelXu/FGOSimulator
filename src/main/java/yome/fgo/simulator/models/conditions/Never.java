package yome.fgo.simulator.models.conditions;

import yome.fgo.simulator.models.Simulation;

public class Never implements Condition {
    public static final Never NEVER = new Never();

    private Never() {}

    @Override
    public boolean evaluate(final Simulation simulation) {
        return false;
    }
}
