package yome.fgo.simulator.models.conditions;

import yome.fgo.simulator.models.Simulation;

public class Always extends Condition {
    public static final Always ALWAYS = new Always();

    private Always() {}

    @Override
    public boolean evaluate(final Simulation simulation) {
        return true;
    }

    @Override
    public String toString() {
        return "";
    }
}
