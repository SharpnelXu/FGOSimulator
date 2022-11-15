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

import static yome.fgo.simulator.ResourceManager.SERVANT_DATA_ANCHOR_MAP;

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
            if (enemyData.getIsServant()) {
                final int id = Integer.parseInt(enemyId.split("servant")[1]);
                this.enemies.add(new Servant(SERVANT_DATA_ANCHOR_MAP.get(id).getServantData(), enemyData));
            } else {
                if (enemyData.hasCombatantDataOverride()) { // always true due to EnemyNode changes
                    this.enemies.add(new Combatant(enemyData.getCombatantDataOverride(), enemyData));
                } else {
                    // just to safeguard
                    this.enemies.add(new Combatant(ResourceManager.getEnemyCombatantData(enemyData.getEnemyCategories(), enemyId), enemyData));
                }
            }
        }
        this.traits = new ArrayList<>(stageData.getTraitsList());
    }

    public void applyStageEffects(final Simulation simulation) {
        for (final Effect effect : effects) {
            effect.apply(simulation);
        }

        simulation.checkBuffStatus();
    }

    public boolean hasMoreEnemies() {
        return !enemies.isEmpty();
    }

    public Combatant getNextEnemy() {
        return enemies.poll();
    }

    private Stage(final Stage other) {
        this.enemies = new LinkedList<>();
        for (final Combatant combatant : other.enemies) {
            this.enemies.add(combatant.makeCopy());
        }
        this.maximumEnemiesOnScreen = other.maximumEnemiesOnScreen;
        this.effects = new ArrayList<>(other.effects);
        this.traits = new ArrayList<>(other.traits);
    }

    public Stage makeCopy() {
        return new Stage(this);
    }
}
