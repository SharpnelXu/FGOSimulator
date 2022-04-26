package yome.fgo.simulator.models.levels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.StageData;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.effects.Effect;
import yome.fgo.simulator.models.effects.EffectFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@AllArgsConstructor
@Getter
public class Stage {
    private final Queue<Combatant> enemies;
    private final int maximumEnemiesOnScreen;
    private final List<Effect> effects;
    private final List<String> traits;

    public Stage(final Collection<Combatant> enemies, final int maximumEnemiesOnScreen, final List<Effect> effects) {
        this.enemies = new LinkedList<>(enemies);
        this.maximumEnemiesOnScreen = maximumEnemiesOnScreen;
        this.effects = new ArrayList<>(effects);
        this.traits = new ArrayList<>();
    }

    public Stage(final StageData stageData) {
        if (stageData.getMaximumEnemiesOnScreen() == 0) {
            this.maximumEnemiesOnScreen = 3;
        } else {
            this.maximumEnemiesOnScreen = stageData.getMaximumEnemiesOnScreen();
        }
        this.effects = EffectFactory.buildEffects(stageData.getEffectsList(), 1);

        this.enemies = new LinkedList<>();
        for (final EnemyData enemyData : stageData.getEnemyDataList()) {
            final String enemyId = enemyData.getEnemyBaseId();
            if (enemyData.getServantAscension() != 0) {
                this.enemies.add(new Servant(ResourceManager.getServantData(enemyId), enemyData));
            } else {
                this.enemies.add(new Combatant(ResourceManager.getEnemyCombatantData(enemyData.getEnemyCategories(), enemyId), enemyData));
            }
        }
        this.traits = new ArrayList<>(stageData.getTraitsList());
    }

    public void applyStageEffects(final Simulation simulation) {
        for (final Effect effect : effects) {
            effect.apply(simulation);
        }
    }

    public boolean hasMoreEnemies() {
        return !enemies.isEmpty();
    }

    public Combatant getNextEnemy() {
        return enemies.poll();
    }
}
