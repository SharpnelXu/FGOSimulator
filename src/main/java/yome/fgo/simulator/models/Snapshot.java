package yome.fgo.simulator.models;

import lombok.Getter;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.levels.Level;
import yome.fgo.simulator.models.mysticcodes.MysticCode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Getter
public class Snapshot {

    private Level level;

    private int currentStage;
    private int currentTurn;

    private List<Combatant> currentEnemies;
    private Queue<Combatant> backupEnemies;

    private List<Servant> currentServants;
    private LinkedList<Servant> backupServants;

    private MysticCode mysticCode;

    private double currentStars;
    private int currentAllyTargetIndex;
    private int currentEnemyTargetIndex;

    private double fixedRandom;
    private double probabilityThreshold;

    public Snapshot(final Simulation simulation) {
        this.level = simulation.level.makeCopy();

        this.currentStage = simulation.currentStage;
        this.currentTurn = simulation.currentTurn;

        this.currentEnemies = new ArrayList<>();
        for (final Combatant combatant : simulation.currentEnemies) {
            if (combatant == null) {
                this.currentEnemies.add(null);
            } else {
                this.currentEnemies.add(combatant.makeCopy());
            }
        }
        this.backupEnemies = new LinkedList<>();
        for (final Combatant combatant : simulation.backupEnemies) {
            this.backupEnemies.add(combatant.makeCopy());
        }

        this.currentServants = new ArrayList<>();
        for (final Servant servant : simulation.currentServants) {
            if (servant == null) {
                this.currentServants.add(null);
            } else {
                this.currentServants.add(servant.makeCopy());
            }
        }
        this.backupServants = new LinkedList<>();
        for (final Servant servant : simulation.backupServants) {
            this.backupServants.add(servant.makeCopy());
        }

        this.mysticCode = simulation.mysticCode.makeCopy();

        this.currentStars = simulation.currentStars;
        this.currentAllyTargetIndex = simulation.currentAllyTargetIndex;
        this.currentEnemyTargetIndex = simulation.currentEnemyTargetIndex;

        this.fixedRandom = simulation.fixedRandom;
        this.probabilityThreshold = simulation.probabilityThreshold;
    }
}
