package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.HealEffectivenessBuff;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;

@SuperBuilder
public class HpChange extends Effect {
    private final Target target;
    private final boolean isLethal;
    private final boolean isPercentBased;
    private final List<Integer> values;
    private final List<Double> percents;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {
                final int baseChange = isPercentBased ?
                        (int) (percents.get(level - 1) * combatant.getMaxHp()) :
                        values.get(level - 1);

                if (baseChange > 0) {
                    final double healEffectiveness = combatant.applyBuff(simulation, HealEffectivenessBuff.class);
                    final int finalHeal = Math.max(0, (int) RoundUtils.roundNearest(baseChange * (1 + healEffectiveness)));
                    combatant.changeHp(finalHeal);
                } else if (isLethal) {
                    combatant.receiveDamage(baseChange);
                } else {
                    combatant.changeHp(baseChange);
                }
            }
            simulation.unsetEffectTarget();
        }
    }
}
