package yome.fgo.simulator.models.variations;

import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.utils.RoundUtils;

public class TurnPassVariation extends Variation {
    public static final TurnPassVariation TURN_PASS_VARIATION = new TurnPassVariation();

    private TurnPassVariation() {}

    @Override
    public double evaluate(final Simulation simulation, final double baseValue, final double additionValue) {
        final int numTurnPassed = simulation.getCurrentBuff().getTurnPassed();
        return RoundUtils.roundNearest(baseValue + numTurnPassed * additionValue);
    }
}
