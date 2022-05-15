package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;

import java.text.NumberFormat;

@AllArgsConstructor
public class HpPercentAtMost extends Condition {
    private final double value;

    @Override
    public boolean evaluate(final Simulation simulation) {
        final Combatant combatant = simulation.getActivator();

        if (combatant instanceof Servant) {
            final Servant servant = (Servant) combatant;
            return 1.0 * servant.getCurrentHp() / servant.getMaxHp() <= value;
        }

        return false;
    }

    @Override
    public String toString() {
        final NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMaximumFractionDigits(2);
        return super.toString() + ": " + numberFormat.format(value);
    }
}
