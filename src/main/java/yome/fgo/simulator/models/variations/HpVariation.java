package yome.fgo.simulator.models.variations;

import lombok.AllArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;

import static yome.fgo.simulator.utils.CommandCardTypeUtils.modifierCap;

@AllArgsConstructor
public class HpVariation implements Variation {
    private final double maxHpPercent;
    private final double minHpPercent;
    private final Target target;

    @Override
    public double evaluate(final Simulation simulation, final double baseValue, final double additionValue) {
        final List<Combatant> targets = TargetUtils.getTargets(simulation, target);
        if (targets.size() != 1) {
            throw new IllegalStateException("Doesn't make sense for HpVariation to have more than one target: " + target);
        }
        final Combatant combatant = targets.get(0);

        final double curHpPercent = 1.0 * combatant.getCurrentHp() / combatant.getMaxHp();
        final double appliedBase = curHpPercent > maxHpPercent ? 0 : baseValue;
        final double additionPercent = (maxHpPercent - modifierCap(curHpPercent, maxHpPercent, minHpPercent)) / (maxHpPercent - minHpPercent);

        return  RoundUtils.roundNearest(appliedBase + additionValue * additionPercent);
    }
}
