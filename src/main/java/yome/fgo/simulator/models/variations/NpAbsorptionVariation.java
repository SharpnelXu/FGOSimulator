package yome.fgo.simulator.models.variations;

import lombok.AllArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.VARIATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@AllArgsConstructor
public class NpAbsorptionVariation extends Variation {
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

    @Override
    public String toString() {
        return String.format(
                getTranslation(VARIATION_SECTION, "Absorb from %s"),
                getTranslation(TARGET_SECTION, target.name())
        );
    }
}
