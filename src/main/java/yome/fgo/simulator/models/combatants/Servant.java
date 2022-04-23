package yome.fgo.simulator.models.combatants;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmData;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.data.proto.FgoStorageData.ServantOption;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.craftessences.CraftEssence;
import yome.fgo.simulator.models.effects.CommandCardExecution;
import yome.fgo.simulator.utils.RoundUtils;

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
        this.attackStatusUp = servantOption.getAttackStatusUp();
        this.hpStatusUp = servantOption.getHealthStatusUp();
        this.servantLevel = servantOption.getServantLevel();
        this.noblePhantasmLevel = servantOption.getNoblePhantasmLevel();
        this.ascension = servantOption.getAscension();
        this.bond = servantOption.getBond();

        final NoblePhantasmData noblePhantasmData = servantData.getServantAscensionData(this.ascension)
                .getNoblePhantasmUpgrades()
                .getNoblePhantasmData(servantOption.getNoblePhantasmRank());
        this.noblePhantasm = new NoblePhantasm(noblePhantasmData, this.noblePhantasmLevel);
    }

    @Override
    public void initiate(final Simulation simulation) {
    }

    @Override
    public boolean hasNextHpBar() {
        return false;
    }

    public int getAttack() {
        return servantData.getServantAscensionData(ascension).getServantStatusData(servantLevel - 1).getATK();
    }

    public CommandCard getCommandCard(final int index) {
        return commandCards.get(index);
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
        simulation.setCurrentCommandCard(getCommandCard(commandCardIndex));

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
