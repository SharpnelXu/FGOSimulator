package yome.fgo.simulator.models.combatants;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillData;
import yome.fgo.data.proto.FgoStorageData.AppendSkillData;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmData;
import yome.fgo.data.proto.FgoStorageData.PassiveSkillData;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.data.proto.FgoStorageData.ServantOption;
import yome.fgo.data.proto.FgoStorageData.Status;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.craftessences.CraftEssence;
import yome.fgo.simulator.models.effects.CommandCardExecution;
import yome.fgo.simulator.utils.RoundUtils;

import java.util.ArrayList;
import java.util.List;

import static yome.fgo.simulator.models.traits.Traits.SERVANT;

@NoArgsConstructor
@Getter
@Setter
public class Servant extends Combatant {
    public static final double NP_CAP_1 = 100;
    public static final double NP_CAP_2 = 200;
    public static final double NP_CAP_3 = 300;
    public static final double NP_GAIN_PITY_THRESHOLD = 98.99;

    private ServantData servantData;
    private int attack;
    private int maxHp;
    private int attackStatusUp;
    private int hpStatusUp;
    private int ascension;
    private int servantLevel;
    private int noblePhantasmLevel;
    private int bond;

    private List<CommandCard> commandCards;
    private CommandCard extraCommandCard;

    private NoblePhantasm noblePhantasm;
    private List<ActiveSkill> activeSkills;
    private List<PassiveSkill> passiveSkills;
    private List<AppendSkill> appendSkills;

    private CraftEssence craftEssence;

    private double currentNp;

    // for testing
    public Servant(final String id) {
        super(id);
    }

    // for testing
    public Servant(final String id, final CombatantData combatantData) {
        super(id, combatantData);
    }

    private static CombatantData getLastCombatantData(final ServantData servantData, final int ascension) {
        final int index = servantData.getServantAscensionDataCount() < ascension ?
                servantData.getServantAscensionDataCount() - 1 : ascension - 1;
        return servantData.getServantAscensionData(index).getCombatantData();
    }

    // when as enemy
    public Servant(final ServantData servantData, final EnemyData enemyData) {
        super(getLastCombatantData(servantData, enemyData.getServantAscension()), enemyData);

        this.servantData = servantData; // shouldn't be used, but won't hurt to store a reference
    }

    public Servant(final String id, final ServantData servantData, final ServantOption servantOption) {
        super(id, getLastCombatantData(servantData, servantOption.getAscension()));
        this.servantData = servantData;
        this.servantLevel = servantOption.getServantLevel();
        this.ascension = servantOption.getAscension();

        final Status servantStatus = servantData.getServantAscensionData(ascension)
                .getServantStatusData(servantLevel - 1);
        this.attack = servantStatus.getATK();
        this.maxHp = servantStatus.getHP();
        this.attackStatusUp = servantOption.getAttackStatusUp();
        this.hpStatusUp = servantOption.getHealthStatusUp();
        this.noblePhantasmLevel = servantOption.getNoblePhantasmLevel();
        this.bond = servantOption.getBond();

        final ServantAscensionData servantAscensionData = servantData.getServantAscensionData(this.ascension);
        final NoblePhantasmData noblePhantasmData = servantAscensionData.getNoblePhantasmUpgrades()
                .getNoblePhantasmData(servantOption.getNoblePhantasmRank());
        this.noblePhantasm = new NoblePhantasm(noblePhantasmData, this.noblePhantasmLevel);

        this.activeSkills = new ArrayList<>();
        for (int i = 0; i < servantAscensionData.getActiveSkillUpgradesCount(); i++) {
            final ActiveSkillData activeSkillData = servantAscensionData.getActiveSkillUpgrades(i)
                    .getActiveSkillData(servantOption.getActiveSkillRanks(i));
            this.activeSkills.add(new ActiveSkill(activeSkillData, servantOption.getActiveSkillLevels(i)));
        }

        this.passiveSkills = new ArrayList<>();
        for (final PassiveSkillData passiveSkillData : servantAscensionData.getPassiveSkillDataList()) {
            this.passiveSkills.add(new PassiveSkill(passiveSkillData));
        }

        this.appendSkills = new ArrayList<>();
        for (int i = 0; i < servantAscensionData.getAppendSkillDataCount(); i++) {
            final AppendSkillData appendSkillData = servantAscensionData.getAppendSkillData(i);
            this.appendSkills.add(new AppendSkill(appendSkillData, servantOption.getAppendSkillLevels(i)));
        }

        this.commandCards = new ArrayList<>();
        for (int i = 0; i < servantAscensionData.getCommandCardDataCount(); i++) {
            final CommandCardData commandCardData = servantAscensionData.getCommandCardData(i);
            this.commandCards.add(new CommandCard(commandCardData, servantOption.getCommandCardOptions(i)));
        }

        this.extraCommandCard = new CommandCard(servantAscensionData.getExtraCard());
    }

    @Override
    public void initiate(final Simulation simulation) {
    }

    public int getAttack() {
        return attack + craftEssence.getAttack() + attackStatusUp;
    }

    public void activateActiveSkill(final Simulation simulation, final int activeSkillIndex) {
        simulation.setActivator(this);
        final ActiveSkill activeSkill = activeSkills.get(activeSkillIndex);
        activeSkill.activate(simulation);
        simulation.setActivator(null);
    }

    public void activateNoblePhantasm(final Simulation simulation, final int extraOvercharge) {
        simulation.setAttacker(this);
        simulation.setCurrentCommandCard(noblePhantasm);

        // TODO: missing overchargeIncreaseBuff
        final int overchargeLevel = Math.max(extraOvercharge + Math.min(1, (int) currentNp / 100), 5);
        noblePhantasm.activate(simulation, overchargeLevel);

        simulation.setCurrentCommandCard(null);
        simulation.setAttacker(null);
    }

    public void activateCommandCard(
            final Simulation simulation,
            final int commandCardIndex,
            final int chainIndex,
            final boolean isCriticalStrike,
            final boolean isTypeChain,
            final CommandCardType firstCardType
    ) {
        simulation.setAttacker(this);
        simulation.setCurrentCommandCard(commandCards.get(commandCardIndex));

        CommandCardExecution.executeCommandCard(simulation, chainIndex, isCriticalStrike, firstCardType, isTypeChain);

        simulation.setCurrentCommandCard(null);
        simulation.setAttacker(null);
    }

    public void activateExtraAttack(
            final Simulation simulation,
            final boolean isTypeChain,
            final CommandCardType firstCardType
    ) {
        simulation.setAttacker(this);
        simulation.setCurrentCommandCard(extraCommandCard);

        CommandCardExecution.executeCommandCard(simulation, 3, false, firstCardType, isTypeChain);

        simulation.setCurrentCommandCard(null);
        simulation.setAttacker(null);
    }

    public CommandCardType getNoblePhantasmType() {
        return noblePhantasm.getCommandCardType();
    }

    public CommandCardType getCommandCardType(final int commandCardIndex) {
        return commandCards.get(commandCardIndex).getCommandCardType();
    }

    @Override
    public List<String> getAllTraits(final Simulation simulation) {
        final ImmutableList.Builder<String> allTraits = ImmutableList.builder();
        allTraits.addAll(super.getAllTraits(simulation));
        allTraits.add(SERVANT);
        return allTraits.build();
    }

    @Override
    public void changeNp(final double percentNpChange) {
        currentNp += percentNpChange;
        currentNp = RoundUtils.roundNearest(currentNp);

        final double npCap = getNpCap(noblePhantasmLevel);
        if (currentNp > npCap) {
            currentNp = npCap;
        } else if (shouldApplyNpPity(currentNp, percentNpChange)) {
            currentNp = NP_CAP_1;
        } else if (currentNp < 0) {
            currentNp = 0;
        }
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

    public static boolean shouldApplyNpPity(final double currentNp, final double percentNpChange) {
        return Math.signum(percentNpChange) > 0 && currentNp < NP_CAP_1 && currentNp > NP_GAIN_PITY_THRESHOLD;
    }
}
