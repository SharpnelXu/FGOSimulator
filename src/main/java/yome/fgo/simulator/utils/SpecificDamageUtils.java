package yome.fgo.simulator.utils;

import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.conditions.Condition;
import yome.fgo.simulator.models.effects.buffs.Buff;

public class SpecificDamageUtils {
    private static final int MAX_COUNT = 10;

    public static int countBuff(
            final Simulation simulation,
            final Target target,
            final Condition matchCondition,
            final int maxCount
    ) {
        int count = 0;
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            for (final Buff buff : combatant.getBuffs()) {
                simulation.setCurrentBuff(buff);

                if (matchCondition.evaluate(simulation)) {
                    count++;
                    if (maxCount == count) {
                        return maxCount;
                    }
                }

                simulation.unsetCurrentBuff();
            }
        }
        return count;
    }

    public static int countBuff(
            final Simulation simulation,
            final Target target,
            final Class<?> targetBuff,
            final int maxCount
    ) {
        int count = 0;
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            for (final Buff buff : combatant.getBuffs()) {
                if (targetBuff.isInstance(buff)) {
                    count++;
                    if (maxCount == count) {
                        return maxCount;
                    }
                }
            }
        }
        return count;
    }

    public static int countBuff(final Simulation simulation, final Target target, final Class<?> targetBuff) {
        return countBuff(simulation, target, targetBuff, MAX_COUNT);
    }

    public static int countTrait(
            final Simulation simulation,
            final Target target,
            final String targetTrait,
            final int maxCount
    ) {
        int count = 0;
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            for (final String trait : combatant.getAllTraits(simulation)) {
                if (targetTrait.equalsIgnoreCase(trait)) {
                    count++;
                    if (maxCount == count) {
                        return maxCount;
                    }
                }
            }
        }
        return count;
    }

    public static int countTrait(final Simulation simulation, final Target target, final String targetTrait) {
        return countTrait(simulation, target, targetTrait, MAX_COUNT);
    }
}
