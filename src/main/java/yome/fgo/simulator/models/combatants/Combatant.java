package yome.fgo.simulator.models.combatants;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.Alignment;
import yome.fgo.data.proto.FgoStorageData.Attribute;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.data.proto.FgoStorageData.PassiveSkillData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.buffs.AttackBuffDurationExtend;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.Burn;
import yome.fgo.simulator.models.effects.buffs.CardTypeChange;
import yome.fgo.simulator.models.effects.buffs.Curse;
import yome.fgo.simulator.models.effects.buffs.DamageReflect;
import yome.fgo.simulator.models.effects.buffs.DeathEffect;
import yome.fgo.simulator.models.effects.buffs.DefenseBuff;
import yome.fgo.simulator.models.effects.buffs.DelayedEffect;
import yome.fgo.simulator.models.effects.buffs.EffectActivatingBuff;
import yome.fgo.simulator.models.effects.buffs.EndOfTurnEffect;
import yome.fgo.simulator.models.effects.buffs.EnterFieldEffect;
import yome.fgo.simulator.models.effects.buffs.GrantTrait;
import yome.fgo.simulator.models.effects.buffs.Guts;
import yome.fgo.simulator.models.effects.buffs.HPBreakEffect;
import yome.fgo.simulator.models.effects.buffs.ImmobilizeDebuff;
import yome.fgo.simulator.models.effects.buffs.LeaveFieldEffect;
import yome.fgo.simulator.models.effects.buffs.MaxHpBuff;
import yome.fgo.simulator.models.effects.buffs.NpCardTypeChange;
import yome.fgo.simulator.models.effects.buffs.NpSeal;
import yome.fgo.simulator.models.effects.buffs.PermanentSleep;
import yome.fgo.simulator.models.effects.buffs.Poison;
import yome.fgo.simulator.models.effects.buffs.PreventDeathAgainstDoT;
import yome.fgo.simulator.models.effects.buffs.RemoveTrait;
import yome.fgo.simulator.models.effects.buffs.SkillSeal;
import yome.fgo.simulator.models.effects.buffs.StartOfTurnEffect;
import yome.fgo.simulator.models.effects.buffs.TriggerOnGutsEffect;
import yome.fgo.simulator.models.effects.buffs.ValuedBuff;
import yome.fgo.simulator.utils.RoundUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.BuffUtils.isImmobilizeOrSeal;
import static yome.fgo.simulator.utils.BuffUtils.shouldDecreaseNumTurnsActiveAtMyTurn;
import static yome.fgo.simulator.utils.FateClassUtils.getClassMaxNpGauge;

@Getter
public class Combatant {
    // enemy only data
    private int maxNpGauge;
    private int currentNpGauge;
    private int currentHpBarIndex;
    private int cumulativeTurnDamage;
    private int activatorHash;

    protected CombatantData combatantData;
    protected EnemyData enemyData;

    protected String id;
    protected int currentHp;
    protected List<Integer> hpBars;
    protected List<Buff> buffs = Lists.newArrayList();

    private List<PassiveSkill> enemyPassiveSkills = Lists.newArrayList();

    private boolean receivedInstantDeath;
    protected boolean isAlly;

    // for testing
    public Combatant() {
        this.hpBars = ImmutableList.of(100);
        this.currentHp = this.hpBars.get(this.currentHpBarIndex);
    }

    // for testing
    public Combatant(final String id) {
        this.id = id;
        this.hpBars = ImmutableList.of(1);
        this.combatantData = CombatantData.getDefaultInstance();
        this.enemyData = EnemyData.getDefaultInstance();
        this.currentHp = this.hpBars.get(this.currentHpBarIndex);
    }

    // for testing
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

    // for testing
    public Combatant(final String id, final CombatantData combatantData) {
        this.id = id;
        this.combatantData = combatantData;
        this.hpBars = ImmutableList.of(1);
        this.currentHp = this.hpBars.get(this.currentHpBarIndex);
    }

    public Combatant(final CombatantData combatantData, final EnemyData enemyData) {
        this(enemyData.getEnemyBaseId(), enemyData.getHpBarsList());

        if (enemyData.hasCombatantDataOverride()) {
            this.combatantData = mergeWithOverride(combatantData, enemyData.getCombatantDataOverride());
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

    public static CombatantData mergeWithOverride(final CombatantData base, final CombatantData override) {
        /* See comments below
        final CombatantData.Builder builder = base.toBuilder();
        builder.mergeFrom(override);
        builder.clearAlignments();
        builder.addAllAlignments(
                override.getAlignmentsCount() == 0
                        ? base.getAlignmentsList()
                        : override.getAlignmentsList()
        );
        builder.setDeathRate(override.getDeathRate());
        builder.clearTraits();
        builder.addAllTraits(
                override.getTraitsCount() == 0
                        ? base.getTraitsList()
                        : override.getTraitsList()
        );
        builder.clearEnemyPassiveSkillData();
        builder.addAllEnemyPassiveSkillData(
                override.getEnemyPassiveSkillDataCount() == 0
                        ? base.getEnemyPassiveSkillDataList()
                        : override.getEnemyPassiveSkillDataList()
        );
        return builder.build();
         */

        // Proto3 doesn't have a `isSet` function, so just use override since it should be built from base (using editor)
        // Keeping the logic in case I want to change it back
        return override;
    }

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

    public int getAttack() {
        // enemy don't have attack
        return 0;
    }

    public FateClass getFateClass() {
        return combatantData.getFateClass();
    }

    public Attribute getAttribute() {
        return combatantData.getAttribute();
    }

    public List<String> getAllTraits(final Simulation simulation) {
        final List<String> allTraits = new ArrayList<>();
        allTraits.addAll(combatantData.getTraitsList());
        allTraits.add(combatantData.getGender().name());
        allTraits.add(combatantData.getAttribute().name());
        allTraits.addAll(combatantData.getAlignmentsList().stream().map(Alignment::name).collect(Collectors.toList()));

        final Set<String> traitsToRemove = new HashSet<>();
        for (final Buff buff : buffs) {
            if (buff instanceof GrantTrait && buff.shouldApply(simulation)) {
                allTraits.add(((GrantTrait) buff).getTrait());
                buff.setApplied();
            } else if (buff instanceof RemoveTrait && buff.shouldApply(simulation)) {
                traitsToRemove.add(((RemoveTrait) buff).getTrait());
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

        for (final Buff buff : buffs) {
            if (buff instanceof MaxHpBuff) {
                additionalHp += ((MaxHpBuff) buff).getChange();
            }
        }

        return Math.max(hpBars.get(currentHpBarIndex) + (int) RoundUtils.roundNearest(additionalHp), 1);
    }

    public boolean hasNextHpBar() {
        return currentHpBarIndex < hpBars.size() - 1;
    }

    public void addBuff(final Buff buff) {
        buffs.add(buff);
    }

    public double applyBuff(final Simulation simulation, final Class<? extends ValuedBuff> buffClass) {
        double totalValue = 0;
        for (final Buff buff : buffs) {
            if (buffClass.isInstance(buff) && buff.shouldApply(simulation)) {
                totalValue += buffClass.cast(buff).getValue(simulation);
                buff.setApplied();
            }
        }
        return RoundUtils.roundNearest(totalValue);
    }

    public double applyPositiveBuff(final Simulation simulation, final Class<? extends ValuedBuff> buffClass) {
        double totalValue = 0;
        for (final Buff buff : buffs) {
            if (buffClass.isInstance(buff) && buff.shouldApply(simulation)) {
                final double value = buffClass.cast(buff).getValue(simulation);
                if (value > 0) {
                    totalValue += value;
                    buff.setApplied();
                }
            }
        }
        return RoundUtils.roundNearest(totalValue);
    }

    public double applyNegativeBuff(final Simulation simulation, final Class<? extends ValuedBuff> buffClass) {
        double totalValue = 0;
        for (final Buff buff : buffs) {
            if (buffClass.isInstance(buff) && buff.shouldApply(simulation)) {
                final double value = buffClass.cast(buff).getValue(simulation);
                if (value < 0) {
                    totalValue += value;
                    buff.setApplied();
                }
            }
        }
        return RoundUtils.roundNearest(totalValue);
    }

    public boolean consumeBuffIfExist(final Simulation simulation, final Class<? extends Buff> buffClass) {
        for (final Buff buff : buffs) {
            if (buffClass.isInstance(buff) && buff.shouldApply(simulation)) {
                buff.setApplied();
                return true;
            }
        }
        return false;
    }

    public boolean isImmobilized() {
        for (final Buff buff : buffs) {
            if (buff instanceof ImmobilizeDebuff) {
                return true;
            }
        }
        return false;
    }

    public boolean isSelectable() {
        for (final Buff buff : buffs) {
            if (buff instanceof PermanentSleep) {
                return false;
            }
        }
        return true;
    }

    public boolean isSkillInaccessible() {
        for (final Buff buff : buffs) {
            if (buff instanceof SkillSeal || buff instanceof ImmobilizeDebuff) {
                return true;
            }
        }
        return false;
    }

    public boolean isNpSealed() {
        for (final Buff buff : buffs) {
            if (buff instanceof NpSeal) {
                return true;
            }
        }
        return false;
    }

    public boolean isNpInaccessible() {
        for (final Buff buff : buffs) {
            if (buff instanceof NpSeal || buff instanceof ImmobilizeDebuff) {
                return true;
            }
        }
        return false;
    }

    public boolean isBuffExtended() {
        for (final Buff buff : buffs) {
            if (buff instanceof AttackBuffDurationExtend) {
                return true;
            }
        }
        return false;
    }

    public boolean isAlreadyDead() {
        return currentHp <= 0;
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

    public void enterField(final Simulation simulation) {
        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logEnterField(id);
        }

        activateEffectActivatingBuff(simulation, EnterFieldEffect.class);
    }

    public void leaveField(final Simulation simulation) {
        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logLeaveField(id);
        }
        activateEffectActivatingBuff(simulation, LeaveFieldEffect.class);
    }

    public void death(final Simulation simulation) {
        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logDeath(id);
        }
        activateEffectActivatingBuff(simulation, DeathEffect.class);
    }

    public void receiveDamage(final int damage) {
        for (final Buff buff : buffs) {
            if (buff instanceof DamageReflect) {
                final DamageReflect damageReflect = (DamageReflect) buff;
                damageReflect.storeDamage(damage);
            }
        }

        currentHp -= damage;
    }

    public void hpBarBreak(final Simulation simulation) {
        receivedInstantDeath = false;
        currentHpBarIndex += 1;
        currentHp = hpBars.get(currentHpBarIndex);
        activateEffectActivatingBuff(simulation, HPBreakEffect.class);
    }

    public void receiveNonHpBarBreakDamage(final int damage) {
        currentHp -= damage;
        if (currentHp <= 0 && hasNextHpBar()) {
            currentHp = 1;
        }
    }

    private void activateDamageReflect(final Simulation simulation) {
        for (final Buff buff : buffs) {
            if (buff instanceof DamageReflect && buff.shouldApply(simulation)) {
                final DamageReflect damageReflect = (DamageReflect) buff;

                final int reflectedDamage = (int) (damageReflect.getStoredDamage() * damageReflect.getValue(simulation));
                if (reflectedDamage != 0) {
                    if (simulation.getStatsLogger() != null) {
                        simulation.getStatsLogger().logEffect(
                                String.format(
                                        getTranslation(APPLICATION_SECTION, "%s activates %s"),
                                        getTranslation(ENTITY_NAME_SECTION, id),
                                        damageReflect + " * " + damageReflect.getStoredDamage() + " = " + reflectedDamage
                                )
                        );
                    }


                    for (final Combatant combatant : simulation.getOtherTeam(this)) {
                        if (combatant != null) {
                            combatant.receiveNonHpBarBreakDamage(reflectedDamage);
                        }
                    }

                    damageReflect.resetDamageStored();
                    damageReflect.setApplied();
                }
            }
        }

        checkBuffStatus();
    }

    public void endOfYourTurn(final Simulation simulation) {
        activateEffectActivatingBuff(simulation, DelayedEffect.class);

        activateDamageReflect(simulation);

        final boolean isBuffExtended = isBuffExtended();

        for (final Buff buff : buffs) {
            if ((isBuffExtended || !shouldDecreaseNumTurnsActiveAtMyTurn(buff)) && !isImmobilizeOrSeal(buff)) {
                buff.decreaseNumTurnsActive();
            }
        }

        clearInactiveBuff();
    }

    public void startOfMyTurn(final Simulation simulation) {
        activateEffectActivatingBuff(simulation, StartOfTurnEffect.class);
    }

    public int calculateDoTDamage(
            final Simulation simulation,
            final Class<? extends ValuedBuff> doTClass,
            final Class<? extends ValuedBuff> doTEffClass
    ) {
        final double baseDamage = applyBuff(simulation, doTClass);
        final double effectiveness = applyBuff(simulation, doTEffClass);
        int totalDamage = Math.max(0, (int) RoundUtils.roundNearest(baseDamage * (1 + effectiveness)));

        if (totalDamage >= currentHp) {
            for (final Buff buff : buffs) {
                if (buff instanceof PreventDeathAgainstDoT && buff.shouldApply(simulation)) {
                    final String preventType = ((PreventDeathAgainstDoT) buff).getType();
                    if (doTClass.getSimpleName().equalsIgnoreCase(preventType) || Strings.isNullOrEmpty(preventType)) {
                        buff.setApplied();
                        return currentHp - 1;
                    }
                }
            }
        }

        return totalDamage;
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
                buff.decreaseNumTurnsActive();
            }
        }
        clearInactiveBuff();

        final int poisonDamage = calculateDoTDamage(simulation, Poison.class, Poison.getEffectivenessClass());
        final int burnDamage = calculateDoTDamage(simulation, Burn.class, Burn.getEffectivenessClass());
        final int curseDamage = calculateDoTDamage(simulation, Curse.class, Curse.getEffectivenessClass());

        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logDoT(id, poisonDamage, burnDamage, curseDamage);
        }

        receiveNonHpBarBreakDamage(poisonDamage + burnDamage + curseDamage);

        activateEffectActivatingBuff(simulation, EndOfTurnEffect.class);

        if (isBuffExtended()) {
            return;
        }

        for (final Buff buff : buffs) {
            if (shouldDecreaseNumTurnsActiveAtMyTurn(buff)) {
                buff.decreaseNumTurnsActive();
            }
        }

        clearInactiveBuff();
    }

    public void checkBuffStatus() {
        for (final Buff buff : buffs) {
            if (buff.isApplied()) {
                buff.decreaseNumTimeActive();
            }
        }

        clearInactiveBuff();

        final int maxHp = getMaxHp();
        if (currentHp > maxHp) {
            currentHp = maxHp;
        }
    }

    public void clearInactiveBuff() {
        for (int j = buffs.size() - 1; j >= 0; j -= 1) {
            if (buffs.get(j).isInactive()) {
                buffs.remove(j);
            }
        }
    }

    public void clearPassiveBuff(final Combatant activator) {
        for (int j = buffs.size() - 1; j >= 0; j -= 1) {
            final Buff buff = buffs.get(j);
            if (buff.isPassive() && buff.getActivatorHash() == activator.getActivatorHash()) {
                buffs.remove(j);
            }
        }
    }

    public void activateEffectActivatingBuff(
            final Simulation simulation,
            final Class<? extends EffectActivatingBuff> buffClass
    ) {
        final List<EffectActivatingBuff> buffsToActivate = Lists.newArrayList();
        for (final Buff buff : buffs) {
            if (buffClass.isInstance(buff)) {
                buffsToActivate.add((EffectActivatingBuff) buff);
            }
        }
        simulation.setActivator(this);
        for (final EffectActivatingBuff buff : buffsToActivate) {
            if (buff.shouldApply(simulation)) {
                if (simulation.getStatsLogger() != null) {
                    simulation.getStatsLogger().logEffectActivatingBuff(id, buffClass);
                }
                buff.activate(simulation);

                // extra step since this is a buff
                buff.setApplied();
                checkBuffStatus();
            }
        }
        simulation.unsetActivator();
    }

    public CardTypeChange hasCardTypeChangeBuff() {
        for (final Buff buff : buffs) {
            if (buff instanceof CardTypeChange) {
                return (CardTypeChange) buff;
            }
        }
        return null;
    }

    public NpCardTypeChange hasNpCardTypeChangeBuff() {
        for (final Buff buff : buffs) {
            if (buff instanceof NpCardTypeChange) {
                return (NpCardTypeChange) buff;
            }
        }
        return null;
    }

    public boolean activateGuts(final Simulation simulation) {
        final Guts gutsToApply = getGutsToActivate(simulation);
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
            if (gutsToApply.isPercentageGuts()) {
                currentHp = (int) (getMaxHp() * gutsToApply.getPercent());
            } else {
                currentHp = gutsToApply.getGutsLeft();
            }
            receivedInstantDeath = false;
            checkBuffStatus();
            activateEffectActivatingBuff(simulation, TriggerOnGutsEffect.class);
        }

        return gutsToApply != null;
    }

    public void changeNp(final double percentNpChange) {
    }

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
        // non-lethal
        if (currentHp <= 0 && !isLethal) {
            currentHp = 1;
        }
    }

    public void decreaseActiveSkillsCoolDown(final int change) {

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

    public Gender getGender() {
        return combatantData.getGender();
    }

    public double getDeathRate() {
        return combatantData.getDeathRate();
    }

    public void instantDeath() {
        receivedInstantDeath = true;
        currentHp = 0;
    }

    public int getRarity() {
        return combatantData.getRarity();
    }

    protected Combatant(final Combatant other) {
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

    public boolean isAlive(final Simulation simulation) {
        if (currentHp > 0 || hasNextHpBar()) {
            return true;
        }
        return getGutsToActivate(simulation) != null;
    }

    public Guts getGutsToActivate(final Simulation simulation) {
        Guts gutsToApply = null;
        for (final Buff buff : buffs) {
            if (buff instanceof Guts && buff.shouldApply(simulation)) {
                if (gutsToApply == null || (gutsToApply.isIrremovable() && !buff.isIrremovable())) {
                    gutsToApply = (Guts) buff;
                }
            }
        }
        return gutsToApply;
    }

    public int getActivatorHash() {
        return activatorHash;
    }
}
