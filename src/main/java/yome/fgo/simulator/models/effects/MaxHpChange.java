package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.buffs.MaxHpBuff;

@SuperBuilder
public class MaxHpChange extends GrantBuff {
    @Override
    protected void afterBuffAdditionalChange(final Simulation simulation) {
        final int change = ((MaxHpBuff) simulation.getCurrentBuff()).getChange();
        simulation.getEffectTarget().changeHpAfterMaxHpChange(change);
    }
}
