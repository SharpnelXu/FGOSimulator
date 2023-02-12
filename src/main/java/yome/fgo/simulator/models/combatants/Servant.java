package yome.fgo.simulator.models.combatants;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import yome.fgo.data.proto.FgoStorageData.AppendSkillData;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmData;
import yome.fgo.data.proto.FgoStorageData.PassiveSkillData;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.data.proto.FgoStorageData.ServantOption;
import yome.fgo.data.proto.FgoStorageData.Status;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.craftessences.CraftEssence;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.data.proto.FgoStorageData.Target.ALL_CHARACTERS_INCLUDING_BACKUP;
import static yome.fgo.data.proto.FgoStorageData.Traits.SERVANT;
import static yome.fgo.simulator.gui.components.FormationSelector.defaultServantLevel;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SKILL_RANK_UP;

@Getter
@Setter
public class Servant extends Combatant {
    public static final double NP_CAP_1 = 1;
    public static final double NP_CAP_2 = 2;
    public static final double NP_CAP_3 = 3;
    public static final double NP_GAIN_PITY_THRESHOLD = 0.9899;

    private ServantData servantData;
    private ServantOption servantOption;
    private int attackStatusUp;
    private int hpStatusUp;
    private int ascension;
    private int servantLevel;
    private int noblePhantasmLevel;
    private int bond;
    private List<ActiveSkill> activeSkills = new ArrayList<>();
    private List<PassiveSkill> passiveSkills = new ArrayList<>();
    private List<AppendSkill> appendSkills = new ArrayList<>();

    private CraftEssence craftEssence;

    /*
     * ================================================================================
     * Execution Fields
     * ================================================================================
     */
    private double currentNp;

    /*
     * ================================================================================
     * NullSource & Master constructor
     * ================================================================================
     */
    public Servant(final String id) {
        super(id);
        this.isAlly = true;
    }

    /*
     * ================================================================================
     * Enemy Servant constructor
     * ================================================================================
     */
    public Servant(final ServantData servantData, final EnemyData enemyData) {
        super(getAscensionOrLastData(servantData, enemyData.getServantAscension()).getCombatantData(), enemyData);

        this.servantData = servantData;

        this.ascension = enemyData.getServantAscension();
        final ServantAscensionData servantAscensionData = servantData.getServantAscensionData(this.ascension - 1);
        final int rarity = servantAscensionData.getCombatantData().getRarity();
        this.servantLevel = Math.min(servantAscensionData.getServantStatusDataCount(), defaultServantLevel(rarity));
        this.noblePhantasmLevel = 1;
        this.servantOption = ServantOption.newBuilder()
                .setAscension(this.ascension)
                .setServantLevel(this.servantLevel)
                .setNoblePhantasmLevel(this.noblePhantasmLevel)
                .setNoblePhantasmRank(1)
                .addAllActiveSkillLevels(List.of(1, 1, 1))
                .addAllActiveSkillRanks(List.of(1, 1, 1))
                .build();
        buildAscension(this.ascension);
    }

    /*
     * ================================================================================
     * Ally Servant constructors
     * ================================================================================
     */
    public Servant(final ServantData servantData, final ServantOption servantOption) {
        this("servant" + servantData.getServantNum(), servantData, servantOption);
    }

    public Servant(final String id, final ServantData servantData, final ServantOption servantOption) {
        this.id = id;
        this.servantData = servantData;
        this.servantOption = servantOption;
        this.servantLevel = servantOption.getServantLevel();
        this.ascension = servantOption.getAscension();
        this.attackStatusUp = servantOption.getAttackStatusUp();
        this.hpStatusUp = servantOption.getHealthStatusUp();
        this.noblePhantasmLevel = servantOption.getNoblePhantasmLevel();
        this.bond = servantOption.getBond();
        this.isAlly = true;

        buildAscension(this.ascension);

        this.currentHp = getMaxHp();
    }

    /*
     * ================================================================================
     * Initiation methods - called by simulation only
     * ================================================================================
     */
    @Override
    public void initiate(final Simulation simulation) {
        super.initiate(simulation);

        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logActivatePassiveSkill(getId());
        }

        simulation.setActivator(this);
        simulation.setActivatingServantPassiveEffects(true);

        for (final PassiveSkill passiveSkill : passiveSkills) {
            passiveSkill.activate(simulation);
        }
        for (final AppendSkill appendSkill : appendSkills) {
            appendSkill.activate(simulation);
        }

        simulation.setActivatingServantPassiveEffects(false);

        if (craftEssence != null) {
            simulation.setActivatingCePassiveEffects(true);
            craftEssence.activate(simulation);
            simulation.setActivatingCePassiveEffects(false);
        }

        simulation.unsetActivator();
    }

    public void equipCraftEssence(final CraftEssence craftEssence) {
        this.craftEssence = craftEssence;
    }

    /*
     * ================================================================================
     * Basic access methods
     * ================================================================================
     */
    public int getAttack() {
        final int craftEssenceAtk = craftEssence == null ? 0 : craftEssence.getAttack();
        return attack + craftEssenceAtk + attackStatusUp;
    }

    public double getDefNpCharge() {
        return servantData.getServantAscensionData(ascension - 1).getDefenseNpRate();
    }

    public String getActiveSkillIconPath(final Simulation simulation, final int activeSkillIndex) {
        simulation.setActivator(this);

        final int currentRank = getCurrentSkillRank(simulation, activeSkillIndex);
        final String iconPath = activeSkills.get(activeSkillIndex).getIconPath(currentRank);

        simulation.unsetActivator();

        return iconPath;
    }

    @Override
    public List<String> getAllTraits(final Simulation simulation) {
        final ImmutableList.Builder<String> allTraits = ImmutableList.builder();
        allTraits.addAll(super.getAllTraits(simulation));
        allTraits.add(SERVANT.name());
        return allTraits.build();
    }

    /*
     * ================================================================================
     * Methods for basic effects
     * ================================================================================
     */
    @Override
    public void changeNp(final double npChange) {
        currentNp += npChange;
        currentNp = RoundUtils.roundNearest(currentNp);

        final double npCap = getNpCap(noblePhantasmLevel);
        if (currentNp > npCap) {
            currentNp = npCap;
        } else if (shouldApplyNpPity(currentNp, npChange)) {
            currentNp = NP_CAP_1;
        } else if (currentNp < 0) {
            currentNp = 0;
        }
    }

    @Override
    public void decreaseActiveSkillsCoolDown(final int decrease) {
        for (final ActiveSkill activeSkill : activeSkills) {
            activeSkill.decreaseCoolDown(decrease);
        }
    }

    @Override
    public void endOfMyTurn(final Simulation simulation) {
        super.endOfMyTurn(simulation);

        if (!isSkillInaccessible()) {
            for (final ActiveSkill activeSkill : activeSkills) {
                activeSkill.decreaseCoolDown(1);
            }
        }
    }

    /*
     * ================================================================================
     * Activation (player action) methods
     * ================================================================================
     */
    public boolean canActivateActiveSkill(final Simulation simulation, final int activeSkillIndex) {
        simulation.setActivator(this);

        final int currentRank = getCurrentSkillRank(simulation, activeSkillIndex);

        final boolean canActivate = !isSkillInaccessible() &&
                activeSkills.get(activeSkillIndex).canActivate(simulation, currentRank);

        simulation.unsetActivator();

        return canActivate;
    }

    public void activateActiveSkill(final Simulation simulation, final int activeSkillIndex) {
        simulation.setActivator(this);

        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logActivateActiveSkill(getId(), activeSkillIndex);
        }
        final int currentRank = getCurrentSkillRank(simulation, activeSkillIndex);

        activeSkills.get(activeSkillIndex).activate(simulation, currentRank);

        simulation.unsetActivator();
    }

    @Override
    public boolean npCheck() {
        if (!isAlly) {
            return super.npCheck();
        }

        return currentNp >= 1;
    }

    @Override
    public void resetNp() {
        if (!isAlly) {
            super.resetNp();
            return;
        }

        currentNp = 0;
    }

    /*
     * ================================================================================
     * Methods for specific effects
     * ================================================================================
     */
    public void changeAscension(final Simulation simulation, final int newAscension) {
        for (final Combatant clearBuff : TargetUtils.getTargets(simulation, ALL_CHARACTERS_INCLUDING_BACKUP)) {
            clearBuff.clearPassiveBuff(this);
        }

        final List<Integer> coolDowns = activeSkills.stream()
                .map(ActiveSkill::getCurrentCoolDown)
                .collect(Collectors.toList());

        ascension = newAscension;
        buildAscension(newAscension);

        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logActivatePassiveSkill(getId());
        }

        simulation.setActivator(this);
        simulation.setActivatingServantPassiveEffects(true);

        // should all be buffs
        for (final PassiveSkill passiveSkill : passiveSkills) {
            passiveSkill.activate(simulation);
        }
        for (final AppendSkill appendSkill : appendSkills) {
            appendSkill.activateOnlyBuffGrantingEffects(simulation);
        }

        for (int i = 0; i < activeSkills.size(); i += 1) {
            if (coolDowns.size() > i) {
                activeSkills.get(i).setCurrentCoolDown(coolDowns.get(i));
            }
        }

        simulation.setActivatingServantPassiveEffects(false);
        simulation.unsetActivator();
    }

    /*
     * ================================================================================
     * Utility methods
     * ================================================================================
     */
    private static ServantAscensionData getAscensionOrLastData(final ServantData servantData, final int ascension) {
        final int index = servantData.getServantAscensionDataCount() < ascension ?
                servantData.getServantAscensionDataCount() - 1 : ascension - 1;
        return servantData.getServantAscensionData(index);
    }

    private void buildAscension(final int ascension) {
        final ServantAscensionData servantAscensionData = getAscensionOrLastData(servantData, ascension);
        combatantData = servantAscensionData.getCombatantData();

        final Status servantStatus = servantAscensionData.getServantStatusData(servantLevel - 1);
        attack = servantStatus.getATK();
        if (isAlly) {
            hpBars = new ArrayList<>(ImmutableList.of(servantStatus.getHP()));
        }

        final NoblePhantasmData noblePhantasmData = servantAscensionData.getNoblePhantasmUpgrades()
                .getNoblePhantasmData(servantOption.getNoblePhantasmRank() - 1);
        noblePhantasm = new NoblePhantasm(noblePhantasmData, noblePhantasmLevel);

        activeSkills = new ArrayList<>();
        for (int i = 0; i < servantAscensionData.getActiveSkillUpgradesCount(); i += 1) {
            activeSkills.add(new ActiveSkill(servantAscensionData.getActiveSkillUpgrades(i), servantOption.getActiveSkillLevels(i)));
        }

        passiveSkills = new ArrayList<>();
        for (final PassiveSkillData passiveSkillData : servantAscensionData.getPassiveSkillDataList()) {
            passiveSkills.add(new PassiveSkill(passiveSkillData));
        }

        appendSkills = new ArrayList<>();
        for (int i = 0; i < servantAscensionData.getAppendSkillDataCount(); i += 1) {
            final int appendSkillLevel = servantOption.getAppendSkillLevelsCount() > i
                    ? servantOption.getAppendSkillLevels(i)
                    : 0;
            if (appendSkillLevel > 0) {
                final AppendSkillData appendSkillData = servantAscensionData.getAppendSkillData(i);
                appendSkills.add(new AppendSkill(appendSkillData, appendSkillLevel));
            } else {
                appendSkills.add(new AppendSkill(AppendSkillData.newBuilder().build(), 1));
            }
        }

        commandCards = new ArrayList<>();
        for (int i = 0; i < servantAscensionData.getCommandCardDataCount(); i += 1) {
            final CommandCardData commandCardData = servantAscensionData.getCommandCardData(i);
            if (servantOption.getCommandCardOptionsCount() == 0) {
                commandCards.add(new CommandCard(commandCardData));
            } else {
                commandCards.add(new CommandCard(commandCardData, servantOption.getCommandCardOptions(i)));
            }
        }

        extraCommandCard = new CommandCard(servantAscensionData.getExtraCard());
    }

    private int getCurrentSkillRank(final Simulation simulation, final int activeSkillIndex) {
        final int currentRank = servantOption.getActiveSkillRanks(activeSkillIndex);
        int increasedRank = 0;
        for (final Buff buff : fetchBuffs(SKILL_RANK_UP)) {
            if (buff.shouldApply(simulation)) {
                increasedRank += 1;
            }
        }
        return currentRank + increasedRank;
    }

    @Override
    int convertOC() {
        return (int) currentNp;
    }

    public static double getNpCap(final int npLevel) {
        if (npLevel == 1) {
            return NP_CAP_1;
        } else if (npLevel < 5) {
            return NP_CAP_2;
        } else {
            return NP_CAP_3;
        }
    }

    public static boolean shouldApplyNpPity(final double currentNp, final double npChange) {
        return Math.signum(npChange) > 0 && currentNp < NP_CAP_1 && currentNp > NP_GAIN_PITY_THRESHOLD;
    }

    /*
     * ================================================================================
     * Make Copy
     * ================================================================================
     */
    private Servant(final Servant other) {
        super(other);

        this.servantData = other.servantData;
        this.servantOption = other.servantOption;
        this.attackStatusUp = other.attackStatusUp;
        this.hpStatusUp = other.hpStatusUp;
        this.ascension = other.ascension;
        this.servantLevel = other.servantLevel;
        this.noblePhantasmLevel = other.noblePhantasmLevel;
        this.bond = other.bond;
        this.activeSkills = Lists.newArrayList();
        for (final ActiveSkill activeSkill : other.activeSkills) {
            this.activeSkills.add(activeSkill.makeCopy());
        }
        this.passiveSkills = Lists.newArrayList(other.passiveSkills);
        this.appendSkills = Lists.newArrayList(other.appendSkills);
        this.craftEssence = other.craftEssence;
        this.currentNp = other.currentNp;
    }

    @Override
    public Servant makeCopy() {
        return new Servant(this);
    }

    /*
     * ================================================================================
     * Test constructors
     * ================================================================================
     */
    public Servant() {
        super();
        this.isAlly = true;
    }

    public Servant(final String id, final CombatantData combatantData) {
        super(id, combatantData);
    }
}
