package yome.fgo.simulator.utils;

import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;

public class SpecificDamageUtils {
    private static final int MAX_COUNT = 10;

    public static int countBuff(final Simulation simulation, final Target target, final Class<?> targetBuff) {
        int count = 0;
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            for (final Buff buff : combatant.getBuffs()) {
                if (targetBuff.isInstance(buff)) {
                    count++;
                    if (MAX_COUNT == count) {
                        return MAX_COUNT;
                    }
                }
            }
        }
        return count;
    }

    public static int countTrait(final Simulation simulation, final Target target, final String targetTrait) {
        int count = 0;
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            for (final String trait : combatant.getAllTraits(simulation)) {
                if (targetTrait.equalsIgnoreCase(trait)) {
                    count++;
                    if (MAX_COUNT == count) {
                        return MAX_COUNT;
                    }
                }
            }
        }
        return count;
    }
}
