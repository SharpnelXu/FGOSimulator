package yome.fgo.simulator.models.levels;

import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.Effect;

import java.util.ArrayList;
import java.util.List;

public class Level {
    private String name;
    private List<Stage> stages;
    private List<Effect> effects;

    public Level(final String name, final List<Stage> stages, final List<Effect> effects) {
        this.name = name;
        this.stages = new ArrayList<>(stages);
        this.effects = new ArrayList<>(effects);
    }

    public void applyLevelEffects(final Simulation simulation) {
        for (final Effect effect : effects) {
            effect.apply(simulation);
        }
    }

    public boolean hasNextStage(final int currentStage) {
        return currentStage < stages.size() - 1;
    }

    public Stage getStage(final int index) {
        return stages.get(index);
    }
}
