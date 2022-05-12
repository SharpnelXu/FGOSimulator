package yome.fgo.simulator.models.levels;

import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.LevelData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.Effect;
import yome.fgo.simulator.models.effects.EffectFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class Level {
    private final String name;
    private final List<Stage> stages;
    private final List<Effect> effects;

    public Level(final String name, final List<Stage> stages, final List<Effect> effects) {
        this.name = name;
        this.stages = new ArrayList<>(stages);
        this.effects = new ArrayList<>(effects);
    }

    public Level(final LevelData levelData) {
        this.name = levelData.getId();
        this.stages = levelData.getStageDataList().stream().map(Stage::new).collect(Collectors.toList());
        this.effects = EffectFactory.buildEffects(levelData.getEffectsList());
    }

    public void applyLevelEffects(final Simulation simulation) {
        for (final Effect effect : effects) {
            effect.apply(simulation);
        }

        simulation.checkBuffStatus();
    }

    public boolean hasNextStage(final int currentStage) {
        return currentStage < stages.size();
    }

    public Stage getStage(final int currentStage) {
        return stages.get(currentStage - 1);
    }
}
