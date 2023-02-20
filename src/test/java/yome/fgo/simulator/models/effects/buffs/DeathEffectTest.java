package yome.fgo.simulator.models.effects.buffs;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.LevelData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeOption;
import yome.fgo.data.proto.FgoStorageData.StageData;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.effects.GrantBuff;
import yome.fgo.simulator.models.levels.Level;
import yome.fgo.simulator.models.mysticcodes.MysticCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.FateClass.RIDER;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_ENEMIES;
import static yome.fgo.data.proto.FgoStorageData.Target.ATTACKER;
import static yome.fgo.simulator.models.SimulationTest.KAMA_ID;
import static yome.fgo.simulator.models.SimulationTest.KAMA_OPTION;
import static yome.fgo.simulator.models.combatants.CombatAction.createCommandCardAction;
import static yome.fgo.simulator.models.effects.buffs.BuffType.NP_DAMAGE_BUFF;

public class DeathEffectTest {

    @Test
    public void testDeathEffect_targetAttacker() {
        final StageData stageData1 = StageData.newBuilder()
                .addEnemyData(
                        EnemyData.newBuilder()
                                .setEnemyBaseId("食尸鬼")
                                .setEnemyCategories("食尸鬼")
                                .addHpBars(100)
                                .setCombatantDataOverride(
                                        CombatantData.newBuilder()
                                                .setFateClass(RIDER)
                                )
                )
                .addEffects(
                        EffectData.newBuilder()
                                .setType(GrantBuff.class.getSimpleName())
                                .setTarget(ALL_ENEMIES)
                                .addBuffData(
                                        BuffData.newBuilder()
                                                .setType("DeathEffect")
                                                .addSubEffects(
                                                        EffectData.newBuilder()
                                                                .setType("GrantBuff")
                                                                .setTarget(ATTACKER)
                                                                .addBuffData(
                                                                        BuffData.newBuilder()
                                                                                .setType("NpDamageBuff")
                                                                                .addValues(-10.0)
                                                                )
                                                )
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
        final Servant servant = simulation.getCurrentServants().get(0);
        final double attackModBeforeKill = servant.applyValuedBuff(simulation, NP_DAMAGE_BUFF);
        assertEquals(0, attackModBeforeKill);
        simulation.executeCombatActions(ImmutableList.of(createCommandCardAction(0, 2, false)));
        assertTrue(simulation.getAliveEnemies().isEmpty());
        final double attackModAfterKill = servant.applyValuedBuff(simulation, NP_DAMAGE_BUFF);
        assertEquals(-10.0, attackModAfterKill);
    }
}
