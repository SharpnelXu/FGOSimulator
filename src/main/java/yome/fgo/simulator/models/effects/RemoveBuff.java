package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffFactory;
import yome.fgo.simulator.models.effects.buffs.BuffRemovalResist;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.simulator.models.variations.NoVariation.NO_VARIATION;
import static yome.fgo.simulator.translation.TranslationManager.EFFECT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@SuperBuilder
public class RemoveBuff extends IntValuedEffect {
    private final Target target;
    private final boolean removeFromStart;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        final double probability = getProbability(level);
        final int numToRemove = isOverchargedEffect ? values.get(level - 1) : values.get(0);

        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);

            if (removeFromStart) {
                removeFromStart(simulation, combatant, probability, numToRemove);
            } else {
                removeFromEnd(simulation, combatant, probability, numToRemove);
            }

            simulation.unsetEffectTarget();
        }
    }

    private void removeFromEnd(
            final Simulation simulation,
            final Combatant combatant,
            final double probability,
            final int numToRemove
    ) {
        int removeCount = 0;
        final List<Buff> buffList = combatant.getBuffs();
        for (int j = buffList.size() - 1; j >= 0; j -= 1) {
            final Buff buff = buffList.get(j);

            simulation.setCurrentBuff(buff);

            if (!buff.isIrremovable() && shouldApply(simulation)) {
                final double buffRemovalResist = combatant.applyBuff(simulation, BuffRemovalResist.class);
                if (probability - buffRemovalResist >= simulation.getProbabilityThreshold()) {
                    buffList.remove(j);
                    removeCount += 1;
                }
            }

            simulation.unsetCurrentBuff();

            if (numToRemove > 0 && numToRemove == removeCount) {
                break;
            }
        }
    }

    private void removeFromStart(
            final Simulation simulation,
            final Combatant combatant,
            final double probability,
            final int numToRemove
    ) {
        int removeCount = 0;
        final List<Buff> buffList = combatant.getBuffs();
        for (int i = 0; i < buffList.size() - 1; i += 1) {
            final Buff buff = buffList.get(i);

            simulation.setCurrentBuff(buff);

            if (!buff.isIrremovable() && shouldApply(simulation)) {
                final double buffRemovalResist = combatant.applyBuff(simulation, BuffRemovalResist.class);
                if (probability - buffRemovalResist >= simulation.getProbabilityThreshold()) {
                    buffList.remove(i);
                    i -= 1;
                    removeCount += 1;
                }
            }

            simulation.unsetCurrentBuff();

            if (numToRemove > 0 && numToRemove == removeCount) {
                break;
            }
        }
    }

    @Override
    public String toString() {
        String base = "";
        if (isValueOvercharged) {
            base = "(OC) " + values;
        } else if (values.get(0) > 0){
            base = values.get(0).toString();
        }
        if (variation != NO_VARIATION) {
            base = base + " + ";
            if (isAdditionsOvercharged) {
                base = base + "(OC) " + additions;
            } else {
                base = base + additions.get(0);
            }
            base = base + " " + variation;
        }
        if (removeFromStart) {
            base = base + getTranslation(EFFECT_SECTION, "Remove from start");
        }
        return String.format(
                getTranslation(EFFECT_SECTION, "Remove %s %s buffs %s"),
                getTranslation(TARGET_SECTION, target.name()),
                base,
                miscString()
        );
    }
}
