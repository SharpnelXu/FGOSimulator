package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;

@AllArgsConstructor
public class ServantsExplodable implements Condition {
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
