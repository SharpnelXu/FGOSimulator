package yome.fgo.simulator.models.effects;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffRemovalResist;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;

@SuperBuilder
public class RemoveBuff extends Effect {
    private final Target target;

    @Builder.Default
    private final List<Integer> numToRemove = Lists.newArrayList(0);

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            int removeCount = 0;
            final List<Buff> buffList = combatant.getBuffs();

            for (int j = buffList.size() - 1; j >= 0; j--) {
                final Buff buff = buffList.get(j);

                simulation.setCurrentBuff(buff);
                if (!buff.isIrremovable() && shouldApply(simulation)) {
                    final double buffRemovalResist = combatant.applyBuff(simulation, BuffRemovalResist.class);
                    if (1 - buffRemovalResist < simulation.getProbabilityThreshold()) {
                        continue;
                    }

                    buffList.remove(j);
                    removeCount++;

                    if (removeCount == numToRemove.get(level - 1)) {
                        break;
                    }
                }
                simulation.setCurrentBuff(null);
            }
            simulation.setEffectTarget(null);
        }
    }
}
