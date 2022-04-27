package yome.fgo.simulator.models.combatants;

import com.google.common.annotations.VisibleForTesting;
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
import yome.fgo.simulator.models.effects.buffs.CardTypeChange;
import yome.fgo.simulator.models.effects.buffs.NpCardTypeChange;
import yome.fgo.simulator.utils.RoundUtils;

import java.util.ArrayList;
import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.Traits.SERVANT;

@NoArgsConstructor
@Getter
@Setter
public class Servant extends Combatant {
    public static final double NP_CAP_1 = 1;
    public static final double NP_CAP_2 = 2;
    public static final double NP_CAP_3 = 3;
    public static final double NP_GAIN_PITY_THRESHOLD = 0.9899;

    private ServantData servantData;
    private int attack;
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

        this.servantData = servantData;

        this.ascension = enemyData.getServantAscension();
        final ServantAscensionData servantAscensionData = servantData.getServantAscensionData(this.ascension - 1);

        this.appendSkills = ImmutableList.of();
        this.passiveSkills = new ArrayList<>();
        for (final PassiveSkillData passiveSkillData : servantAscensionData.getPassiveSkillDataList()) {
            this.passiveSkills.add(new PassiveSkill(passiveSkillData));
        }
    }

    public Servant(final String id, final ServantData servantData, final ServantOption servantOption) {
        super(id, getLastCombatantData(servantData, servantOption.getAscension()));
        this.servantData = servantData;
        this.servantLevel = servantOption.getServantLevel();
        this.ascension = servantOption.getAscension();

        final Status servantStatus = servantData.getServantAscensionData(this.ascension - 1)
                .getServantStatusData(this.servantLevel - 1);
        this.attack = servantStatus.getATK();
        this.hpBars = new ArrayList<>(ImmutableList.of(servantStatus.getHP()));
        this.attackStatusUp = servantOption.getAttackStatusUp();
        this.hpStatusUp = servantOption.getHealthStatusUp();
        this.noblePhantasmLevel = servantOption.getNoblePhantasmLevel();
        this.bond = servantOption.getBond();

        final ServantAscensionData servantAscensionData = servantData.getServantAscensionData(this.ascension - 1);
        final NoblePhantasmData noblePhantasmData = servantAscensionData.getNoblePhantasmUpgrades()
                .getNoblePhantasmData(servantOption.getNoblePhantasmRank() - 1);
        this.noblePhantasm = new NoblePhantasm(noblePhantasmData, this.noblePhantasmLevel);

        this.activeSkills = new ArrayList<>();
        for (int i = 0; i < servantAscensionData.getActiveSkillUpgradesCount(); i++) {
            final ActiveSkillData activeSkillData = servantAscensionData.getActiveSkillUpgrades(i)
                    .getActiveSkillData(servantOption.getActiveSkillRanks(i) - 1);
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

        this.currentHp = getMaxHp();
    }

    public void equipCraftEssence(final CraftEssence craftEssence) {
        this.craftEssence = craftEssence;
    }

    @Override
    public void initiate(final Simulation simulation) {
        simulation.setActivator(this);

        for (final PassiveSkill passiveSkill : passiveSkills) {
            passiveSkill.activate(simulation);
        }
        for (final AppendSkill appendSkill : appendSkills) {
            appendSkill.activate(simulation);
        }

        if (craftEssence != null) {
            craftEssence.activate(simulation);
        }

        simulation.setActivator(null);
    }

    public int getAttack() {
        final int craftEssenceAtk = craftEssence == null ? 0 : craftEssence.getAttack();
        return attack + craftEssenceAtk + attackStatusUp;
    }

    public void activateActiveSkill(final Simulation simulation, final int activeSkillIndex) {
        simulation.setActivator(this);

        activeSkills.get(activeSkillIndex).activate(simulation);

        simulation.setActivator(null);
    }

    public void activateNoblePhantasm(final Simulation simulation, final int extraOvercharge) {
        simulation.setActivator(this);

        final NpCardTypeChange cardTypeChange = hasNpCardTypeChangeBuff(simulation);
        if (cardTypeChange != null) {
            simulation.setCurrentCommandCard(
                    new NoblePhantasm(
                            cardTypeChange.getCommandCardType(),
                            noblePhantasm.getHitPercentages(),
                            noblePhantasm.getNpCharge(),
                            noblePhantasm.getCriticalStarGeneration(),
                            noblePhantasm.getEffects(),
                            noblePhantasm.getNoblePhantasmType()
                    )
            );
        } else {
            simulation.setCurrentCommandCard(noblePhantasm);
        }

        // TODO: missing overchargeIncreaseBuff
        final int overchargeLevel = calculateOverchargeLevel(extraOvercharge, currentNp);
        currentNp = 0;
        noblePhantasm.activate(simulation, overchargeLevel);

        simulation.setCurrentCommandCard(null);
        simulation.setActivator(null);
    }

    @VisibleForTesting
    static int calculateOverchargeLevel(final int extraOvercharge, double currentNp) {
        return Math.min(extraOvercharge + Math.max(1, (int) currentNp), 5);
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
        simulation.setDefender(simulation.getTargetedEnemy());
        simulation.setCurrentCommandCard(getCommandCard(simulation, commandCardIndex));

        CommandCardExecution.executeCommandCard(simulation, chainIndex, isCriticalStrike, firstCardType, isTypeChain);

        simulation.setCurrentCommandCard(null);
        simulation.setDefender(null);
        simulation.setAttacker(null);
    }

    public void activateExtraAttack(
            final Simulation simulation,
            final boolean isTypeChain,
            final CommandCardType firstCardType
    ) {
        simulation.setAttacker(this);
        simulation.setDefender(simulation.getTargetedEnemy());
        simulation.setCurrentCommandCard(extraCommandCard);

        CommandCardExecution.executeCommandCard(simulation, 3, false, firstCardType, isTypeChain);

        simulation.setCurrentCommandCard(null);
        simulation.setDefender(null);
        simulation.setAttacker(null);
    }

    public CommandCardType getNoblePhantasmCardType(final Simulation simulation) {
        final NpCardTypeChange cardTypeChange = hasNpCardTypeChangeBuff(simulation);
        if (cardTypeChange != null) {
            return cardTypeChange.getCommandCardType();
        } else {
            return noblePhantasm.getCommandCardType();
        }
    }

    public CommandCardType getOriginalNoblePhantasmCardType() {
        return noblePhantasm.getCommandCardType();
    }

    public CommandCardType getCommandCardType(final Simulation simulation, final int commandCardIndex) {
        return getCommandCard(simulation, commandCardIndex).getCommandCardType();
    }

    public CommandCard getCommandCard(final Simulation simulation, final int index) {
        final CardTypeChange cardTypeChange = hasCardTypeChangeBuff(simulation);
        if (cardTypeChange != null) {
            final CommandCardType cardTypeOfChangedType = cardTypeChange.getCommandCardType();

            CommandCardData cardDataOfChangedType = null;
            for (final CommandCard commandCard : commandCards) {
                if (commandCard.getCommandCardType() == cardTypeOfChangedType) {
                    cardDataOfChangedType = commandCard.getCommandCardData();
                    break;
                }
            }
            assert cardDataOfChangedType != null;

            final CommandCard supposedCard = commandCards.get(index);
            return new CommandCard(cardDataOfChangedType, supposedCard.getCommandCode(), supposedCard.getCommandCardStrengthen());
        } else {
            return commandCards.get(index);
        }
    }

    @Override
    public List<String> getAllTraits(final Simulation simulation) {
        final ImmutableList.Builder<String> allTraits = ImmutableList.builder();
        allTraits.addAll(super.getAllTraits(simulation));
        allTraits.add(SERVANT.name());
        return allTraits.build();
    }

    @Override
    public void endOfTurn(final Simulation simulation) {
        super.endOfTurn(simulation);

        for (final ActiveSkill activeSkill : activeSkills) {
            activeSkill.decreaseCoolDown(1);
        }
    }

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

    @Override
    public void decreaseActiveSkillsCoolDown(final int decrease) {
        for (final ActiveSkill activeSkill : activeSkills) {
            activeSkill.decreaseCoolDown(decrease);
        }
    }
}
