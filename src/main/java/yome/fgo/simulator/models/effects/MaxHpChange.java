package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffFactory;
import yome.fgo.simulator.models.effects.buffs.MaxHpBuff;
import yome.fgo.simulator.utils.TargetUtils;

@SuperBuilder
public class MaxHpChange extends GrantBuff {
    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {
                final Buff buff = BuffFactory.buildBuff(buffData.get(level - 1), buffLevel);
                simulation.setCurrentBuff(buff);

                grantBuff(simulation, buff, combatant, probability);
                final int change = ((MaxHpBuff) buff).getChange();
                combatant.changeHpAfterMaxHpChange(change);

                simulation.setCurrentBuff(null);
            }
            simulation.setEffectTarget(null);
        }
    }
}
