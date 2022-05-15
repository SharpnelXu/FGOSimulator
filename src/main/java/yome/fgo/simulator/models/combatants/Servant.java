package yome.fgo.simulator.models.combatants;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
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
import yome.fgo.data.proto.FgoStorageData.SpecialActivationParams;
import yome.fgo.data.proto.FgoStorageData.Status;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.craftessences.CraftEssence;
import yome.fgo.simulator.models.effects.CommandCardExecution;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.CardTypeChange;
import yome.fgo.simulator.models.effects.buffs.NpCardTypeChange;
import yome.fgo.simulator.models.effects.buffs.OverchargeBuff;
import yome.fgo.simulator.models.effects.buffs.SkillRankUp;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.data.proto.FgoStorageData.Target.ALL_CHARACTERS_INCLUDING_BACKUP;
import static yome.fgo.data.proto.FgoStorageData.Traits.SERVANT;

@Getter
@Setter
public class Servant extends Combatant {
    public static final double NP_CAP_1 = 1;
    public static final double NP_CAP_2 = 2;
    public static final double NP_CAP_3 = 3;
    public static final double NP_GAIN_PITY_THRESHOLD = 0.9899;

    private ServantData servantData;
    private ServantOption servantOption;
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
    public Servant() {
        this.isAlly = true;
    }

    public Servant(final boolean isAlly) {
        this.isAlly = isAlly;
    }

    // for testing
    public Servant(final String id) {
        super(id);
        this.isAlly = true;
    }

    // for testing
    public Servant(final String id, final CombatantData combatantData) {
        super(id, combatantData);
    }

    private static ServantAscensionData getAscensionOrLastData(final ServantData servantData, final int ascension) {
        final int index = servantData.getServantAscensionDataCount() < ascension ?
                servantData.getServantAscensionDataCount() - 1 : ascension - 1;
        return servantData.getServantAscensionData(index);
    }

    // when as enemy
    public Servant(final ServantData servantData, final EnemyData enemyData) {
        super(getAscensionOrLastData(servantData, enemyData.getServantAscension()).getCombatantData(), enemyData);

        this.servantData = servantData;

        this.ascension = enemyData.getServantAscension();
        final ServantAscensionData servantAscensionData = servantData.getServantAscensionData(this.ascension - 1);

        this.appendSkills = ImmutableList.of();
        this.passiveSkills = new ArrayList<>();
        for (final PassiveSkillData passiveSkillData : servantAscensionData.getPassiveSkillDataList()) {
            this.passiveSkills.add(new PassiveSkill(passiveSkillData));
        }
    }

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

        buildAscension(this.ascension);

        this.currentHp = getMaxHp();
        this.isAlly = true;
    }

    public void buildAscension(final int ascension) {
        final ServantAscensionData servantAscensionData = getAscensionOrLastData(servantData, ascension);
        combatantData = servantAscensionData.getCombatantData();

        final Status servantStatus = servantAscensionData.getServantStatusData(servantLevel - 1);
        attack = servantStatus.getATK();
        hpBars = new ArrayList<>(ImmutableList.of(servantStatus.getHP()));

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
            final int appendSkillLevel = servantOption.getAppendSkillLevels(i);
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

    public void changeAscension(final Simulation simulation, final int newAscension) {
        for (final Combatant clearBuff : TargetUtils.getTargets(simulation, ALL_CHARACTERS_INCLUDING_BACKUP)) {
            clearBuff.clearPassiveBuff(this);
        }

        final List<Integer> coolDowns = activeSkills.stream()
                .map(ActiveSkill::getCurrentCoolDown)
                .collect(Collectors.toList());

        ascension = newAscension;
        buildAscension(newAscension);

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

    public void equipCraftEssence(final CraftEssence craftEssence) {
        this.craftEssence = craftEssence;
    }

    @Override
    public void initiate(final Simulation simulation) {
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

    public int getAttack() {
        final int craftEssenceAtk = craftEssence == null ? 0 : craftEssence.getAttack();
        return attack + craftEssenceAtk + attackStatusUp;
    }

    public int getCurrentSkillRank(final Simulation simulation, final int activeSkillIndex) {
        final int currentRank = servantOption.getActiveSkillRanks(activeSkillIndex);
        int increasedRank = 0;
        for (final Buff buff : buffs) {
            if (buff instanceof SkillRankUp && buff.shouldApply(simulation)) {
                increasedRank += 1;
            }
        }
        return currentRank + increasedRank;
    }

    public void activateActiveSkill(final Simulation simulation, final int activeSkillIndex) {
        simulation.setActivator(this);

        final int currentRank = getCurrentSkillRank(simulation, activeSkillIndex);

        activeSkills.get(activeSkillIndex).activate(simulation, currentRank);

        simulation.unsetActivator();
    }

    public String getActiveSkillIconPath(final Simulation simulation, final int activeSkillIndex) {
        simulation.setActivator(this);

        final int currentRank = getCurrentSkillRank(simulation, activeSkillIndex);

        final String iconPath = activeSkills.get(activeSkillIndex).getIconPath(currentRank);

        simulation.unsetActivator();

        return iconPath;
    }

    public SpecialActivationParams getActiveSkillSpecialTarget(final Simulation simulation, final int activeSkillIndex) {
        simulation.setActivator(this);

        final int currentRank = getCurrentSkillRank(simulation, activeSkillIndex);

        final SpecialActivationParams specialActivationParams =
                activeSkills.get(activeSkillIndex).getSpecialActivationParams(currentRank);

        simulation.unsetActivator();

        return specialActivationParams;
    }

    public boolean canActivateActiveSkill(final Simulation simulation, final int activeSkillIndex) {
        simulation.setActivator(this);

        final int currentRank = getCurrentSkillRank(simulation, activeSkillIndex);

        final boolean canActivate = !isSkillInaccessible() &&
                activeSkills.get(activeSkillIndex).canActivate(simulation, currentRank);

        simulation.unsetActivator();

        return canActivate;
    }

    public boolean canActivateNoblePhantasm(final Simulation simulation) {
        simulation.setActivator(this);

        final boolean canActivate = !isNpInaccessible() && noblePhantasm.canActivate(simulation);

        simulation.unsetActivator();

        return canActivate;
    }

    public void activateNoblePhantasm(final Simulation simulation, final int extraOvercharge) {
        simulation.setActivator(this);
        simulation.setCriticalStrike(false);

        final int overchargeLevel = calculateOverchargeLevel(simulation, extraOvercharge);
        currentNp = 0;
        noblePhantasm.activate(simulation, overchargeLevel);

        simulation.unsetActivator();
    }

    @VisibleForTesting
    int calculateOverchargeLevel(final Simulation simulation, final int extraOvercharge) {
        int overchargeBuff = 0;

        for (final Buff buff : buffs) {
            if (buff instanceof OverchargeBuff && buff.shouldApply(simulation)) {
                overchargeBuff += ((OverchargeBuff) buff).getValue();
            }
        }

        final int calculatedOvercharge = overchargeBuff + extraOvercharge + (int) currentNp;
        if (calculatedOvercharge > 5) {
            return 5;
        } if (calculatedOvercharge < 1) {
            return 1;
        } else {
            return calculatedOvercharge;
        }
    }

    public void activateCommandCard(
            final Simulation simulation,
            final int commandCardIndex,
            final int chainIndex,
            final boolean isCriticalStrike,
            final boolean isTypeChain,
            final CommandCardType firstCardType
    ) {
        simulation.setActivator(this);
        simulation.setAttacker(this);
        simulation.setDefender(simulation.getTargetedEnemy());
        simulation.setCurrentCommandCard(getCommandCard(simulation, commandCardIndex));
        simulation.setCriticalStrike(isCriticalStrike);

        CommandCardExecution.executeCommandCard(simulation, chainIndex, isCriticalStrike, firstCardType, isTypeChain);

        simulation.setCriticalStrike(false);
        simulation.unsetCurrentCommandCard();
        simulation.unsetDefender();
        simulation.unsetAttacker();
        simulation.unsetActivator();
    }

    public void activateExtraAttack(
            final Simulation simulation,
            final boolean isTypeChain,
            final CommandCardType firstCardType
    ) {
        simulation.setActivator(this);
        simulation.setAttacker(this);
        simulation.setDefender(simulation.getTargetedEnemy());
        simulation.setCurrentCommandCard(extraCommandCard);
        simulation.setCriticalStrike(false);

        CommandCardExecution.executeCommandCard(simulation, 3, false, firstCardType, isTypeChain);

        simulation.unsetCurrentCommandCard();
        simulation.unsetDefender();
        simulation.unsetAttacker();
        simulation.unsetActivator();
    }

    public CommandCardType getNoblePhantasmCardType(final Simulation simulation) {
        final NpCardTypeChange cardTypeChange = hasNpCardTypeChangeBuff(simulation);
        if (cardTypeChange != null) {
            return cardTypeChange.getCommandCardType();
        } else {
            return noblePhantasm.getCommandCardType();
        }
    }

    public NoblePhantasm getNoblePhantasm(final Simulation simulation) {
        final NpCardTypeChange cardTypeChange = hasNpCardTypeChangeBuff(simulation);
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

    public CommandCardType getCommandCardType(final Simulation simulation, final int commandCardIndex) {
        final CardTypeChange cardTypeChange = hasCardTypeChangeBuff(simulation);
        if (cardTypeChange != null) {
            return cardTypeChange.getCommandCardType();
        } else {
            return commandCards.get(commandCardIndex).getCommandCardType();
        }
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
            return new CommandCard(cardDataOfChangedType, supposedCard.getCommandCodeBuffs(), supposedCard.getCommandCardStrengthen());
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
    public void endOfMyTurn(final Simulation simulation) {
        super.endOfMyTurn(simulation);

        if (!isSkillInaccessible()) {
            for (final ActiveSkill activeSkill : activeSkills) {
                activeSkill.decreaseCoolDown(1);
            }
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
