package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;

@SuperBuilder
public class DecreaseActiveSKillCoolDown extends Effect {
    private final Target target;
    private final List<Integer> values;

    @Override
    protected void internalApply(Simulation simulation, int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {
                combatant.decreaseActiveSkillsCoolDown(values.get(level));
            }
            simulation.setEffectTarget(null);
        }
    }
}
