package yome.fgo.simulator.models.conditions;

import yome.fgo.simulator.models.Simulation;

public interface Condition {
    boolean evaluate(final Simulation simulation);
}
