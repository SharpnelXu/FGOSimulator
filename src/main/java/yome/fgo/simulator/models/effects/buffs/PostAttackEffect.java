package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.Effect;

import java.util.List;

@SuperBuilder
public class PostAttackEffect extends Buff {
    private List<Effect> effects;

    public void activate(final Simulation simulation) {
        for (final Effect effect : effects) {
            effect.apply(simulation);
        }
    }

    @Override
    public boolean isBuff() {
        return forceBuff > 0;
    }

    @Override
    public boolean isDebuff() {
        return forceBuff < 0;
    }
}
