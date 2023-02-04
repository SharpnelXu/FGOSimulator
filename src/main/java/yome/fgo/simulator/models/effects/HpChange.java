package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

import static yome.fgo.simulator.models.effects.buffs.BuffType.HEAL_EFFECTIVENESS_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.HEAL_GRANT_EFF_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SKILL_EFFECTIVENESS_UP;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@SuperBuilder
public class HpChange extends IntValuedEffect {
    private final Target target;
    private final boolean isLethal;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {
                final double skillEffectiveness = simulation.getActivator().applyBuff(simulation, SKILL_EFFECTIVENESS_UP);
                final int baseChange = (int) ((1 + skillEffectiveness) * getValue(simulation, level));
                heal(simulation, combatant, baseChange, isLethal);
            }
            simulation.unsetEffectTarget();
        }
    }

    public static void heal(final Simulation simulation, final Combatant combatant, final int baseChange, final boolean isLethal) {
        if (baseChange > 0) {
            final double healEffectiveness = combatant.applyBuff(simulation, HEAL_EFFECTIVENESS_BUFF);
            final double healGrantEffectiveness = simulation.getActivator().applyBuff(simulation, HEAL_GRANT_EFF_BUFF);
            final int finalHeal = Math.max(
                    0,
                    (int) RoundUtils.roundNearest(baseChange * (1 + healEffectiveness) * (1 + healGrantEffectiveness))
            );
            combatant.changeHp(finalHeal, false); // heal cannot be lethal (勇者传除外)
        } else {
            combatant.changeHp(baseChange, isLethal);
        }
    }

    @Override
    public String toString() {
        return getTranslation(TARGET_SECTION, target.name()) + super.toString();
    }
}
