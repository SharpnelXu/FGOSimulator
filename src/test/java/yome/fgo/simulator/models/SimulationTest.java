package yome.fgo.simulator.models;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.CommandCardOption;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceOption;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.LevelData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeOption;
import yome.fgo.data.proto.FgoStorageData.ServantOption;
import yome.fgo.data.proto.FgoStorageData.StageData;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.models.combatants.CombatAction;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.CommandCard;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.craftessences.CraftEssence;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.levels.Level;
import yome.fgo.simulator.models.mysticcodes.MysticCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.IMMOBILIZE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.UNRECOGNIZED;
import static yome.fgo.data.proto.FgoStorageData.FateClass.CASTER;
import static yome.fgo.data.proto.FgoStorageData.Traits.DIVINE;
import static yome.fgo.simulator.models.Simulation.getNextNonNullTargetIndex;
import static yome.fgo.simulator.models.Simulation.isBraveChain;
import static yome.fgo.simulator.models.Simulation.shouldRemoveDeadCombatants;
import static yome.fgo.simulator.models.combatants.CombatAction.createCommandCardAction;
import static yome.fgo.simulator.models.combatants.CombatAction.createNoblePhantasmAction;
import static yome.fgo.simulator.models.effects.buffs.BuffType.PERMANENT_SLEEP;
import static yome.fgo.simulator.models.effects.buffs.BuffType.STUN;
import static yome.fgo.simulator.models.levels.LevelTest.TEST_LEVEL_NAME;
import static yome.fgo.simulator.models.levels.LevelTest.TEST_LEVEL_PATH;

public class SimulationTest {
    public static final List<CombatAction> COMMAND_CARD_0_1_0 = ImmutableList.of(
            createCommandCardAction(0, 0, false),
            createCommandCardAction(1, 0, false),
            createCommandCardAction(0, 0, false)
    );
    public static final List<CombatAction> COMMAND_CARD_0_0_0 = ImmutableList.of(
            createCommandCardAction(0, 0, false),
            createCommandCardAction(0, 0, false),
            createCommandCardAction(0, 0, false)
    );
    public static final List<CombatAction> COMMAND_CARD_0_0 = ImmutableList.of(
            createCommandCardAction(0, 0, false),
            createCommandCardAction(0, 0, false)
    );
    public static final ServantOption KAMA_OPTION = ServantOption.newBuilder()
            .setServantLevel(120)
            .setNoblePhantasmRank(1)
            .setNoblePhantasmLevel(5)
            .setAttackStatusUp(2000)
            .setHealthStatusUp(2000)
            .addAllActiveSkillRanks(ImmutableList.of(1, 1, 1))
            .addAllActiveSkillLevels(ImmutableList.of(10, 10, 10))
            .addAllAppendSkillLevels(ImmutableList.of(10, 10, 10))
            .addCommandCardOptions(CommandCardOption.newBuilder().setStrengthen(500))
            .addCommandCardOptions(CommandCardOption.newBuilder())
            .addCommandCardOptions(CommandCardOption.newBuilder())
            .addCommandCardOptions(CommandCardOption.newBuilder())
            .addCommandCardOptions(CommandCardOption.newBuilder())
            .setBond(15)
            .setAscension(1)
            .build();
    public static final String KAMA_ID = "servant321";

    public static final CraftEssenceOption CE_OPTION = CraftEssenceOption.newBuilder()
            .setCraftEssenceLevel(100)
            .setIsLimitBreak(true)
            .build();

    public static final String ALTRIA_ID = "servant284";
    public static final ServantOption ALTRIA_OPTION = ServantOption.newBuilder()
            .setServantLevel(90)
            .setNoblePhantasmRank(1)
            .setNoblePhantasmLevel(5)
            .setAttackStatusUp(1000)
            .setHealthStatusUp(1000)
            .addAllActiveSkillRanks(ImmutableList.of(1, 1, 1))
            .addAllActiveSkillLevels(ImmutableList.of(10, 10, 10))
            .addAllAppendSkillLevels(ImmutableList.of(10, 0, 10))
            .addCommandCardOptions(CommandCardOption.newBuilder())
            .addCommandCardOptions(CommandCardOption.newBuilder())
            .addCommandCardOptions(CommandCardOption.newBuilder())
            .addCommandCardOptions(CommandCardOption.newBuilder())
            .addCommandCardOptions(CommandCardOption.newBuilder())
            .setBond(10)
            .setAscension(1)
            .build();

    @Test
    public void testSimpleKamaLoop() {
        final Level level = new Level(ResourceManager.getLevelData(TEST_LEVEL_PATH, TEST_LEVEL_NAME));
        final List<Combatant> stage1Enemies = new ArrayList<>(level.getStage(1).getEnemies());
        final List<Combatant> stage2Enemies = new ArrayList<>(level.getStage(2).getEnemies());
        final List<Combatant> stage3Enemies = new ArrayList<>(level.getStage(3).getEnemies());
        final Servant kama = new Servant(KAMA_ID, ResourceManager.getServantData(KAMA_ID), KAMA_OPTION);
        final CraftEssence ce = new CraftEssence(ResourceManager.getCraftEssenceData("craftEssence1080"), CE_OPTION);
        kama.equipCraftEssence(ce);
        final Servant altria1 = new Servant(ALTRIA_ID, ResourceManager.getServantData(ALTRIA_ID), ALTRIA_OPTION);
        final Servant altria2 = new Servant(ALTRIA_ID, ResourceManager.getServantData(ALTRIA_ID), ALTRIA_OPTION);

        final Simulation simNp3TClear = new Simulation(
                level,
                ImmutableList.of(kama, altria1, altria2),
                new MysticCode(MysticCodeData.newBuilder().build(), MysticCodeOption.newBuilder().build())
        );
        simNp3TClear.initiate();
        simNp3TClear.setFixedRandom(0.9);

        assertEquals(12, kama.getBuffs().size());
        assertEquals(6, altria1.getBuffs().size());
        assertEquals(6, altria2.getBuffs().size());

        simNp3TClear.setCurrentAllyTargetIndex(0);
        simNp3TClear.activateServantSkill(0, 0);
        simNp3TClear.activateServantSkill(1, 0);
        simNp3TClear.activateServantSkill(1, 1);
        simNp3TClear.activateServantSkill(1, 2);
        simNp3TClear.activateServantSkill(2, 0);
        simNp3TClear.activateServantSkill(2, 1);
        simNp3TClear.activateServantSkill(2, 2);
        assertEquals(6, kama.getActiveSkills().get(0).getCurrentCoolDown());
        assertEquals(6, altria1.getActiveSkills().get(0).getCurrentCoolDown());
        assertEquals(5, altria1.getActiveSkills().get(1).getCurrentCoolDown());
        assertEquals(6, altria1.getActiveSkills().get(2).getCurrentCoolDown());
        assertEquals(6, altria2.getActiveSkills().get(0).getCurrentCoolDown());
        assertEquals(5, altria2.getActiveSkills().get(1).getCurrentCoolDown());
        assertEquals(6, altria2.getActiveSkills().get(2).getCurrentCoolDown());

        assertEquals(1.20, kama.getCurrentNp());

        final CombatAction kamaNp = createNoblePhantasmAction(0);
        simNp3TClear.executeCombatActions(ImmutableList.of(kamaNp));
        assertEquals(4.32, simNp3TClear.getCurrentStars(), 0.02);

        assertEquals(1.0472, kama.getCurrentNp());
        for (final Combatant combatant : stage1Enemies) {
            assertEquals(
                    127111,
                    combatant.getHpBars().get(combatant.getCurrentHpBarIndex()) - combatant.getCurrentHp(),
                    5
            );
        }

        simNp3TClear.executeCombatActions(ImmutableList.of(kamaNp));
        assertEquals(3.72, simNp3TClear.getCurrentStars(), 0.02);

        assertEquals(0.9842, kama.getCurrentNp());
        for (final Combatant combatant : stage2Enemies) {
            assertEquals(
                    127111,
                    combatant.getHpBars().get(combatant.getCurrentHpBarIndex()) - combatant.getCurrentHp(),
                    5
            );
        }

        assertFalse(kama.canActivateActiveSkill(simNp3TClear, 0));
        assertFalse(altria1.canActivateActiveSkill(simNp3TClear, 1));
        assertFalse(altria2.canActivateActiveSkill(simNp3TClear, 2));
        assertTrue(kama.canActivateActiveSkill(simNp3TClear, 1));

        simNp3TClear.activateServantSkill(0, 1);
        simNp3TClear.activateServantSkill(0, 2);

        final CombatAction kamaArts1 = createCommandCardAction(0, 2, false);
        final CombatAction kamaArts2 = createCommandCardAction(0, 3, true);
        simNp3TClear.executeCombatActions(ImmutableList.of(kamaArts1, kamaArts2, kamaNp));
        assertEquals(5.18, simNp3TClear.getCurrentStars(), 0.02);

        assertEquals(0.9211, kama.getCurrentNp());
        final Combatant stage3first = stage3Enemies.get(0);
        assertEquals(
                288890 + 48948 + 17142,
                stage3first.getHpBars().get(stage3first.getCurrentHpBarIndex()) - stage3first.getCurrentHp(),
                5
        );
        final Combatant stage3second = stage3Enemies.get(1);
        assertEquals(
                247652,
                stage3second.getHpBars().get(stage3second.getCurrentHpBarIndex()) - stage3second.getCurrentHp(),
                5
        );
        final Combatant stage3third = stage3Enemies.get(2);
        assertEquals(
                450096,
                stage3third.getHpBars().get(stage3third.getCurrentHpBarIndex()) - stage3third.getCurrentHp(),
                5
        );

        assertTrue(simNp3TClear.isSimulationCompleted());
        assertEquals(12 + 3, kama.getBuffs().size()); // skill 2 & 3 activated at turn 3
        assertEquals(3, kama.getActiveSkills().get(0).getCurrentCoolDown());
        assertEquals(6, kama.getActiveSkills().get(1).getCurrentCoolDown());
        assertEquals(5, kama.getActiveSkills().get(2).getCurrentCoolDown());
        assertEquals(3, altria1.getActiveSkills().get(0).getCurrentCoolDown());
        assertEquals(2, altria1.getActiveSkills().get(1).getCurrentCoolDown());
        assertEquals(3, altria1.getActiveSkills().get(2).getCurrentCoolDown());
        assertEquals(3, altria2.getActiveSkills().get(0).getCurrentCoolDown());
        assertEquals(2, altria2.getActiveSkills().get(1).getCurrentCoolDown());
        assertEquals(3, altria2.getActiveSkills().get(2).getCurrentCoolDown());
    }

    @Test
    public void testOverchargeCarryOverInStun() {
        ResourceManager.rebuildDataMap();
        final Level level = new Level(ResourceManager.getLevelData("", "test"));
        final List<Combatant> stage1Enemies = new ArrayList<>(level.getStage(1).getEnemies());
        final Servant kama = new Servant(KAMA_ID, ResourceManager.getServantData(KAMA_ID), KAMA_OPTION);
        final CraftEssence ce = new CraftEssence(ResourceManager.getCraftEssenceData("craftEssence34"), CE_OPTION);
        kama.equipCraftEssence(ce);
        final Servant altria1 = new Servant(ALTRIA_ID, ResourceManager.getServantData(ALTRIA_ID), ALTRIA_OPTION);
        final Servant altria2 = new Servant(ALTRIA_ID, ResourceManager.getServantData(ALTRIA_ID), ALTRIA_OPTION);

        final Simulation simNp3TClear = new Simulation(
                level,
                ImmutableList.of(kama, altria1, altria2),
                new MysticCode(MysticCodeData.newBuilder().build(), MysticCodeOption.newBuilder().build())
        );
        simNp3TClear.initiate();
        simNp3TClear.setFixedRandom(0.9);

        altria1.addBuff(Buff.builder().buffType(STUN).buffTraits(Collections.singletonList(IMMOBILIZE_BUFF.name())).build());
        simNp3TClear.activateServantSkill(0, 1);

        final ImmutableList<CombatAction> npStunNp = ImmutableList.of(
                createNoblePhantasmAction(2),
                createCommandCardAction(1, 0, false),
                createNoblePhantasmAction(0)
        );
        simNp3TClear.executeCombatActions(npStunNp);
        assertEquals(2905470, stage1Enemies.get(1).getCurrentHp(), 5);
    }

    @Test
    public void testBuggedOverkill() {
        final Level level = new Level(ResourceManager.getLevelData(TEST_LEVEL_PATH, TEST_LEVEL_NAME));
        final Servant kama = new Servant(KAMA_ID, ResourceManager.getServantData(KAMA_ID), KAMA_OPTION);
        final CraftEssence ce = new CraftEssence(ResourceManager.getCraftEssenceData("craftEssence1080"), CE_OPTION);
        kama.equipCraftEssence(ce);

        final Simulation simNp3TClear = new Simulation(
                level,
                ImmutableList.of(kama),
                new MysticCode(MysticCodeData.newBuilder().build(), MysticCodeOption.newBuilder().build())
        );
        simNp3TClear.initiate();
        simNp3TClear.setFixedRandom(0.9);
        simNp3TClear.setCurrentAllyTargetIndex(0);

        assertEquals(0.20, kama.getCurrentNp());

        final CombatAction kamaQuick = createCommandCardAction(0, 0, true);
        final CombatAction kamaArts = createCommandCardAction(0, 2, true);
        final CombatAction kamaBuster = createCommandCardAction(0, 4, true);
        simNp3TClear.executeCombatActions(ImmutableList.of(kamaBuster, kamaArts, kamaQuick));

        assertEquals(0.8817, kama.getCurrentNp());
    }

    @Test
    public void testBuggedOverkill_scathach() {
        final Level level = new Level(
                LevelData.newBuilder()
                        .addStageData(
                                StageData.newBuilder()
                                        .setMaximumEnemiesOnScreen(3)
                                        .addEnemyData(
                                                EnemyData.newBuilder()
                                                        .setEnemyBaseId("冰雪咒书")
                                                        .setEnemyCategories("书")
                                                        .addAllHpBars(ImmutableList.of(154248, 270540, 321433))
                                                        .setCombatantDataOverride(
                                                                CombatantData.newBuilder()
                                                                        .setFateClass(CASTER)
                                                                        .addTraits(DIVINE.name())
                                                        )
                                        )
                        )
                        .build());
        final Servant scathach = new Servant(
                "servant70",
                ResourceManager.getServantData("servant70"),
                ServantOption.newBuilder()
                        .setServantLevel(90)
                        .setNoblePhantasmRank(2)
                        .setNoblePhantasmLevel(2)
                        .setAttackStatusUp(1000)
                        .setHealthStatusUp(1000)
                        .addAllActiveSkillRanks(ImmutableList.of(1, 2, 1))
                        .addAllActiveSkillLevels(ImmutableList.of(10, 10, 10))
                        .addAllAppendSkillLevels(ImmutableList.of(0, 0, 0))
                        .addCommandCardOptions(CommandCardOption.newBuilder())
                        .addCommandCardOptions(CommandCardOption.newBuilder())
                        .addCommandCardOptions(CommandCardOption.newBuilder())
                        .addCommandCardOptions(CommandCardOption.newBuilder())
                        .addCommandCardOptions(CommandCardOption.newBuilder())
                        .setBond(15)
                        .setAscension(2)
                        .build()
        );
        final CraftEssence ce = new CraftEssence(ResourceManager.getCraftEssenceData("craftEssence309"), CE_OPTION);
        scathach.equipCraftEssence(ce);

        final Simulation simulation = new Simulation(
                level,
                ImmutableList.of(scathach),
                new MysticCode(MysticCodeData.newBuilder().build(), MysticCodeOption.newBuilder().build())
        );
        simulation.initiate();
        simulation.setFixedRandom(0.92);
        simulation.setProbabilityThreshold(1);
        simulation.setCurrentAllyTargetIndex(0);

        final CombatAction np = createNoblePhantasmAction(0);
        final CombatAction buster = createCommandCardAction(0, 4, false);
        final CombatAction busterCrit = createCommandCardAction(0, 4, true);
        final CombatAction artsCrit = createCommandCardAction(0, 2, true);

        simulation.activateServantSkill(0, 1);
        simulation.executeCombatActions(ImmutableList.of(np, busterCrit, artsCrit));
        assertEquals(1.1449, scathach.getCurrentNp(), 0.005);

        simulation.executeCombatActions(ImmutableList.of(np, busterCrit, artsCrit));
        assertEquals(1.1449, scathach.getCurrentNp(), 0.005);

        simulation.setFixedRandom(0.95);
        simulation.activateServantSkill(0, 2);
        simulation.executeCombatActions(ImmutableList.of(np, busterCrit, artsCrit));
        assertEquals(1.0749, scathach.getCurrentNp(), 0.005);

        simulation.setFixedRandom(1.1);
        simulation.executeCombatActions(ImmutableList.of(np, buster, artsCrit));
        assertEquals(1.0349, scathach.getCurrentNp(), 0.005);

        simulation.setFixedRandom(1.0);
        simulation.executeCombatActions(ImmutableList.of(np, buster, artsCrit));
        assertEquals(0.6949, scathach.getCurrentNp(), 0.005);

        simulation.activateServantSkill(0, 1);
        simulation.activateServantSkill(0, 2);
        simulation.executeCombatActions(ImmutableList.of(np, busterCrit, artsCrit));
        assertEquals(1.1449, scathach.getCurrentNp(), 0.005);
    }

    @Test
    public void testGetFirstCardType() {
        final Servant busterServant = new Servant("buster");
        busterServant.setCommandCards(ImmutableList.of(new CommandCard(BUSTER, ImmutableList.of(), 0.5, 39)));
        final Servant artsServant = new Servant("arts");
        artsServant.setCommandCards(ImmutableList.of(new CommandCard(ARTS, ImmutableList.of(), 0.5, 39)));
        final List<Servant> servants = ImmutableList.of(busterServant, artsServant);
        final Simulation simulation = new Simulation();
        simulation.setCurrentServants(servants);

        assertEquals(BUSTER, simulation.getFirstCardType(COMMAND_CARD_0_1_0));

        busterServant.addBuff(Buff.builder().buffType(PERMANENT_SLEEP).buffTraits(Collections.singletonList(IMMOBILIZE_BUFF.name())).build());
        assertEquals(UNRECOGNIZED, simulation.getFirstCardType(COMMAND_CARD_0_1_0));
        assertEquals(UNRECOGNIZED, simulation.getFirstCardType(COMMAND_CARD_0_0));
    }

    @Test
    public void testIsTypeChain() {
        final Servant busterServant = new Servant("buster");
        busterServant.setCommandCards(ImmutableList.of(new CommandCard(BUSTER, ImmutableList.of(), 0.5, 39)));
        final Servant artsServant = new Servant("arts");
        artsServant.setCommandCards(ImmutableList.of(new CommandCard(ARTS, ImmutableList.of(), 0.5, 39)));
        final Servant artsServant2 = new Servant("arts2");
        artsServant2.setCommandCards(ImmutableList.of(new CommandCard(ARTS, ImmutableList.of(), 0.5, 39)));
        final List<Servant> servants = ImmutableList.of(busterServant, artsServant, artsServant2);
        final Simulation simulation = new Simulation();
        simulation.setCurrentServants(servants);

        assertFalse(simulation.isTypeChain(COMMAND_CARD_0_1_0));
        assertTrue(simulation.isTypeChain(COMMAND_CARD_0_0_0));
        assertFalse(simulation.isTypeChain(COMMAND_CARD_0_0));
        final ImmutableList<CombatAction> arts = ImmutableList.of(
                createCommandCardAction(1, 0, false),
                createCommandCardAction(2, 0, false),
                createCommandCardAction(1, 0, false)
        );
        assertTrue(simulation.isTypeChain(arts));

        artsServant2.addBuff(Buff.builder().buffType(PERMANENT_SLEEP).buffTraits(Collections.singletonList(IMMOBILIZE_BUFF.name())).build());
        assertFalse(simulation.isTypeChain(arts));
    }

    @Test
    public void testIsTriColorChain() {
        final Servant busterServant = new Servant("buster");
        busterServant.setCommandCards(ImmutableList.of(new CommandCard(BUSTER, ImmutableList.of(), 0.5, 39)));
        final Servant artsServant = new Servant("arts");
        artsServant.setCommandCards(ImmutableList.of(new CommandCard(ARTS, ImmutableList.of(), 0.5, 39)));
        final Servant quickServant = new Servant("quick");
        quickServant.setCommandCards(ImmutableList.of(new CommandCard(QUICK, ImmutableList.of(), 0.5, 39)));
        final List<Servant> servants = ImmutableList.of(busterServant, artsServant, quickServant);
        final Simulation simulation = new Simulation();
        simulation.setCurrentServants(servants);

        assertFalse(simulation.isTriColorChain(COMMAND_CARD_0_1_0));

        final ImmutableList<CombatAction> qab = ImmutableList.of(
                createCommandCardAction(0, 0, false),
                createCommandCardAction(2, 0, false),
                createCommandCardAction(1, 0, false)
        );
        assertTrue(simulation.isTriColorChain(qab));

        quickServant.addBuff(Buff.builder().buffType(PERMANENT_SLEEP).buffTraits(Collections.singletonList(IMMOBILIZE_BUFF.name())).build());
        assertFalse(simulation.isTriColorChain(qab));
    }

    @Test
    public void testIsBraveChain() {
        assertFalse(isBraveChain(COMMAND_CARD_0_1_0));
        assertTrue(isBraveChain(COMMAND_CARD_0_0_0));
        assertFalse(isBraveChain(COMMAND_CARD_0_0));
    }

    @Test
    public void testShouldRemoveDeadCombatants() {
        final List<CombatAction> noblePhantasm = ImmutableList.of(createNoblePhantasmAction(0));
        assertTrue(shouldRemoveDeadCombatants(noblePhantasm, 0));

        final List<CombatAction> nextCardNoblePhantasm = ImmutableList.of(
                createCommandCardAction(0, 0, false),
                createNoblePhantasmAction(0)
        );
        assertTrue(shouldRemoveDeadCombatants(nextCardNoblePhantasm, 0));

        final List<CombatAction> nextCardDifferentServant = ImmutableList.of(
                createCommandCardAction(0, 0, false),
                createCommandCardAction(1, 0, false)
        );
        assertTrue(shouldRemoveDeadCombatants(nextCardDifferentServant, 0));

        final List<CombatAction> nextCardSameServant = ImmutableList.of(
                createCommandCardAction(0, 0, false),
                createCommandCardAction(0, 0, false)
        );
        assertFalse(shouldRemoveDeadCombatants(nextCardSameServant, 0));

        // last card no brave chain
        assertTrue(shouldRemoveDeadCombatants(COMMAND_CARD_0_0, 1));

        // last card brave chain
        assertFalse(shouldRemoveDeadCombatants(COMMAND_CARD_0_0_0, 2));
    }

    @Test
    public void testGetNextNonNullTargetIndex() {
        final List<Combatant> combatants = Lists.newArrayList(
                null,
                new Combatant(),
                new Combatant(),
                null
        );
        assertEquals(1, getNextNonNullTargetIndex(combatants, 0));
        assertEquals(1, getNextNonNullTargetIndex(combatants, 1));
        assertEquals(2, getNextNonNullTargetIndex(combatants, 2));
        assertEquals(1, getNextNonNullTargetIndex(combatants, 3));

        final List<Combatant> nullList = Lists.newArrayList(null, null);
        assertEquals(0, getNextNonNullTargetIndex(nullList, 0));
        assertEquals(0, getNextNonNullTargetIndex(nullList, 1));
    }
}
