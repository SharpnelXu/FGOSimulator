package yome.fgo.simulator.models;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.SpecialActivationParams;
import yome.fgo.simulator.gui.components.SimulationWindow;
import yome.fgo.simulator.gui.components.StatsLogger;
import yome.fgo.simulator.models.combatants.CombatAction;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.CommandCard;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.effects.CriticalStarChange;
import yome.fgo.simulator.models.effects.Effect;
import yome.fgo.simulator.models.effects.EffectFactory;
import yome.fgo.simulator.models.effects.NpChange;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.levels.Level;
import yome.fgo.simulator.models.levels.Stage;
import yome.fgo.simulator.models.mysticcodes.MysticCode;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.UNRECOGNIZED;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_ALLIES;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_CHARACTERS;
import static yome.fgo.simulator.models.effects.buffs.BuffType.GRANT_STAGE_TRAIT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.REMOVE_STAGE_TRAIT;

@NoArgsConstructor
@Getter
@Setter
public class Simulation {
    public static final int MAXIMUM_SERVANTS_ON_SCREEN = 3;
    public static final int MAXIMUM_CARDS_PER_TURN = 3;
    public static final Effect ARTS_CHAIN_EFFECT = NpChange.builder()
            .target(ALL_ALLIES)
            .values(ImmutableList.of(0.2))
            .build();
    public static final Effect QUICK_CHAIN_EFFECT = CriticalStarChange.builder()
            .values(ImmutableList.of(20))
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
    public Simulation(
            final Level level,
            final List<Servant> servants,
            final MysticCode mysticCode,
            final SimulationWindow simulationWindow
    ) {
        this(level, servants, mysticCode);
        this.simulationWindow = simulationWindow;
    }

    /*
     * ================================================================================
     * GUI Fields
     * ================================================================================
     */
    public SimulationWindow simulationWindow;
    private StatsLogger statsLogger;

    /*
     * ================================================================================
     * Basic Fields
     * ================================================================================
     */
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
    public int currentEnemyTargetIndex;

    public double fixedRandom;
    public double probabilityThreshold;
    private Combatant mysticCodeActivator = new Servant("master"); // on ally side

    /*
     * ================================================================================
     * Execution Fields
     * ================================================================================
     */
    // For activating chain effect & custom effects & level & stage effects
    private Combatant nullSourceSkillActivator = new Servant("nullSource"); // on ally side
    private boolean simulateEnemyAction;
    private List<Integer> orderChangeSelections;
    private CommandCardType selectedCommandCardType;
    private EffectData selectedEffectData;
    private Stack<Combatant> attacker = new Stack<>();
    private Stack<Combatant> defender = new Stack<>();
    private Stack<CommandCard> currentCommandCard = new Stack<>();
    private boolean criticalStrike;
    private Stack<Combatant> activator = new Stack<>();
    private Stack<Combatant> effectTarget = new Stack<>();
    private Stack<Buff> currentBuff = new Stack<>();

    private boolean activatingServantPassiveEffects;
    private boolean activatingCePassiveEffects;

    /*
     * ================================================================================
     * Execution Methods - used only for managing simluation states
     * ================================================================================
     */
    public void requestSpecialActivationTarget(final SpecialActivationParams specialActivationParams) {
        simulationWindow.showSpecialTargetSelectionWindow(specialActivationParams);
    }

    public CommandCardType selectCommandCardType() {
        return selectedCommandCardType;
    }

    public EffectData selectRandomEffects() {
        return selectedEffectData;
    }

    public List<Integer> getOrderChangeTargets() {
        return orderChangeSelections;
    }

    public void setAttacker(final Combatant combatant) {
        attacker.push(combatant);
    }

    public Combatant getAttacker() {
        return attacker.peek();
    }

    public void unsetAttacker() {
        attacker.pop();
    }

    public void setDefender(final Combatant combatant) {
        defender.push(combatant);
    }

    public Combatant getDefender() {
        return defender.peek();
    }

    public void unsetDefender() {
        defender.pop();
    }

    public void setCurrentCommandCard(final CommandCard commandCard) {
        currentCommandCard.push(commandCard);
    }

    public CommandCard getCurrentCommandCard() {
        return currentCommandCard.peek();
    }

    public void unsetCurrentCommandCard() {
        currentCommandCard.pop();
    }


    public boolean hasActivator() {
        return !activator.isEmpty();
    }

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

    public void setCurrentBuff(final Buff buff) {
        currentBuff.push(buff);
    }

    public Buff getCurrentBuff() {
        return currentBuff.peek();
    }

    public void unsetCurrentBuff() {
        currentBuff.pop();
    }

    /*
     * ================================================================================
     * Initiation method
     * ================================================================================
     */
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

        // level effect & stage effect
        setActivator(nullSourceSkillActivator);
        level.applyLevelEffects(this);
        level.getStage(currentStage).applyStageEffects(this);
        unsetActivator();

        for (final Servant servant : currentServants) {
            servant.enterField(this);
        }

        for (final Combatant combatant : currentEnemies) {
            combatant.enterField(this);
        }


        if (getStatsLogger() != null) {
            getStatsLogger().logTurnStart(currentTurn);
        }
    }

    /*
     * ================================================================================
     * Basic Methods
     * ================================================================================
     */
    public boolean isSimulationCompleted() {
        return currentServants.stream().allMatch(Objects::isNull) || isAllEnemiesDead();
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

    public List<Combatant> getOtherTeam(final Combatant combatant) {
        if (!combatant.isAlly()) {
            return currentServants.stream().map(servant -> (Combatant) servant).collect(Collectors.toList());
        } else {
            return currentEnemies;
        }
    }

    public Servant getTargetedAlly() {
        if (currentServants.size() <= currentAllyTargetIndex) {
            return null;
        }

        return currentServants.get(currentAllyTargetIndex);
    }

    public Combatant getTargetedEnemy() {
        if (currentEnemies.size() <= currentEnemyTargetIndex) {
            return null;
        }

        return currentEnemies.get(currentEnemyTargetIndex);
    }

    public Set<String> getStageTraits() {
        final Set<String> fieldTraits = new TreeSet<>(getLevel().getStage(getCurrentStage()).getTraits());
        final Set<String> removeTraits = new HashSet<>();
        for (final Combatant combatant : TargetUtils.getTargets(this, ALL_CHARACTERS)) {
            for (final Buff buff : combatant.getBuffs()) {
                if (buff.getBuffType() == GRANT_STAGE_TRAIT && buff.shouldApply(this)) {
                    fieldTraits.add(buff.getStringValue());
                }

                if (buff.getBuffType() == REMOVE_STAGE_TRAIT && buff.shouldApply(this)) {
                    removeTraits.add(buff.getStringValue());
                }
            }
        }
        fieldTraits.removeAll(removeTraits);
        return fieldTraits;
    }

    /*
     * ================================================================================
     * Activation (player action) methods
     * ================================================================================
     */
    public void gainStar(final double starsToGain) {
        currentStars = RoundUtils.roundNearest(currentStars + starsToGain);
        if (currentStars < 0) {
            currentStars = 0;
        }
    }

    public void activateServantSkill(final int servantIndex, final int activeSkillIndex) {
        if (isSimulationCompleted()) {
            return;
        }

        takeSnapshot();

        currentServants.get(servantIndex).activateActiveSkill(this, activeSkillIndex);
    }

    public boolean canActivateMysticCodeSkill(final int i) {
        if (isSimulationCompleted()) {
            return false;
        }

        setActivator(mysticCodeActivator);

        final boolean result = mysticCode.canActivateSkill(this, i);

        unsetActivator();

        return result;
    }

    public void activateMysticCodeSkill(final int skillIndex) {
        if (isSimulationCompleted()) {
            return;
        }

        takeSnapshot();

        setActivator(mysticCodeActivator);
        if (statsLogger != null) {
            statsLogger.logActivateActiveSkill(mysticCodeActivator.getId(), skillIndex);
        }
        mysticCode.activateSkill(this, skillIndex);
        unsetActivator();
    }

    public void activateCustomEffect(final EffectData effectData) {
        if (isSimulationCompleted()) {
            return;
        }

        try {
            takeSnapshot();

            setActivator(nullSourceSkillActivator);

            EffectFactory.buildEffect(effectData, 1).apply(this, 1);

            checkBuffStatus();

            unsetActivator();
        } catch (final Exception e) {
            if (getStatsLogger() != null) {
                getStatsLogger().logException("Error activating custom effect", e);
            }
        }
    }

    public void executeCombatActions(final List<CombatAction> combatActions) {
        if (isSimulationCompleted()) {
            return;
        }

        takeSnapshot();

        currentStars = 0;
        // calculate chain
        final boolean typeChain = isTypeChain(combatActions);
        final boolean triColorChain = isTriColorChain(combatActions);
        final CommandCardType firstCardType = getFirstCardType(combatActions);
        if (typeChain && firstCardType != BUSTER && firstCardType != UNRECOGNIZED) {
            applyTypeChainEffect(firstCardType);
        }

        // apply command card effects
        int extraOvercharge = 0;
        for (int i = 0; i < combatActions.size(); i += 1) {
            final CombatAction combatAction = combatActions.get(i);
            final Servant servant = currentServants.get(combatAction.servantIndex);

            if (canAttack(servant) && !isAllEnemiesDead()) {
                if (combatAction.isNoblePhantasm) {
                    if (servant.isNpInaccessible()) {
                        continue;
                    }
                    servant.activateNoblePhantasm(this, extraOvercharge);
                    extraOvercharge += 1;
                } else {
                    setDefender(getTargetedEnemy());
                    servant.activateCommandCard(
                            this,
                            combatAction.commandCardIndex,
                            i,
                            combatAction.isCriticalStrike,
                            firstCardType,
                            typeChain,
                            triColorChain
                    );
                    extraOvercharge = 0;
                    unsetDefender();
                }
            }

            if (shouldRemoveDeadCombatants(combatActions, i)) {
                removeDeadEnemies();
                removeDeadAlly();
            }
        }

        if (isBraveChain(combatActions) && currentEnemies.get(currentEnemyTargetIndex) != null) {
            final Servant servant = currentServants.get(combatActions.get(0).servantIndex);
            if (canAttack(servant)) {
                setDefender(getTargetedEnemy());
                servant.activateExtraAttack(
                        this,
                        firstCardType,
                        typeChain,
                        triColorChain
                );
                unsetDefender();
            }
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

    /*
     * ================================================================================
     * Utility Methods
     * ================================================================================
     */
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

    public static boolean canAttack(final Servant servant) {
        return servant != null && !servant.isAlreadyDead() && !servant.isImmobilized();
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
        for (int i = 1; i < MAXIMUM_CARDS_PER_TURN; i += 1) {
            if (combatActions.get(i).servantIndex != firstServantIndex) {
                return false;
            }
        }
        return true;
    }

    @VisibleForTesting
    boolean isTriColorChain(final List<CombatAction> combatActions) {
        if (combatActions.size() != MAXIMUM_CARDS_PER_TURN) {
            return false;
        }
        final Set<CommandCardType> triColorSet = new HashSet<>();
        triColorSet.add(BUSTER);
        triColorSet.add(ARTS);
        triColorSet.add(QUICK);

        for (final CombatAction combatAction : combatActions) {
            if (!currentServants.get(combatAction.servantIndex).isImmobilized()) {
                triColorSet.remove(getCommandCardType(combatAction));
            }
        }

        return triColorSet.isEmpty();
    }

    @VisibleForTesting
    boolean isTypeChain(final List<CombatAction> combatActions) {
        if (combatActions.size() != MAXIMUM_CARDS_PER_TURN) {
            return false;
        }

        final CommandCardType firstCardType = getFirstCardType(combatActions);
        if (firstCardType == UNRECOGNIZED) {
            return false;
        }

        for (final CombatAction combatAction : combatActions) {
            if (firstCardType != getCommandCardType(combatAction) ||
                    currentServants.get(combatAction.servantIndex).isImmobilized()) {
                return false;
            }
        }
        return true;
    }

    @VisibleForTesting
    CommandCardType getFirstCardType(final List<CombatAction> combatActions) {
        if (currentServants.get(combatActions.get(0).servantIndex).isImmobilized()) {
            return UNRECOGNIZED;
        } else {
            return getCommandCardType(combatActions.get(0));
        }
    }

    private CommandCardType getCommandCardType(final CombatAction combatAction) {
        final Servant currentServant = currentServants.get(combatAction.servantIndex);
        if (combatAction.isNoblePhantasm) {
            return currentServant.getNoblePhantasmCardType();
        } else {
            return currentServant.getCommandCardType(combatAction.commandCardIndex);
        }
    }

    private void applyTypeChainEffect(final CommandCardType commandCardType) {
        setActivator(nullSourceSkillActivator);
        if (commandCardType == ARTS) {
            ARTS_CHAIN_EFFECT.apply(this);
        } else if (commandCardType == QUICK) {
            QUICK_CHAIN_EFFECT.apply(this);
        }
        unsetActivator();
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
        for (int i = 0; i < combatants.size(); i += 1) {
            final Combatant combatant = combatants.get(i);
            if (combatant == null) {
                continue;
            }

            if (combatant.getCurrentHp() <= 0 && !combatant.hasNextHpBar()) {
                if (!combatant.activateGuts(this)) {
                    combatant.death(this);
                    combatants.set(i, null); // preserve order
                }
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

        mysticCode.decreaseCoolDown();

        removeDeadAlly();
        removeDeadEnemies();
    }

    private void executeEnemyTurn() {
        for (final Combatant combatant : currentEnemies) {
            if (combatant != null) {
                if (combatant.getCurrentHp() <= 0) {
                    combatant.hpBarBreak(this);
                }
                combatant.startOfMyTurn(this);
            }
        }

        if (simulateEnemyAction) {
            for (final Combatant combatant : currentEnemies) {

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

        removeDeadAlly();
        removeDeadEnemies();
    }

    private void proceedTurn() {
        replenishDeadCombatantFromBackup(currentServants, backupServants);
        replenishDeadCombatantFromBackup(currentEnemies, backupEnemies);

        if (isAllEnemiesDead() && level.hasNextStage(currentStage)) {
            currentStage += 1;
            populateStageWithEnemies(currentStage);
            for (final Combatant combatant : currentEnemies) {
                combatant.initiate(this);
            }
            for (final Combatant combatant : backupEnemies) {
                combatant.initiate(this);
            }

            setActivator(nullSourceSkillActivator);
            level.getStage(currentStage).applyStageEffects(this);
            unsetActivator();

            for (final Combatant combatant : currentEnemies) {
                combatant.enterField(this);
            }
        }
        currentTurn += 1;
        if (getStatsLogger() != null) {
            getStatsLogger().logTurnStart(currentTurn);
        }
        for (final Servant servant : currentServants) {
            if (servant != null) {
                servant.startOfMyTurn(this);
            }
        }
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

    @VisibleForTesting
    static int getNextNonNullTargetIndex(final List<? extends Combatant> combatants, int currentTargetIndex) {
        if ( currentTargetIndex >= combatants.size() || combatants.get(currentTargetIndex) == null) {
            for (int i = 0; i < combatants.size(); i += 1) {
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
        for (int i = 0; i < currentCombatants.size(); i += 1) {
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

    /*
     * ================================================================================
     * Make Copy
     * ================================================================================
     */
    private Stack<Snapshot> snapshots = new Stack<>();

    public void takeSnapshot() {
        snapshots.push(new Snapshot(this));
    }

    /**
     * @return whether there's more snapshots
     */
    public boolean fromSnapshot() {
        if (snapshots.isEmpty()) {
            return false;
        }

        if (getStatsLogger() != null) {
            getStatsLogger().logRevertAction();
        }

        final Snapshot snapshot = snapshots.pop();
        this.level = snapshot.getLevel();

        this.currentStage = snapshot.getCurrentStage();
        this.currentTurn = snapshot.getCurrentTurn();

        this.currentEnemies = snapshot.getCurrentEnemies();
        this.backupEnemies = snapshot.getBackupEnemies();

        this.currentServants = snapshot.getCurrentServants();
        this.backupServants = snapshot.getBackupServants();

        this.mysticCode = snapshot.getMysticCode();
        this.mysticCodeActivator = snapshot.getMaster();

        this.currentStars = snapshot.getCurrentStars();
        this.currentAllyTargetIndex = snapshot.getCurrentAllyTargetIndex();
        this.currentEnemyTargetIndex = snapshot.getCurrentEnemyTargetIndex();

        this.fixedRandom = snapshot.getFixedRandom();
        this.probabilityThreshold = snapshot.getProbabilityThreshold();

        return !snapshots.isEmpty();
    }
}
