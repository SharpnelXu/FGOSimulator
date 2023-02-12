package yome.fgo.simulator.models.combatants;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import yome.fgo.data.proto.FgoStorageData.Alignment;
import yome.fgo.data.proto.FgoStorageData.Attribute;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.data.proto.FgoStorageData.PassiveSkillData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.CommandCardExecution;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffType;
import yome.fgo.simulator.utils.BuffUtils;
import yome.fgo.simulator.utils.RoundUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static yome.fgo.simulator.models.combatants.CommandCard.ENEMY_DEFAULT_ARTS;
import static yome.fgo.simulator.models.combatants.CommandCard.ENEMY_DEFAULT_BUSTER;
import static yome.fgo.simulator.models.combatants.CommandCard.ENEMY_DEFAULT_EXTRA;
import static yome.fgo.simulator.models.combatants.CommandCard.ENEMY_DEFAULT_QUICK;
import static yome.fgo.simulator.models.combatants.NoblePhantasm.ENEMY_DEFAULT_NOBLE_PHANTASM;
import static yome.fgo.simulator.models.effects.buffs.BuffType.ATTACK_BUFF_DURATION_EXTEND;
import static yome.fgo.simulator.models.effects.buffs.BuffType.BURN;
import static yome.fgo.simulator.models.effects.buffs.BuffType.BURN_EFFECTIVENESS_UP;
import static yome.fgo.simulator.models.effects.buffs.BuffType.CARD_TYPE_CHANGE;
import static yome.fgo.simulator.models.effects.buffs.BuffType.CURSE;
import static yome.fgo.simulator.models.effects.buffs.BuffType.CURSE_EFFECTIVENESS_UP;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DAMAGE_REFLECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DEATH_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DELAYED_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.END_OF_TURN_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.ENTER_FIELD_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.GRANT_TRAIT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.GUTS;
import static yome.fgo.simulator.models.effects.buffs.BuffType.HP_BREAK_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.LEAVE_FIELD_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.MAX_HP_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.NP_CARD_TYPE_CHANGE;
import static yome.fgo.simulator.models.effects.buffs.BuffType.NP_SEAL;
import static yome.fgo.simulator.models.effects.buffs.BuffType.ON_FIELD_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.OVERCHARGE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.PERMANENT_SLEEP;
import static yome.fgo.simulator.models.effects.buffs.BuffType.POISON;
import static yome.fgo.simulator.models.effects.buffs.BuffType.POISON_EFFECTIVENESS_UP;
import static yome.fgo.simulator.models.effects.buffs.BuffType.PREVENT_DEATH_AGAINST_DOT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.REMOVE_TRAIT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SKILL_SEAL;
import static yome.fgo.simulator.models.effects.buffs.BuffType.START_OF_TURN_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.TRIGGER_ON_GUTS_EFFECT;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.BuffUtils.isImmobilizeOrSeal;
import static yome.fgo.simulator.utils.BuffUtils.shouldDecreaseNumTurnsActiveAtMyTurn;
import static yome.fgo.simulator.utils.FateClassUtils.getClassMaxNpGauge;

@Getter
@Setter
public class Combatant {
    /*
     * ================================================================================
     * Enemy Fields
     * ================================================================================
     */
    private EnemyData enemyData;
    private int maxNpGauge;
    private int currentNpGauge;
    private List<PassiveSkill> enemyPassiveSkills = Lists.newArrayList();
    /*
     * ================================================================================
     * Basic Fields
     * ================================================================================
     */
    protected CombatantData combatantData;
    protected String id;
    protected List<Integer> hpBars;
    protected boolean isAlly;
    protected int attack;

    protected List<CommandCard> commandCards = new ArrayList<>();
    protected CommandCard extraCommandCard;

    protected NoblePhantasm noblePhantasm;

    /*
     * ================================================================================
     * Execution Fields
     * ================================================================================
     */
    protected int currentHp;
    private int currentHpBarIndex;
    protected List<Buff> buffs = Lists.newArrayList();
    private int cumulativeTurnDamage;
    private boolean receivedInstantDeath;
    private int activatorHash;

    /*
     * ================================================================================
     * Enemy constructor
     * ================================================================================
     */
    public Combatant(final CombatantData combatantData, final EnemyData enemyData) {
        this(enemyData.getEnemyBaseId(), enemyData.getHpBarsList());
        this.attack = 1000;
        this.noblePhantasm = ENEMY_DEFAULT_NOBLE_PHANTASM;
        this.commandCards.addAll(List.of(ENEMY_DEFAULT_QUICK, ENEMY_DEFAULT_ARTS, ENEMY_DEFAULT_BUSTER));
        this.extraCommandCard = ENEMY_DEFAULT_EXTRA;

        if (enemyData.hasCombatantDataOverride()) {
            this.combatantData = enemyData.getCombatantDataOverride();
        } else {
            this.combatantData = combatantData;
        }
        if (enemyData.getHasCustomMaxNpGauge()) {
            this.maxNpGauge = enemyData.getCustomMaxNpGauge();
        } else {
            this.maxNpGauge = getClassMaxNpGauge(this.combatantData.getFateClass());
        }
        this.enemyData = enemyData;
        this.enemyPassiveSkills = new ArrayList<>();
        for (final PassiveSkillData passiveSkillData : this.combatantData.getEnemyPassiveSkillDataList()) {
            this.enemyPassiveSkills.add(new PassiveSkill(passiveSkillData));
        }
    }

    /*
     * ================================================================================
     * Initiation methods - called by simulation only
     * ================================================================================
     */
    public void initiate(final Simulation simulation) {
        simulation.setActivator(this);
        simulation.setActivatingServantPassiveEffects(true);
        for (final PassiveSkill passiveSkill : enemyPassiveSkills) {
            passiveSkill.activate(simulation);
        }
        simulation.setActivatingServantPassiveEffects(false);
        simulation.unsetActivator();
        this.activatorHash = hashCode();
    }

    /*
     * ================================================================================
     * Basic access methods
     * ================================================================================
     */
    public int getAttack() {
        return attack;
    }

    public FateClass getFateClass() {
        return combatantData.getFateClass();
    }

    public Attribute getAttribute() {
        return combatantData.getAttribute();
    }

    public Gender getGender() {
        return combatantData.getGender();
    }

    public double getDeathRate() {
        return combatantData.getDeathRate();
    }

    public int getRarity() {
        return combatantData.getRarity();
    }

    public CommandCardType getNoblePhantasmCardType() {
        final Buff cardTypeChange = fetchFirst(NP_CARD_TYPE_CHANGE);
        if (cardTypeChange != null) {
            return cardTypeChange.getCommandCardType();
        } else {
            return noblePhantasm.getCommandCardType();
        }
    }

    public NoblePhantasm getNoblePhantasm() {
        final Buff cardTypeChange = fetchFirst(NP_CARD_TYPE_CHANGE);
        if (cardTypeChange != null) {
            return new NoblePhantasm(
                    cardTypeChange.getCommandCardType(),
                    noblePhantasm.getHitPercentages(),
                    noblePhantasm.getNpCharge(),
                    noblePhantasm.getCriticalStarGeneration(),
                    noblePhantasm.getEffects(),
                    noblePhantasm.getNoblePhantasmType(),
                    noblePhantasm.getActivationCondition()
            );
        } else {
            return noblePhantasm;
        }
    }

    public CommandCardType getOriginalNoblePhantasmCardType() {
        return noblePhantasm.getCommandCardType();
    }

    public CommandCardType getCommandCardType(final int commandCardIndex) {
        final Buff cardTypeChange = fetchFirst(CARD_TYPE_CHANGE);
        if (cardTypeChange != null) {
            return cardTypeChange.getCommandCardType();
        } else {
            return commandCards.get(commandCardIndex).getCommandCardType();
        }
    }

    public CommandCard getCommandCard(final int index) {
        final Buff cardTypeChange = fetchFirst(CARD_TYPE_CHANGE);
        if (cardTypeChange != null) {
            final CommandCardType cardTypeOfChangedType = cardTypeChange.getCommandCardType();

            CommandCardData cardDataOfChangedType = null;
            for (final CommandCard commandCard : commandCards) {
                if (commandCard.getCommandCardType() == cardTypeOfChangedType) {
                    cardDataOfChangedType = commandCard.getCommandCardData();
                    break;
                }
            }

            final CommandCard supposedCard = commandCards.get(index);
            return new CommandCard(
                    cardDataOfChangedType,
                    supposedCard.getCommandCodeData(),
                    supposedCard.getCommandCodeBuffs(),
                    supposedCard.getCommandCardStrengthen()
            );
        } else {
            return commandCards.get(index);
        }
    }

    public List<String> getAllTraits(final Simulation simulation) {
        final List<String> allTraits = new ArrayList<>();
        allTraits.addAll(combatantData.getTraitsList());
        allTraits.add(combatantData.getGender().name());
        allTraits.add(combatantData.getAttribute().name());
        allTraits.addAll(combatantData.getAlignmentsList().stream().map(Alignment::name).collect(Collectors.toList()));

        final Set<String> traitsToRemove = new HashSet<>();
        for (final Buff buff : buffs) {
            if (buff.getBuffType() == GRANT_TRAIT && buff.shouldApply(simulation)) {
                allTraits.add(buff.getStringValue());
                buff.setApplied();
            } else if (buff.getBuffType() == REMOVE_TRAIT && buff.shouldApply(simulation)) {
                traitsToRemove.add(buff.getStringValue());
                buff.setApplied();
            }
        }

        allTraits.removeAll(traitsToRemove);
        return allTraits;
    }

    public boolean getUndeadNpCorrection() {
        return combatantData.getUndeadNpCorrection();
    }

    public int getMaxHp() {
        double additionalHp = 0;

        for (final Buff buff : fetchBuffs(MAX_HP_BUFF)) {
            additionalHp += buff.getValue();
        }

        return Math.max(hpBars.get(currentHpBarIndex) + (int) RoundUtils.roundNearest(additionalHp), 1);
    }

    public boolean hasNextHpBar() {
        return currentHpBarIndex < hpBars.size() - 1;
    }

    public int getActivatorHash() {
        return activatorHash;
    }

    /*
     * ================================================================================
     * Methods for basic effects
     * ================================================================================
     */
    public boolean npCheck() {
        return currentNpGauge == maxNpGauge;
    }

    public boolean canActivateNoblePhantasm(final Simulation simulation) {
        simulation.setActivator(this);

        final boolean canActivate = npCheck() && !isNpInaccessible() && noblePhantasm.canActivate(simulation);

        simulation.unsetActivator();

        return canActivate;
    }

    public void resetNp() {
        currentNpGauge = 0;
    }

    public void activateNoblePhantasm(final Simulation simulation, final int extraOvercharge) {
        final boolean isCrit = simulation.isCriticalStrike();
        simulation.setActivator(this);
        simulation.setCriticalStrike(false);

        final int overchargeLevel = calculateOverchargeLevel(simulation, extraOvercharge);
        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logNoblePhantasm(getId(), overchargeLevel);
        }
        resetNp();
        noblePhantasm.activate(simulation, overchargeLevel);

        simulation.setCriticalStrike(isCrit);
        simulation.unsetActivator();
    }

    public void activateCommandCard(
            final Simulation simulation,
            final int commandCardIndex,
            final int chainIndex,
            final boolean isCriticalStrike,
            final CommandCardType firstCardType,
            final boolean isTypeChain,
            final boolean isTriColorChain
    ) {
        final boolean isCrit = simulation.isCriticalStrike();
        simulation.setActivator(this);
        simulation.setAttacker(this);
        simulation.setDefender(simulation.getTargetedEnemy());
        simulation.setCurrentCommandCard(getCommandCard(commandCardIndex));
        simulation.setCriticalStrike(isCriticalStrike);

        CommandCardExecution.executeCommandCard(simulation, chainIndex, isCriticalStrike, firstCardType, isTypeChain, isTriColorChain);

        simulation.setCriticalStrike(isCrit);
        simulation.unsetCurrentCommandCard();
        simulation.unsetDefender();
        simulation.unsetAttacker();
        simulation.unsetActivator();
    }

    public void activateExtraAttack(
            final Simulation simulation,
            final CommandCardType firstCardType,
            final boolean isTypeChain,
            final boolean isTriColorChain
    ) {
        final boolean isCrit = simulation.isCriticalStrike();
        simulation.setActivator(this);
        simulation.setAttacker(this);
        simulation.setDefender(simulation.getTargetedEnemy());
        simulation.setCurrentCommandCard(extraCommandCard);
        simulation.setCriticalStrike(false);

        CommandCardExecution.executeCommandCard(simulation, 3, false, firstCardType, isTypeChain, isTriColorChain);

        simulation.setCriticalStrike(isCrit);
        simulation.unsetCurrentCommandCard();
        simulation.unsetDefender();
        simulation.unsetAttacker();
        simulation.unsetActivator();
    }

    public void decreaseActiveSkillsCoolDown(final int change) {}

    public void changeNp(final double percentNpChange) {}

    public void changeNpGauge(final int gaugeChange) {
        currentNpGauge += gaugeChange;
        if (currentNpGauge > maxNpGauge) {
            currentNpGauge = maxNpGauge;
        }
        if (currentNpGauge < 0) {
            currentNpGauge = 0;
        }
    }

    public void changeHp(final int hpChange, final boolean isLethal) {
        currentHp += hpChange;

        final int maxHp = getMaxHp();
        if (currentHp > maxHp) {
            currentHp = maxHp;
        }
        // non-lethal damage
        if (currentHp <= 0 && hpChange < 0 && !isLethal) {
            currentHp = 1;
        }
    }

    public void changeHpAfterMaxHpChange(final int change) {
        if (change > 0) {
            currentHp += change;
        } else {
            final int maxHp = getMaxHp();
            if (currentHp > maxHp) {
                currentHp = maxHp;
            }
        }
    }

    public void instantDeath() {
        receivedInstantDeath = true;
        currentHp = 0;
    }

    public boolean isAlreadyDead() {
        return currentHp <= 0;
    }

    public boolean isAlive(final Simulation simulation) {
        if (currentHp > 0 || hasNextHpBar()) {
            return true;
        }
        return getGutsToActivate(simulation) != null;
    }

    public boolean isBuggedOverkill() {
        return getCumulativeTurnDamage() > getCurrentHp();
    }

    public void addCumulativeTurnDamage(final int damage) {
        cumulativeTurnDamage += damage;
    }

    public void clearCumulativeTurnDamage() {
        cumulativeTurnDamage = 0;
    }

    public void receiveDamage(final int damage) {
        for (final Buff buff : fetchBuffs(DAMAGE_REFLECT)) {
            buff.storeDamage(damage);
        }

        currentHp -= damage;
    }

    public void receiveNonHpBarBreakDamage(final int damage) {
        currentHp -= damage;
        if (currentHp <= 0 && hasNextHpBar()) {
            currentHp = 1;
        }
    }

    public void hpBarBreak(final Simulation simulation) {
        receivedInstantDeath = false;
        currentHpBarIndex += 1;
        currentHp = hpBars.get(currentHpBarIndex);
        activateEffectActivatingBuff(simulation, HP_BREAK_EFFECT);
    }

    public void enterField(final Simulation simulation) {
        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logEnterField(id);
        }

        activateEffectActivatingBuff(simulation, ENTER_FIELD_EFFECT);
        activateOnFieldBuff(simulation, true);
    }

    public void leaveField(final Simulation simulation) {
        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logLeaveField(id);
        }
        activateEffectActivatingBuff(simulation, LEAVE_FIELD_EFFECT);
        activateOnFieldBuff(simulation, false);
    }

    public void death(final Simulation simulation) {
        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logDeath(id);
        }
        activateEffectActivatingBuff(simulation, DEATH_EFFECT);
        activateOnFieldBuff(simulation, false);
    }

    public void endOfYourTurn(final Simulation simulation) {
        activateEffectActivatingBuff(simulation, DELAYED_EFFECT);

        activateDamageReflect(simulation);

        for (final Buff buff : buffs) {
            if (!shouldDecreaseNumTurnsActiveAtMyTurn(buff, isBuffExtended()) && !isImmobilizeOrSeal(buff)) {
                buff.decrementActiveTurns();
            }
        }

        clearInactiveBuff();
    }

    public void startOfMyTurn(final Simulation simulation) {
        activateEffectActivatingBuff(simulation, START_OF_TURN_EFFECT);
    }

    public void endOfMyTurn(final Simulation simulation) {
        final boolean npSealed = isNpSealed();
        final boolean immobilized = isImmobilized();
        if (currentNpGauge == maxNpGauge && !npSealed && !immobilized) {
            currentNpGauge = 0;
        } else if (!npSealed) {
            currentNpGauge += 1;
            if (currentNpGauge > maxNpGauge) {
                currentNpGauge = maxNpGauge;
            }
        }

        for (final Buff buff : buffs) {
            if (isImmobilizeOrSeal(buff)) {
                buff.decrementActiveTurns();
            }
        }
        clearInactiveBuff();

        final int poisonDamage = calculateDoTDamage(simulation, POISON, POISON_EFFECTIVENESS_UP);
        final int burnDamage = calculateDoTDamage(simulation, BURN, BURN_EFFECTIVENESS_UP);
        final int curseDamage = calculateDoTDamage(simulation, CURSE, CURSE_EFFECTIVENESS_UP);

        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logDoT(id, poisonDamage, burnDamage, curseDamage);
        }

        receiveNonHpBarBreakDamage(poisonDamage + burnDamage + curseDamage);

        activateEffectActivatingBuff(simulation, END_OF_TURN_EFFECT);

        for (final Buff buff : buffs) {
            if (shouldDecreaseNumTurnsActiveAtMyTurn(buff, isBuffExtended())) {
                buff.decrementActiveTurns();
            }
        }

        clearInactiveBuff();
    }

    /*
     * ================================================================================
     * Buff basic methods
     * ================================================================================
     */
    public void addBuff(final Buff buff) {
        buffs.add(buff);
    }

    public List<Buff> fetchBuffs(final BuffType buffType) {
        return buffs.stream()
                .filter(buff -> buffType == buff.getBuffType())
                .collect(Collectors.toList());
    }

    public Buff fetchFirst(final BuffType buffType) {
        return buffs.stream()
                .filter(buff -> buffType == buff.getBuffType())
                .findFirst()
                .orElse(null);
    }

    private double applyValuedBuff(final Simulation simulation, final BuffType buffType, final Predicate<Double> predicate) {
        double totalValue = 0;
        for (final Buff buff : fetchBuffs(buffType)) {
            if (buff.shouldApply(simulation)) {
                final double value = buff.getValue(simulation);
                if (predicate.test(value)) {
                    totalValue += value;
                    buff.setApplied();
                }
            }
        }

        return RoundUtils.roundNearest(totalValue);
    }

    public double applyValuedBuff(final Simulation simulation, final BuffType buffType) {
        return applyValuedBuff(simulation, buffType, (value) -> true);
    }

    public double applyPositiveBuff(final Simulation simulation, final BuffType buffType) {
        return applyValuedBuff(simulation, buffType, (value) -> value > 0);
    }

    public double applyNegativeBuff(final Simulation simulation, final BuffType buffType) {
        return applyValuedBuff(simulation, buffType, (value) -> value < 0);
    }

    public boolean consumeFirstBuff(final Simulation simulation, final BuffType buffType) {
        final Buff buff = fetchFirst(buffType);
        if (buff != null && buff.shouldApply(simulation)) {
            buff.setApplied();
            return true;
        }
        return false;
    }

    public void activateEffectActivatingBuff(
            final Simulation simulation,
            final BuffType buffType
    ) {
        final List<Buff> buffsToActivate = fetchBuffs(buffType);
        simulation.setActivator(this);
        for (final Buff buff : buffsToActivate) {
            if (buff.shouldApply(simulation)) {
                if (simulation.getStatsLogger() != null) {
                    simulation.getStatsLogger().logEffectActivatingBuff(id, buffType);
                }
                buff.activate(simulation);

                // extra step since this is a buff
                buff.setApplied();
                checkBuffStatus();
            }
        }
        simulation.unsetActivator();
    }

    public void activateOnFieldBuff(final Simulation simulation, final boolean isEnterField) {
        final List<Buff> buffsToActivate = fetchBuffs(ON_FIELD_EFFECT);
        simulation.setActivator(this);
        for (final Buff buff : buffsToActivate) {
            if (simulation.getStatsLogger() != null) {
                simulation.getStatsLogger().logEffectActivatingBuff(id, ON_FIELD_EFFECT);
            }
            buff.activateOnFieldEffect(simulation, isEnterField);

            // extra step since this is a buff
            buff.setApplied();
            checkBuffStatus();
        }
        simulation.unsetActivator();
    }

    /*
     * ================================================================================
     * Buff check methods
     * ================================================================================
     */
    public boolean anyBuffMatch(final Predicate<Buff> predicate) {
        return buffs.stream().anyMatch(predicate);
    }

    public boolean isImmobilized() {
        return anyBuffMatch(BuffUtils::isImmobilizeDebuff);
    }

    public boolean isSelectable() {
        return !anyBuffMatch(buff -> buff.getBuffType() == PERMANENT_SLEEP);
    }

    public boolean isSkillInaccessible() {
        return anyBuffMatch(buff -> buff.getBuffType() == SKILL_SEAL || BuffUtils.isImmobilizeDebuff(buff));
    }

    public boolean isNpSealed() {
        return anyBuffMatch(buff -> buff.getBuffType() == NP_SEAL);
    }

    public boolean isNpInaccessible() {
        return anyBuffMatch(buff -> buff.getBuffType() == NP_SEAL || BuffUtils.isImmobilizeDebuff(buff));
    }

    public boolean isBuffExtended() {
        return anyBuffMatch(buff -> buff.getBuffType() == ATTACK_BUFF_DURATION_EXTEND);
    }

    /*
     * ================================================================================
     * Methods for specific effects
     * ================================================================================
     */
    public boolean activateGuts(final Simulation simulation) {
        final Buff gutsToApply = getGutsToActivate(simulation);
        if (gutsToApply != null) {
            gutsToApply.setApplied();
            if (simulation.getStatsLogger() != null) {
                final String message = String.format(
                        getTranslation(APPLICATION_SECTION, "%s activates %s"),
                        getTranslation(ENTITY_NAME_SECTION, id),
                        gutsToApply
                );
                simulation.getStatsLogger().logEffect(message);
            }
            final double value = gutsToApply.getValue(simulation);
            if (gutsToApply.isPercentageGuts()) {
                currentHp = (int) (getMaxHp() * value);
            } else {
                currentHp = (int) value;
            }
            receivedInstantDeath = false;
            checkBuffStatus();
            activateEffectActivatingBuff(simulation, TRIGGER_ON_GUTS_EFFECT);
        }

        return gutsToApply != null;
    }

    private void activateDamageReflect(final Simulation simulation) {
        for (final Buff buff : fetchBuffs(DAMAGE_REFLECT)) {
            if (buff.shouldApply(simulation)) {
                final int reflectedDamage = (int) (buff.getStoredDamage() * buff.getValue(simulation));
                if (reflectedDamage != 0) {
                    if (simulation.getStatsLogger() != null) {
                        simulation.getStatsLogger().logEffect(
                                String.format(
                                        getTranslation(APPLICATION_SECTION, "%s activates %s"),
                                        getTranslation(ENTITY_NAME_SECTION, id),
                                        buff + " * " + buff.getStoredDamage() + " = " + reflectedDamage
                                )
                        );
                    }


                    for (final Combatant combatant : simulation.getOtherTeam(this)) {
                        if (combatant != null) {
                            combatant.receiveNonHpBarBreakDamage(reflectedDamage);
                        }
                    }

                    buff.resetStoredDamage();
                    buff.setApplied();
                }
            }
        }

        checkBuffStatus();
    }

    /*
     * ================================================================================
     * Utility methods
     * ================================================================================
     */
    public void checkBuffStatus() {
        for (final Buff buff : buffs) {
            if (buff.isApplied()) {
                buff.decrementActiveTimes();
            }
        }

        clearInactiveBuff();

        final int maxHp = getMaxHp();
        if (currentHp > maxHp) {
            currentHp = maxHp;
        }
    }

    private void clearInactiveBuff() {
        for (int j = buffs.size() - 1; j >= 0; j -= 1) {
            if (buffs.get(j).isInactive()) {
                buffs.remove(j);
            }
        }
    }

    protected void clearPassiveBuff(final Combatant activator) {
        for (int j = buffs.size() - 1; j >= 0; j -= 1) {
            final Buff buff = buffs.get(j);
            if (buff.isPassive() && buff.getActivatorHash() == activator.getActivatorHash()) {
                buffs.remove(j);
            }
        }
    }

    int convertOC() {
        return 1;
    }

    @VisibleForTesting
    int calculateOverchargeLevel(final Simulation simulation, final int extraOvercharge) {
        int overchargeBuff = 0;

        for (final Buff buff : fetchBuffs(OVERCHARGE_BUFF)) {
            if (buff.shouldApply(simulation)) {
                overchargeBuff += buff.getValue();
                buff.setApplied();
            }
        }

        final int calculatedOvercharge = overchargeBuff + extraOvercharge + convertOC();
        if (calculatedOvercharge > 5) {
            return 5;
        } else if (calculatedOvercharge < 1) {
            return 1;
        } else {
            return calculatedOvercharge;
        }
    }

    private int calculateDoTDamage(
            final Simulation simulation,
            final BuffType dotType,
            final BuffType dotEffType
    ) {
        final double baseDamage = applyValuedBuff(simulation, dotType);
        final double effectiveness = applyValuedBuff(simulation, dotEffType);
        int totalDamage = Math.max(0, (int) RoundUtils.roundNearest(baseDamage * (1 + effectiveness)));

        if (totalDamage >= currentHp) {
            for (final Buff buff : fetchBuffs(PREVENT_DEATH_AGAINST_DOT)) {
                if (buff.shouldApply(simulation)) {
                    final String preventType = buff.getStringValue();
                    if (dotType.getType().equalsIgnoreCase(preventType) || Strings.isNullOrEmpty(preventType)) {
                        buff.setApplied();
                        return currentHp - 1;
                    }
                }
            }
        }

        return totalDamage;
    }

    private Buff getGutsToActivate(final Simulation simulation) {
        Buff gutsToApply = null;
        for (final Buff buff : fetchBuffs(GUTS)) {
            if (buff.shouldApply(simulation)) {
                if (gutsToApply == null || (gutsToApply.isIrremovable() && !buff.isIrremovable())) {
                    gutsToApply = buff;
                }
            }
        }
        return gutsToApply;
    }

    /*
     * ================================================================================
     * Make Copy
     * ================================================================================
     */
    protected Combatant(final Combatant other) {
        this.attack = other.attack;
        this.commandCards = Lists.newArrayList(other.commandCards);
        this.extraCommandCard = other.extraCommandCard;
        this.noblePhantasm = other.noblePhantasm;
        this.maxNpGauge = other.maxNpGauge;
        this.currentNpGauge = other.currentNpGauge;
        this.currentHpBarIndex = other.currentHpBarIndex;
        this.cumulativeTurnDamage = other.cumulativeTurnDamage;
        this.activatorHash = other.activatorHash;
        this.combatantData = other.combatantData;
        this.enemyData = other.enemyData;
        this.id = other.id;
        this.currentHp = other.currentHp;
        this.hpBars = Lists.newArrayList(other.hpBars);
        for (final Buff buff : other.buffs) {
            this.buffs.add(buff.makeCopy());
        }
        this.receivedInstantDeath = other.receivedInstantDeath;
        this.isAlly = other.isAlly;
        this.enemyPassiveSkills = Lists.newArrayList(other.enemyPassiveSkills);
    }

    public Combatant makeCopy() {
        return new Combatant(this);
    }

    /*
     * ================================================================================
     * Testing Constructors
     * ================================================================================
     */
    public Combatant() {
        this.hpBars = ImmutableList.of(100);
        this.currentHp = this.hpBars.get(this.currentHpBarIndex);
    }

    public Combatant(final String id) {
        this.id = id;
        this.hpBars = ImmutableList.of(1);
        this.combatantData = CombatantData.getDefaultInstance();
        this.enemyData = EnemyData.getDefaultInstance();
        this.currentHp = this.hpBars.get(this.currentHpBarIndex);
    }

    public Combatant(final String id, final List<Integer> hpBars) {
        if (hpBars.isEmpty()) {
            throw new IllegalArgumentException("Empty hpBars");
        }
        for (final int hpBar : hpBars) {
            if (hpBar <= 0) {
                throw new IllegalArgumentException("Invalid hpBar: " + hpBar + " in " + hpBars);
            }
        }

        this.id = id;
        this.hpBars = Lists.newArrayList(hpBars);
        this.currentHp = this.hpBars.get(this.currentHpBarIndex);
    }

    public Combatant(final String id, final CombatantData combatantData) {
        this.id = id;
        this.combatantData = combatantData;
        this.hpBars = ImmutableList.of(1);
        this.currentHp = this.hpBars.get(this.currentHpBarIndex);
    }
}
