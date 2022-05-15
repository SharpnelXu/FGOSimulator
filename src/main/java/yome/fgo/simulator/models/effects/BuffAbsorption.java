package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.ArrayList;
import java.util.List;

import static yome.fgo.simulator.translation.TranslationManager.EFFECT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@SuperBuilder
public class BuffAbsorption extends Effect {
    private final Target target;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        final List<Buff> buffsToMove = new ArrayList<>();
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);

            final List<Buff> buffs = combatant.getBuffs();
            for (int j = buffs.size() - 1; j >= 0; j -= 1) {
                final Buff buff = buffs.get(j);
                simulation.setCurrentBuff(buff);
                if (!buff.isIrremovable() && shouldApply(simulation)) {
                    buffsToMove.add(buffs.remove(j));
                }

                simulation.unsetCurrentBuff();
            }

            simulation.unsetEffectTarget();
        }

        // preserve addition order
        final Combatant activator = simulation.getActivator();
        for (int j = buffsToMove.size() - 1; j >= 0; j -= 1) {
            activator.addBuff(buffsToMove.get(j));
        }
    }

    @Override
    public String toString() {
        return getTranslation(EFFECT_SECTION, "From") + getTranslation(TARGET_SECTION, target.name()) + super.toString();
    }
}
