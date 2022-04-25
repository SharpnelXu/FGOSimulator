package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;

@SuperBuilder
public class RemoveBuff extends Effect {
    private final FgoStorageData.Target target;
    private final List<Integer> numToRemove;

    @Override
    public void internalApply(final Simulation simulation, final int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            int removeCount = 0;
            final List<Buff> buffList = combatant.getBuffs();

            for (int j = buffList.size() - 1; j >= 0; j--) {
                final Buff buff = buffList.get(j);

                simulation.setCurrentBuff(buff);
                if (!buff.isIrremovable() && shouldApply(simulation)) {
                    buffList.remove(j);
                    removeCount++;

                    if (removeCount == numToRemove.get(level)) {
                        break;
                    }
                }
                simulation.setCurrentBuff(null);
            }
        }
    }
}
