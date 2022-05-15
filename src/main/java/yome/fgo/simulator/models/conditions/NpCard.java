package yome.fgo.simulator.models.conditions;

import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.NoblePhantasm;

public class NpCard extends Condition {
    public static final NpCard NP_CARD = new NpCard();

    private NpCard() {}

    @Override
    public boolean evaluate(final Simulation simulation) {
        return simulation.getCurrentCommandCard() instanceof NoblePhantasm;
    }
}
