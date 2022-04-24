package yome.fgo.simulator.models;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.simulator.models.combatants.CombatAction;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.CommandCard;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.effects.Effect;
import yome.fgo.simulator.models.effects.NpChange;
import yome.fgo.simulator.models.effects.CriticalStarChange;
import yome.fgo.simulator.models.levels.Level;
import yome.fgo.simulator.models.levels.Stage;
import yome.fgo.simulator.models.mysticcodes.MysticCode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_ALLIES;

@NoArgsConstructor
@Getter
@Setter
public class Simulation {
    public static final int MAXIMUM_SERVANTS_ON_SCREEN = 3;
    public static final int MAXIMUM_CARDS_PER_TURN = 3;
    public static final Effect ARTS_CHAIN_EFFECT = NpChange.builder()
            .target(ALL_ALLIES)
            .percentNpChanges(ImmutableList.of(20.0))
            .build();
    public static final Effect QUICK_CHAIN_EFFECT = CriticalStarChange.builder()
            .numStarsGains(ImmutableList.of(10))
            .build();

    public Simulation(
            final Level level,
            final List<Servant> servants,
            final MysticCode mysticCode
    ) {
        this.level = level;
        this.backupServants.addAll(servants);
        this.mysticCode = mysticCode;
    }

    public Level level;

    public int currentStage;
    public int currentTurn;

    public List<Combatant> currentEnemies = new ArrayList<>();
    public Queue<Combatant> backupEnemies = new LinkedList<>();

    public List<Servant> currentServants = new ArrayList<>();
    public LinkedList<Servant> backupServants = new LinkedList<>();

    public MysticCode mysticCode;

    public double currentStars;
    public int currentAllyTargetIndex;
    public int currentBackupTargetIndex;
    public int currentEnemyTargetIndex;

    public double fixedRandom;
    public double probabilityThreshold;

    // condition related fields
    private Servant attacker;
    private Combatant defender;
    private Combatant activator;
    private CommandCard currentCommandCard;
    private Combatant effectTarget;

    public void initiate() {
        currentStage = 0;
        currentTurn = 1;
        currentStars = 0;

        // populate ally
        while (!backupServants.isEmpty() && currentServants.size() < MAXIMUM_SERVANTS_ON_SCREEN) {
            currentServants.add(backupServants.poll());
        }

        populateStageWithEnemies(currentStage);

        // need to wait for all combatants to be populated before initiating
        final List<Combatant> allCombatants = new ArrayList<>();
        allCombatants.addAll(currentServants);
        allCombatants.addAll(backupServants);
        allCombatants.addAll(currentEnemies);
        allCombatants.addAll(backupEnemies);
        for (final Combatant combatant : allCombatants) {
            combatant.initiate(this);
        }

        for (final Servant servant : currentServants) {
            servant.enterField(this);
        }

        for (final Combatant combatant : currentEnemies) {
            combatant.enterField(this);
        }

        // level effect & stage effect
        level.applyLevelEffects(this);
        level.getStage(currentStage).applyStageEffects(this);
    }

    public boolean isSimulationCompleted() {
        return currentServants.stream().allMatch(Objects::isNull) || isAllEnemiesDead();
    }

    public void render() {
        // TODO: display method
    }
    public void activateServantSkill(final int servantIndex, final int activeSkillIndex) {
        currentServants.get(servantIndex).activateActiveSkill(this, activeSkillIndex);
    }

    public void activateMysticCodeSkill(final int skillIndex) {
        mysticCode.activateSkill(this, skillIndex);
    }

    public void executeCombatActions(final List<CombatAction> combatActions) {
        // TODO: move to front end
        assert !combatActions.isEmpty() && combatActions.size() <= MAXIMUM_CARDS_PER_TURN;

        // calculate chain
        final boolean typeChain = isTypeChain(combatActions);
        final CommandCardType firstCardType = getCommandCardType(combatActions.get(0));
        if (typeChain && firstCardType != BUSTER) {
            applyTypeChainEffect(firstCardType);
        }

        // apply command card effects
        int extraOvercharge = 0;
        for (int i = 0; i < combatActions.size(); i++) {
            final CombatAction combatAction = combatActions.get(i);
            final Servant servant = currentServants.get(combatAction.servantIndex);
            if (servant == null || isAllEnemiesDead()) { // servant dead in previous attack or all enemies dead
                continue;
            }

            if (combatAction.isNoblePhantasm) {
                servant.activateNoblePhantasm(this, extraOvercharge);
                extraOvercharge++;

                for (final Combatant combatant : getAliveEnemies()) {
                    combatant.clearCumulativeTurnDamage();
                }
            } else {
                servant.activateCommandCard(this, combatAction.commandCardIndex, i, combatAction.isCriticalStrike, typeChain, firstCardType);
                extraOvercharge = 0;
            }

            if (shouldRemoveDeadCombatants(combatActions, i)) {
                removeDeadEnemies();
                removeDeadAlly();
            }
        }

        if (isBraveChain(combatActions) && currentEnemies.get(currentEnemyTargetIndex) != null) {
            currentServants.get(combatActions.get(0).servantIndex).activateExtraAttack(this, typeChain, firstCardType);
            removeDeadEnemies();
        }

        for (final Combatant combatant : getAliveEnemies()) {
            combatant.clearCumulativeTurnDamage();
        }
        removeDeadAlly();
        endAllyTurn();
        executeEnemyTurn();
        endEnemyTurn();
        proceedTurn();
    }

    private void endAllyTurn() {
        for (final Servant servant : currentServants) {
            if (servant != null) {
                servant.endOfTurn(this);
            }
        }
        removeDeadAlly();
        replenishDeadCombatantFromBackup(currentServants, backupServants);
    }

    private void executeEnemyTurn() {
        for (final Combatant combatant : currentEnemies) {
            if (combatant != null && combatant.getCurrentHp() <= 0) {
                combatant.hpBarBreak();
            }
        }
    }

    private void endEnemyTurn() {
        for (final Combatant combatant : currentEnemies) {
            if (combatant != null) {
                combatant.endOfTurn(this);
            }
        }
        removeDeadEnemies();
        replenishDeadCombatantFromBackup(currentEnemies, backupEnemies);
    }

    private void proceedTurn() {
        if (isAllEnemiesDead() && level.hasNextStage(currentStage)) {
            currentStage++;
            populateStageWithEnemies(currentStage);
            for (final Combatant combatant : currentEnemies) {
                combatant.initiate(this);
            }
            for (final Combatant combatant : backupEnemies) {
                combatant.initiate(this);
            }

            for (final Combatant combatant : currentEnemies) {
                combatant.enterField(this);
            }

            level.getStage(currentStage).applyStageEffects(this);
        }
        currentTurn++;
    }

    private void populateStageWithEnemies(final int stageIndex) {
        currentEnemies.clear();
        final Stage stage = level.getStage(stageIndex);
        while (stage.hasMoreEnemies() && currentEnemies.size() < stage.getMaximumEnemiesOnScreen()) {
            currentEnemies.add(stage.getNextEnemy());
        }
        while (stage.hasMoreEnemies()) {
            backupEnemies.add(stage.getNextEnemy());
        }
    }

    private boolean isAllEnemiesDead() {
        return currentEnemies.stream().allMatch(Objects::isNull);
    }

    @VisibleForTesting
    static boolean isBraveChain(final List<CombatAction> combatActions) {
        if (combatActions.size() != MAXIMUM_CARDS_PER_TURN) {
            return false;
        }

        final int firstServantIndex = combatActions.get(0).servantIndex;
        for (int i = 1; i < MAXIMUM_CARDS_PER_TURN; i++) {
            if (combatActions.get(i).servantIndex != firstServantIndex) {
                return false;
            }
        }
        return true;
    }

    @VisibleForTesting
    boolean isTypeChain(final List<CombatAction> combatActions) {
        if (combatActions.size() != MAXIMUM_CARDS_PER_TURN) {
            return false;
        }

        final CommandCardType firstCardType = getCommandCardType(combatActions.get(0));
        for (int i = 1; i < MAXIMUM_CARDS_PER_TURN; i++) {
            if (getCommandCardType(combatActions.get(i)) != firstCardType) {
                return false;
            }
        }
        return true;
    }

    private CommandCardType getCommandCardType(final CombatAction combatAction) {
        final Servant currentServant = currentServants.get(combatAction.servantIndex);
        if (combatAction.isNoblePhantasm) {
            return currentServant.getNoblePhantasmType();
        } else {
            return currentServant.getCommandCardType(combatAction.commandCardIndex);
        }
    }

    private void applyTypeChainEffect(final CommandCardType commandCardType) {
        if (commandCardType == ARTS) {
            ARTS_CHAIN_EFFECT.apply(this);
        } else if (commandCardType == QUICK) {
            QUICK_CHAIN_EFFECT.apply(this);
        }
        // well, if another chain effect is available in the future...
    }

    @VisibleForTesting
    static boolean shouldRemoveDeadCombatants(final List<CombatAction> combatActions, final int currentIndex) {
        final CombatAction currentAction = combatActions.get(currentIndex);
        if (currentAction.isNoblePhantasm) {
            return true;
        }

        if (currentIndex < combatActions.size() - 1) {
            final CombatAction nextAction = combatActions.get(currentIndex + 1);
            return nextAction.isNoblePhantasm || nextAction.servantIndex != currentAction.servantIndex;
        } else {
            return !isBraveChain(combatActions);
        }
    }

    private void removeDeadAlly() {
        removeDeadCombatant(currentServants);
        currentAllyTargetIndex = getNextNonNullTargetIndex(currentServants, currentAllyTargetIndex);
    }

    private void removeDeadEnemies() {
        removeDeadCombatant(currentEnemies);
        currentEnemyTargetIndex = getNextNonNullTargetIndex(currentEnemies, currentEnemyTargetIndex);
    }

    private void removeDeadCombatant(final List<? extends Combatant> combatants) {
        // TODO: test this when combatants have guts implemented
        for (int i = 0; i < combatants.size(); i++) {
            final Combatant combatant = combatants.get(i);
            if (combatant == null) {
                continue;
            }

            if (combatant.getCurrentHp() <= 0 && !combatant.hasNextHpBar()) {
                if (!combatant.activateGuts(this)) {
                    combatant.leaveField(this);
                    combatants.set(i, null); // preserve order
                }
            }
        }
    }

    public Combatant getFirstAliveEnemy() {
        final List<Combatant> aliveEnemies = getAliveEnemies();
        if (aliveEnemies.isEmpty()) {
            return null;
        } else {
            return aliveEnemies.get(0);
        }
    }

    public List<Combatant> getAliveEnemies() {
        return currentEnemies.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<Servant> getAliveAllies() {
        return currentServants.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Servant getTargetedAlly() {
        return currentServants.get(currentAllyTargetIndex);
    }

    public Combatant getTargetedEnemy() {
        return currentEnemies.get(currentEnemyTargetIndex);
    }

    @VisibleForTesting
    static int getNextNonNullTargetIndex(final List<? extends Combatant> combatants, int currentTargetIndex) {
        if (combatants.get(currentTargetIndex) == null) {
            for (int i = 0; i < combatants.size(); i++) {
                if (combatants.get(i) != null) {
                    return i;
                }
            }
            // i == combatants.size(), reset target index
            return 0;
        }
        return currentTargetIndex;
    }

    private <E extends Combatant> void replenishDeadCombatantFromBackup(
            final List<E> currentCombatants,
            final Queue<E> backupCombatants
    ) {
        final List<Combatant> newCombatants = new ArrayList<>();
        for (int i = 0; i < currentCombatants.size(); i++) {
            final Combatant combatant = currentCombatants.get(i);
            if (combatant == null && !backupCombatants.isEmpty()) {
                final E backupServant = backupCombatants.poll();
                currentCombatants.set(i, backupServant);
                newCombatants.add(backupServant);
            }
        }
        for (final Combatant combatant : newCombatants) {
            combatant.enterField(this);
        }
    }

    public void gainStar(final double starsToGain) {
        currentStars += starsToGain;
        if (currentStars < 0) {
            currentStars = 0;
        }
    }
}
