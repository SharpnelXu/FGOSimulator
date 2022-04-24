package yome.fgo.simulator.models.levels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.StageData;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
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

    public Stage(final Collection<Combatant> enemies, final int maximumEnemiesOnScreen, final List<Effect> effects) {
        this.enemies = new LinkedList<>(enemies);
        this.maximumEnemiesOnScreen = maximumEnemiesOnScreen;
        this.effects = new ArrayList<>(effects);
    }

    public Stage(final StageData stageData) {
        this.maximumEnemiesOnScreen = stageData.getMaximumEnemiesOnScreen();
        this.effects = EffectFactory.buildEffects(stageData.getEffectsList(), 1);

        this.enemies = new LinkedList<>();
        for (final EnemyData enemyData : stageData.getEnemyDataList()) {
            final CombatantData combatantData = enemyData.getServantAscension() != 0 ?
                    ResourceManager.getServantCombatantData(enemyData.getEnemyBaseId(), enemyData.getServantAscension()) :
                    ResourceManager.getEnemyCombatantData(enemyData.getEnemyBaseId());
            this.enemies.add(new Combatant(combatantData, enemyData));
        }
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
