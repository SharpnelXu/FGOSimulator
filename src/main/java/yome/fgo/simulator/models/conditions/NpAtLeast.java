package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;

@AllArgsConstructor
public class NpAtLeast implements Condition {
    private final double value;

    @Override
    public boolean evaluate(final Simulation simulation) {
        final Combatant combatant = simulation.getActivator();

        if (combatant instanceof Servant) {
            final Servant servant = (Servant) combatant;
            return servant.getCurrentNp() >= value;
        }

        return false;
    }
}
