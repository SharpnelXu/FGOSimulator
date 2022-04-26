package yome.fgo.simulator.utils;

import yome.fgo.simulator.models.combatants.Combatant;

import static yome.fgo.simulator.utils.CommandCardTypeUtils.modifierCap;

public class HpVariedValuedBuffUtils {
    public static double calculateValue(
            final Combatant combatant,
            final double base,
            final double addition,
            final double maxHpPercent,
            final double minHpPercent
    ) {
        final double curHpPercent = 1.0 * combatant.getCurrentHp() / combatant.getMaxHp();
        final double appliedBase = curHpPercent > maxHpPercent ? 0 : base;
        final double additionPercent = (maxHpPercent - modifierCap(curHpPercent, maxHpPercent, minHpPercent)) / (maxHpPercent - minHpPercent);

        return  RoundUtils.roundNearest(appliedBase + addition * additionPercent);
    }
}
