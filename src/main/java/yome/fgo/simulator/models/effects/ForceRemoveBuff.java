package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.buffs.Buff;

@SuperBuilder
public class ForceRemoveBuff extends RemoveBuff {
    @Override
    public boolean shouldRemove(final Buff buff, final Simulation simulation) {
        return shouldApply(simulation);
    }
}
