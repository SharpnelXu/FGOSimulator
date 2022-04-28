package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.TargetUtils;

@SuperBuilder
public class DecreaseActiveSKillCoolDown extends IntValuedEffect {
    private final Target target;

    @Override
    protected void internalApply(Simulation simulation, int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {
                combatant.decreaseActiveSkillsCoolDown(values.get(level));
            }
            simulation.unsetEffectTarget();
        }
    }
}
