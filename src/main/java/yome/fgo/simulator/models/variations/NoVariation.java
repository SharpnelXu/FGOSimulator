package yome.fgo.simulator.models.variations;

import yome.fgo.simulator.models.Simulation;

public class NoVariation implements Variation {
    public static final NoVariation NO_VARIATION = new NoVariation();

    private NoVariation() {}

    @Override
    public double evaluate(final Simulation simulation, final double baseValue, final double additionValue) {
        return baseValue;
    }
}
