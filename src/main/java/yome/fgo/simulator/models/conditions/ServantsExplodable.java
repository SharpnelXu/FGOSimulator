package yome.fgo.simulator.models.conditions;

import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;

public class ServantsExplodable implements Condition {
    public static final ServantsExplodable EXPLOOOOOOOOOSION = new ServantsExplodable();

    private ServantsExplodable() {}

    @Override
    public boolean evaluate(final Simulation simulation) {
        for (final Combatant combatant : simulation.currentServants) {
            if (combatant != simulation.getActivator() && combatant.isSelectable()) {
                return true;
            }
        }

        return false;
    }
}
