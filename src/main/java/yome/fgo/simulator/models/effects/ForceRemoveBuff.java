package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;

@SuperBuilder
public class ForceRemoveBuff extends RemoveBuff {
    @Override
    public boolean shouldRemove(
            final Buff buff,
            final Simulation simulation,
            final Combatant combatant,
            final double probability
    ) {
        return shouldApply(simulation);
    }
}
