package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.SkillEffectivenessUp;
import yome.fgo.simulator.utils.TargetUtils;

import static yome.fgo.simulator.models.effects.HpChange.heal;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@SuperBuilder
public class PercentHpChange extends ValuedEffect {
    private final Target target;
    private final boolean isLethal;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {
                final double skillEffectiveness = simulation.getActivator().applyBuff(simulation, SkillEffectivenessUp.class);
                final int baseChange = (int) ((1 + skillEffectiveness) * combatant.getMaxHp() * getValue(simulation, level));
                heal(simulation, combatant, baseChange, isLethal);
            }
            simulation.unsetEffectTarget();
        }
    }

    @Override
    public String toString() {
        return getTranslation(TARGET_SECTION, target.name()) + super.toString();
    }
}
