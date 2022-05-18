package yome.fgo.simulator.models.variations;

import lombok.AllArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.VARIATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@AllArgsConstructor
public class HpAbsorptionVariation extends Variation {
    private final Target target;

    @Override
    public double evaluate(final Simulation simulation, final double baseValue, final double additionValue) {
        double totalAddition = 0;
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            totalAddition += Math.min(combatant.getCurrentHp(), additionValue);
        }

        return  RoundUtils.roundNearest(baseValue + totalAddition);
    }

    @Override
    public String toString() {
        return String.format(
                getTranslation(VARIATION_SECTION, "Absorb from %s"),
                getTranslation(TARGET_SECTION, target.name())
        );
    }
}
