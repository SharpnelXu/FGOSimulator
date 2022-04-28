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
import yome.fgo.simulator.models.effects.CriticalStarChange;
import yome.fgo.simulator.models.effects.Effect;
import yome.fgo.simulator.models.effects.NpChange;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.levels.Level;
import yome.fgo.simulator.models.levels.Stage;
import yome.fgo.simulator.models.mysticcodes.MysticCode;
import yome.fgo.simulator.utils.RoundUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
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
            .npChanges(ImmutableList.of(0.2))
            .build();
    public static final Effect QUICK_CHAIN_EFFECT = CriticalStarChange.builder()
            .values(ImmutableList.of(10))
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
    // used when dealing damages
    private Combatant attacker;
    private Combatant defender;
    private CommandCard currentCommandCard;

    // used when activating effects
    private Stack<Combatant> activator = new Stack<>();
    private Stack<Combatant> effectTarget = new Stack<>();

    public void setActivator(final Combatant combatant) {
        activator.push(combatant);
    }

    public Combatant getActivator() {
        return activator.peek();
    }

    public void unsetActivator() {
        activator.pop();
    }
    public void setEffectTarget(final Combatant combatant) {
        effectTarget.push(combatant);
    }

    public Combatant getEffectTarget() {
        return effectTarget.peek();
    }

    public void unsetEffectTarget() {
        effectTarget.pop();
    }

    private Buff currentBuff; // used by grantBuff
    private boolean activatingServantPassiveEffects; // used to denote passive & append skill buffs
    private boolean activatingCePassiveEffects; // used to denote ce skills

    private Combatant nullSourceSkillActivator = new Combatant();

    public void initiate() {
        currentStage = 1;
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
        setActivator(nullSourceSkillActivator);
        level.applyLevelEffects(this);
        level.getStage(currentStage).applyStageEffects(this);
        unsetActivator();
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
        setActivator(nullSourceSkillActivator);
        mysticCode.activateSkill(this, skillIndex);
        unsetActivator();
    }

    public void executeCombatActions(final List<CombatAction> combatActions) {
        // TODO: move to front end
        assert !combatActions.isEmpty() && combatActions.size() <= MAXIMUM_CARDS_PER_TURN;
        this.currentStars = 0;

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

    public void checkBuffStatus() {
        for (final Servant servant : currentServants) {
            if (servant != null) {
                servant.checkBuffStatus();
            }
        }
        for (final Combatant combatant : currentEnemies) {
            if (combatant != null) {
                combatant.checkBuffStatus();
            }
        }
    }

    private void endAllyTurn() {
        for (final Servant servant : currentServants) {
            if (servant != null) {
                servant.endOfMyTurn(this);
            }
        }
        for (final Combatant combatant : currentEnemies) {
            if (combatant != null) {
                combatant.endOfYourTurn(this);
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
                combatant.endOfMyTurn(this);
            }
        }

        for (final Servant servant : currentServants) {
            if (servant != null) {
                servant.endOfYourTurn(this);
            }
        }
        checkBuffStatus();

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

    private void populateStageWithEnemies(final int currentStage) {
        currentEnemies.clear();
        final Stage stage = level.getStage(currentStage);
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
            return currentServant.getNoblePhantasmCardType(this);
        } else {
            return currentServant.getCommandCardType(this, combatAction.commandCardIndex);
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

    public Servant getTargetedBackup() {
        return backupServants.get(currentBackupTargetIndex);
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
        currentStars = RoundUtils.roundNearest(currentStars + starsToGain);
        if (currentStars < 0) {
            currentStars = 0;
        }
    }

    public CommandCardType selectCommandCardType(final Set<CommandCardType> selections) {
        // TODO: UI
        return selections.stream().findFirst().get();
    }
}
