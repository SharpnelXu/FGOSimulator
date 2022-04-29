package yome.fgo.simulator.models.variations;

import yome.fgo.simulator.models.Simulation;

public interface Variation {
    double evaluate(final Simulation simulation, final double baseValue, final double additionValue);
}
