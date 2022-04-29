package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.Effect;

import java.util.List;

@SuperBuilder
public abstract class EffectActivatingBuff extends Buff {
    private List<Effect> effects;

    public void activate(final Simulation simulation) {
        for (final Effect effect : effects) {
            effect.apply(simulation);
        }
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
}
