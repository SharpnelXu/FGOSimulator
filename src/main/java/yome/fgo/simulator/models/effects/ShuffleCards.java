package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;

@SuperBuilder
public class ShuffleCards extends Effect {
    public static final ShuffleCards SHUFFLE_CARDS = ShuffleCards.builder().build();

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        // fancy shuffle card effect
    }
}
