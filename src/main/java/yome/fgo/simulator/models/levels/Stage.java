package yome.fgo.simulator.models.levels;

import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.Effect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Stage {
    private Queue<Combatant> enemies;
    private int maximumEnemiesOnScreen;
    private List<Effect> effects;

    public Stage(final Collection<Combatant> enemies, final int maximumEnemiesOnScreen, final List<Effect> effects) {
        this.enemies = new LinkedList<>(enemies);
        this.maximumEnemiesOnScreen = maximumEnemiesOnScreen;
        this.effects = new ArrayList<>(effects);
    }

    public void applyStageEffects(final Simulation simulation) {
        for (final Effect effect : effects) {
            effect.apply(simulation);
        }
    }

    public int getMaximumEnemiesOnScreen() {
        return maximumEnemiesOnScreen;
    }

    public boolean hasMoreEnemies() {
        return !enemies.isEmpty();
    }

    public Combatant getEnemy() {
        return enemies.poll();
    }
}
