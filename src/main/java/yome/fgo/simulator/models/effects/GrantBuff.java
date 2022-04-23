package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.buffs.Buff;

@SuperBuilder
public class GrantBuff extends Effect {
    private Buff buff;

    @Override
    public void apply(Simulation simulation, int level) {

    }
}
