package yome.fgo.simulator.models.variations;

import lombok.AllArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

@AllArgsConstructor
public class NpAbsorptionVariation implements Variation {
    private final Target target;

    @Override
    public double evaluate(final Simulation simulation, final double baseValue, final double additionValue) {
        int validCombatantCount = 0;
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            if (combatant.isAlly()) {
                final Servant servant = (Servant) combatant;
                if (servant.getCurrentNp() >= additionValue) {
                    validCombatantCount += 1;
                }
            } else {
                if (combatant.getCurrentNpGauge() > 0) {
                    validCombatantCount += 1;
                }
            }
        }

        return  RoundUtils.roundNearest(baseValue + additionValue * validCombatantCount);
    }
}
