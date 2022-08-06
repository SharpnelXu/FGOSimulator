package yome.fgo.simulator.models.effects.buffs;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.LevelData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeOption;
import yome.fgo.data.proto.FgoStorageData.StageData;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.levels.Level;
import yome.fgo.simulator.models.mysticcodes.MysticCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.data.proto.FgoStorageData.FateClass.RIDER;
import static yome.fgo.simulator.models.SimulationTest.KAMA_ID;
import static yome.fgo.simulator.models.SimulationTest.KAMA_OPTION;
import static yome.fgo.simulator.models.combatants.CombatAction.createCommandCardAction;

public class DamageReflectTest {
    @Test
    public void testDamageReflect() {
        final StageData stageData1 = StageData.newBuilder()
                .addEnemyData(
                        EnemyData.newBuilder()
                                .setEnemyBaseId("食尸鬼")
                                .setEnemyCategories("食尸鬼")
                                .addHpBars(100)
                                .addHpBars(100)
                                .setCombatantDataOverride(
                                        CombatantData.newBuilder()
                                                .setFateClass(RIDER)
                                )
                )
                .build();
        final LevelData levelData = LevelData.newBuilder()
                .setId("test")
                .addStageData(stageData1)
                .build();
        final Level level = new Level(levelData);
        final Servant kama = new Servant(KAMA_ID, ResourceManager.getServantData(KAMA_ID), KAMA_OPTION);

        final Simulation simulation = new Simulation(
                level,
                ImmutableList.of(kama),
                new MysticCode(
                        MysticCodeData.newBuilder().build(),
                        MysticCodeOption.newBuilder().build()
                )
        );
        simulation.initiate();
        final Combatant enemy = simulation.getCurrentEnemies().get(0);

        final DamageReflect damageReflect = DamageReflect.builder()
                .buffData(BuffData.newBuilder().setType("DamageReflect").addValues(2).build())
                .buffLevel(1)
                .value(2)
                .numTimesActive(3)
                .build();
        final Stun stun = Stun.builder().numTurnsActive(3).buffData(BuffData.newBuilder().setType("Stun").setNumTurnsActive(3).build()).buffLevel(1).build();
        kama.addBuff(damageReflect);
        kama.addBuff(stun);

        assertEquals(100, enemy.getCurrentHp());
        kama.receiveDamage(15);
        simulation.executeCombatActions(ImmutableList.of(createCommandCardAction(0, 2, false)));
        assertEquals(70, enemy.getCurrentHp());
        kama.receiveDamage(15);
        simulation.executeCombatActions(ImmutableList.of(createCommandCardAction(0, 2, false)));
        assertEquals(40, enemy.getCurrentHp());
        kama.receiveDamage(20);
        simulation.executeCombatActions(ImmutableList.of(createCommandCardAction(0, 2, false)));
        assertEquals(1, enemy.getCurrentHp());
        assertEquals(12, kama.getBuffs().size());
    }
}
