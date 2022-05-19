package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;

@AllArgsConstructor
public class HpAtLeast extends Condition {
    private final int value;

    @Override
    public boolean evaluate(final Simulation simulation) {
        final Combatant combatant = simulation.getActivator();

        return value <= combatant.getCurrentHp();
    }

    @Override
    public String toString() {
        return super.toString() + ": " + value;
    }
}
