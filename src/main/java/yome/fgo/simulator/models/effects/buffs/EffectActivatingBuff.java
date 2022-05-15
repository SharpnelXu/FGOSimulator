package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.Effect;

import java.util.List;
import java.util.stream.Collectors;

@SuperBuilder
public abstract class EffectActivatingBuff extends Buff {
    private List<Effect> effects;

    public void activate(final Simulation simulation) {
        for (final Effect effect : effects) {
            effect.apply(simulation);
        }

        simulation.checkBuffStatus();
    }

    @Override
    public boolean commonBuffCondition() {
        return false;
    }

    @Override
    public boolean commonDebuffCondition() {
        return false;
    }

    @Override
    protected boolean commonStackableCondition() {
        return true;
    }

    @Override
    public String toString() {
        String base = ": " + effects.stream().map(Effect::toString).collect(Collectors.toList());
        return super.toString() + base;
    }
}
