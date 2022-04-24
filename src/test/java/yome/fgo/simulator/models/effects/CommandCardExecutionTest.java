package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import org.easymock.Capture;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.CommandCard;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.effects.CommandCardExecution.CriticalStarParameters;
import yome.fgo.simulator.models.effects.CommandCardExecution.DamageParameters;
import yome.fgo.simulator.models.effects.CommandCardExecution.NpParameters;
import yome.fgo.simulator.models.effects.buffs.CommandCardBuff;
import yome.fgo.simulator.models.effects.buffs.DamageAdditionBuff;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.captureInt;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.newCapture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.data.proto.FgoStorageData.Attribute.MAN;
import static yome.fgo.data.proto.FgoStorageData.Attribute.SKY;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.EXTRA;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.data.proto.FgoStorageData.FateClass.AVENGER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BEAST_III_R;
import static yome.fgo.data.proto.FgoStorageData.FateClass.CASTER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.LANCER;
import static yome.fgo.simulator.models.effects.CommandCardExecution.calculateCritStar;
import static yome.fgo.simulator.models.effects.CommandCardExecution.calculateNpGainPercentage;
import static yome.fgo.simulator.models.effects.CommandCardExecution.calculateTotalDamage;
import static yome.fgo.simulator.models.effects.CommandCardExecution.executeCommandCard;

public class CommandCardExecutionTest extends EasyMockSupport {
    public static final CommandCard KAMA_AVENGER_BUSTER = new CommandCard(
            CommandCardData.newBuilder()
                    .setCommandCardType(BUSTER)
                    .addAllHitsData(ImmutableList.of(16, 33, 51))
                    .setNpRate(0.52)
                    .setCriticalStarGen(6.1)
                    .build());
    public static final CommandCard KAMA_AVENGER_ARTS = new CommandCard(
            CommandCardData.newBuilder()
                    .setCommandCardType(ARTS)
                    .addAllHitsData(ImmutableList.of(16, 33, 51))
                    .setNpRate(0.52)
                    .setCriticalStarGen(6.1)
                    .build());
    public static final CommandCard KAMA_AVENGER_QUICK = new CommandCard(
            CommandCardData.newBuilder()
                     .setCommandCardType(QUICK)
                     .addAllHitsData(ImmutableList.of(10, 20, 30, 40))
                     .setNpRate(0.52)
                     .setCriticalStarGen(6.1)
                     .build());
    public static final CommandCard KAMA_AVENGER_EXTRA = new CommandCard(
            CommandCardData.newBuilder()
                     .setCommandCardType(EXTRA)
                     .addAllHitsData(ImmutableList.of(6, 13, 20, 26, 35))
                     .setNpRate(0.52)
                     .setCriticalStarGen(6.1)
                     .build());

    private Simulation simulation;
    private Servant attacker;
    private Combatant defender;

    @Test
    public void testExecuteCommandCard_kamaAvenger_busterInBAQ() {
        simulation = mock(Simulation.class);
        attacker = niceMock(Servant.class);
        defender = niceMock(Combatant.class);

        expect(simulation.getAttacker()).andReturn(attacker);
        expect(simulation.getDefender()).andReturn(defender);
        expect(defender.getFateClass()).andReturn(CASTER);
        expect(simulation.getCurrentCommandCard()).andReturn(KAMA_AVENGER_BUSTER);

        expect(attacker.applyBuff(simulation, DamageAdditionBuff.class)).andReturn(225.0);

        expect(attacker.getAttack()).andReturn(20977);
        expect(attacker.getFateClass()).andReturn(AVENGER);
        expect(attacker.getAttribute()).andReturn(SKY);
        expect(defender.getAttribute()).andReturn(SKY);
        expect(simulation.getFixedRandom()).andReturn(0.9);

        expect(defender.activateEvade(simulation)).andReturn(false);

        final List<Capture<Integer>> captures = new ArrayList<>();
        for (int i = 0; i < KAMA_AVENGER_BUSTER.getHitPercentages().size(); i++) {
            final Capture<Integer> capturedDamage = newCapture();
            defender.receiveDamage(captureInt(capturedDamage));
            captures.add(capturedDamage);
        }
        expect(defender.isAlreadyDead()).andReturn(false);
        expect(defender.isBuggedOverkill()).andReturn(false);
        expect(defender.isAlreadyDead()).andReturn(true).times(2);
        expect(defender.getUndeadNpCorrection()).andReturn(false).times(3);
        attacker.changeNp(0);
        expectLastCall().times(3);
        simulation.gainStar(1.08);
        defender.addCumulativeTurnDamage(anyInt());
        replayAll();

        executeCommandCard(simulation, 0, false, BUSTER, false);

        verifyAll();
        final int totalDamage = captures.stream().mapToInt(Capture::getValue).sum();
        assertEquals(9776, totalDamage, 10);
    }

    @Test
    public void testExecuteCommandCard_kamaAvenger_extraInBAQ() {
        simulation = mock(Simulation.class);
        attacker = niceMock(Servant.class);
        defender = niceMock(Combatant.class);

        expect(simulation.getAttacker()).andReturn(attacker);
        expect(simulation.getDefender()).andReturn(defender);
        expect(defender.getFateClass()).andReturn(CASTER);
        expect(simulation.getCurrentCommandCard()).andReturn(KAMA_AVENGER_EXTRA);

        expect(attacker.applyBuff(simulation, CommandCardBuff.class)).andReturn(0.5);
        expect(attacker.applyBuff(simulation, DamageAdditionBuff.class)).andReturn(225.0);

        expect(attacker.getAttack()).andReturn(20977);
        expect(attacker.getFateClass()).andReturn(AVENGER);
        expect(attacker.getAttribute()).andReturn(SKY);
        expect(defender.getAttribute()).andReturn(SKY);
        expect(simulation.getFixedRandom()).andReturn(0.9);

        expect(defender.activateEvade(simulation)).andReturn(false);

        final List<Capture<Integer>> captures = new ArrayList<>();
        for (int i = 0; i < KAMA_AVENGER_EXTRA.getHitPercentages().size(); i++) {
            final Capture<Integer> capturedDamage = newCapture();
            defender.receiveDamage(captureInt(capturedDamage));
            captures.add(capturedDamage);
        }
        expect(defender.isAlreadyDead()).andReturn(true).times(KAMA_AVENGER_EXTRA.getHitPercentages().size());
        expect(defender.getUndeadNpCorrection()).andReturn(false).times(KAMA_AVENGER_EXTRA.getHitPercentages().size());
        attacker.changeNp(1.39);
        expectLastCall().times(KAMA_AVENGER_EXTRA.getHitPercentages().size());
        simulation.gainStar(9.3);
        defender.addCumulativeTurnDamage(anyInt());
        replayAll();

        executeCommandCard(simulation, 3, false, BUSTER, false);

        verifyAll();
        final int totalDamage = captures.stream().mapToInt(Capture::getValue).sum();
        assertEquals(19329, totalDamage, 10);
    }

    @Test
    public void testExecuteCommandCard_kamaAvenger_evaded() {
        simulation = mock(Simulation.class);
        attacker = niceMock(Servant.class);
        defender = niceMock(Combatant.class);

        expect(simulation.getAttacker()).andReturn(attacker);
        expect(simulation.getDefender()).andReturn(defender);
        expect(defender.getFateClass()).andReturn(CASTER);
        expect(simulation.getCurrentCommandCard()).andReturn(KAMA_AVENGER_EXTRA);

        expect(attacker.applyBuff(simulation, CommandCardBuff.class)).andReturn(0.5);
        expect(attacker.applyBuff(simulation, DamageAdditionBuff.class)).andReturn(225.0);

        expect(attacker.getAttack()).andReturn(20977);
        expect(attacker.getFateClass()).andReturn(AVENGER);
        expect(attacker.getAttribute()).andReturn(SKY);
        expect(defender.getAttribute()).andReturn(SKY);
        expect(simulation.getFixedRandom()).andReturn(0.9);

        expect(defender.activateEvade(simulation)).andReturn(true);

        expect(defender.isAlreadyDead()).andReturn(false).times(KAMA_AVENGER_EXTRA.getHitPercentages().size());
        expect(defender.isBuggedOverkill()).andReturn(false).times(KAMA_AVENGER_EXTRA.getHitPercentages().size());
        expect(defender.getUndeadNpCorrection()).andReturn(false).times(KAMA_AVENGER_EXTRA.getHitPercentages().size());
        attacker.changeNp(0.93);
        expectLastCall().times(KAMA_AVENGER_EXTRA.getHitPercentages().size());
        simulation.gainStar(7.8);
        defender.addCumulativeTurnDamage(0);
        replayAll();

        executeCommandCard(simulation, 3, false, BUSTER, false);

        verifyAll();
    }

    @Test
    public void testCalculateTotalDamage_kamaAvenger() {
        final DamageParameters.DamageParametersBuilder damageParametersBuilder = DamageParameters.builder()
                .attack(20977)
                .totalHits(100)
                .attackerClass(AVENGER)
                .attackerAttribute(SKY)
                .isCriticalStrike(false)
                .firstCardType(BUSTER)
                .isTypeChain(false)
                .commandCardBuff(0)
                .commandCardResist(0)
                .attackBuff(0)
                .defenseBuff(0)
                .specificAttackBuff(0)
                .specificDefenseBuff(0)
                .percentAttackBuff(0)
                .percentDefenseBuff(0)
                .damageAdditionBuff(225)
                .damageReductionBuff(0)
                .criticalDamageBuff(0.1)
                .fixedRandom(0.9);

        // Kama (Avenger) BAQ
        damageParametersBuilder.defenderClass(CASTER).defenderAttribute(SKY);
        damageParametersBuilder.currentCardType(BUSTER).chainIndex(0).commandCardBuff(0);
        assertEquals(9776.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.currentCardType(ARTS).chainIndex(1).commandCardBuff(0);
        assertEquals(8343.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.currentCardType(QUICK).chainIndex(2).commandCardBuff(0.1);
        assertEquals(8496.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.currentCardType(EXTRA).chainIndex(3).commandCardBuff(0.5);
        assertEquals(19329.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        // Kama (Avenger) BAQ, with all skills
        damageParametersBuilder.currentCardType(BUSTER).chainIndex(0).commandCardBuff(0);
        assertEquals(9776.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.currentCardType(ARTS).chainIndex(1).commandCardBuff(0.3).specificAttackBuff(0.1);
        assertEquals(11046.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.currentCardType(QUICK).chainIndex(2).commandCardBuff(0.1).specificAttackBuff(0.2);
        assertEquals(10150.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.currentCardType(EXTRA).chainIndex(3).commandCardBuff(0.5).specificAttackBuff(0.3);
        assertEquals(25060.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        // Kama (Avenger) BAQ, but critical
        damageParametersBuilder.specificAttackBuff(0);
        damageParametersBuilder.isCriticalStrike(true);
        damageParametersBuilder.currentCardType(BUSTER).chainIndex(0).commandCardBuff(0);
        assertEquals(21240.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.currentCardType(ARTS).chainIndex(1).commandCardBuff(0);
        assertEquals(18088.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.currentCardType(QUICK).chainIndex(2).commandCardBuff(0.1);
        assertEquals(18424.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.isCriticalStrike(false);
        damageParametersBuilder.currentCardType(EXTRA).chainIndex(3).commandCardBuff(0.5);
        assertEquals(19329.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        // Kama (Avenger) BAQ, but in event
        damageParametersBuilder.defenderAttribute(MAN);
        damageParametersBuilder.defenderClass(LANCER);
        damageParametersBuilder.specificAttackBuff(1);
        damageParametersBuilder.attackBuff(0.3);
        damageParametersBuilder.currentCardType(BUSTER).chainIndex(0).commandCardBuff(0);
        assertEquals(22578.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.currentCardType(ARTS).chainIndex(1).commandCardBuff(0);
        assertEquals(19225.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.currentCardType(QUICK).chainIndex(2).commandCardBuff(0.1);
        assertEquals(19582.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.currentCardType(EXTRA).chainIndex(3).commandCardBuff(0.5);
        assertEquals(44930.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        // AAB
        damageParametersBuilder.isTypeChain(false);
        damageParametersBuilder.firstCardType(ARTS);
        damageParametersBuilder.currentCardType(BUSTER).chainIndex(2).commandCardBuff(0);
        assertEquals(23695.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        // Buster Chain
        damageParametersBuilder.isTypeChain(true);
        damageParametersBuilder.firstCardType(BUSTER);
        damageParametersBuilder.currentCardType(BUSTER).chainIndex(2).commandCardBuff(0);
        assertEquals(33478.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.isTypeChain(false);
        damageParametersBuilder.firstCardType(BUSTER);
        damageParametersBuilder.percentDefenseBuff(2);
        assertEquals(225, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.isTypeChain(true);
        damageParametersBuilder.firstCardType(BUSTER);
        damageParametersBuilder.percentDefenseBuff(2);
        assertEquals(4420, calculateTotalDamage(damageParametersBuilder.build()), 10);
    }

    @Test
    public void testCalculateNpGainPercentage_kamaAvenger() {
        final NpParameters.NpParametersBuilder npParametersBuilder = NpParameters.builder().npCharge(0.52);

        // Kama (Avenger) AQB & ARTS 30%
        npParametersBuilder.defenderClass(LANCER);
        npParametersBuilder.useUndeadNpCorrection(false);
        npParametersBuilder.firstCardType(ARTS);
        npParametersBuilder.isCriticalStrike(false);
        npParametersBuilder.currentCardType(ARTS).chainIndex(0).commandCardBuff(0.3).isOverkill(false);
        double totalHitsNp = 0;
        for (int i = 0; i < 3; i++) {
            totalHitsNp += calculateNpGainPercentage(npParametersBuilder.build());
        }
        assertEquals(7.62, totalHitsNp, 0.001);

        totalHitsNp = 0;
        npParametersBuilder.currentCardType(QUICK).chainIndex(1).commandCardBuff(0.1).isOverkill(false);
        for (int i = 0; i < 4; i++) {
            totalHitsNp += calculateNpGainPercentage(npParametersBuilder.build());
        }
        assertEquals(5.48, totalHitsNp, 0.001);

        totalHitsNp = 0;
        npParametersBuilder.currentCardType(BUSTER).chainIndex(2).commandCardBuff(0).isOverkill(true);
        for (int i = 0; i < 3; i++) {
            totalHitsNp += calculateNpGainPercentage(npParametersBuilder.build());
        }
        assertEquals(2.34, totalHitsNp, 0.001);

        totalHitsNp = 0;
        npParametersBuilder.currentCardType(EXTRA).chainIndex(3).commandCardBuff(0.5).isOverkill(true);
        for (int i = 0; i < 5; i++) {
            totalHitsNp += calculateNpGainPercentage(npParametersBuilder.build());
        }
        assertEquals(9.75, totalHitsNp, 0.001);

        // Kama (Avenger) BQA critical strike & ARTS 30%
        npParametersBuilder.firstCardType(BUSTER);
        npParametersBuilder.isCriticalStrike(true);
        npParametersBuilder.currentCardType(BUSTER).chainIndex(0).commandCardBuff(0).isOverkill(false);
        totalHitsNp = 0;
        for (int i = 0; i < 3; i++) {
            totalHitsNp += calculateNpGainPercentage(npParametersBuilder.build());
        }
        assertEquals(0, totalHitsNp, 0.001);

        totalHitsNp = 0;
        npParametersBuilder.currentCardType(QUICK).chainIndex(1).commandCardBuff(0.1).isOverkill(true);
        for (int i = 0; i < 4; i++) {
            totalHitsNp += calculateNpGainPercentage(npParametersBuilder.build());
        }
        assertEquals(10.24, totalHitsNp, 0.001);

        totalHitsNp = 0;
        npParametersBuilder.currentCardType(ARTS).chainIndex(2).commandCardBuff(0.3).isOverkill(true);
        for (int i = 0; i < 3; i++) {
            totalHitsNp += calculateNpGainPercentage(npParametersBuilder.build());
        }
        assertEquals(36.48, totalHitsNp, 0.001);

        totalHitsNp = 0;
        npParametersBuilder.isCriticalStrike(false);
        npParametersBuilder.currentCardType(EXTRA).chainIndex(3).commandCardBuff(0.5).isOverkill(true);
        for (int i = 0; i < 5; i++) {
            totalHitsNp += calculateNpGainPercentage(npParametersBuilder.build());
        }
        assertEquals(5.85, totalHitsNp, 0.001);
    }

    @Test
    public void testCalculateNpGainPercentage_minamotoNoRaikou() {
        final NpParameters.NpParametersBuilder npParametersBuilder = NpParameters.builder().npCharge(0.46);

        npParametersBuilder.defenderClass(LANCER)
                .useUndeadNpCorrection(false)
                .firstCardType(BUSTER)
                .isCriticalStrike(true)
                .npGenerationBuff(0.45)
                .commandCardBuff(0)
                .isOverkill(true);
        npParametersBuilder.currentCardType(ARTS).chainIndex(1);
        double totalHitsNp = 0;
        for (int i = 0; i < 4; i++) {
            totalHitsNp += calculateNpGainPercentage(npParametersBuilder.build());
        }
        assertEquals(36, totalHitsNp, 0.001);

        totalHitsNp = 0;
        npParametersBuilder.currentCardType(ARTS).chainIndex(2);
        for (int i = 0; i < 4; i++) {
            totalHitsNp += calculateNpGainPercentage(npParametersBuilder.build());
        }
        assertEquals(48, totalHitsNp, 0.001);

        totalHitsNp = 0;
        npParametersBuilder.currentCardType(EXTRA).chainIndex(3).isCriticalStrike(false);
        for (int i = 0; i < 5; i++) {
            totalHitsNp += calculateNpGainPercentage(npParametersBuilder.build());
        }
        assertEquals(4.95, totalHitsNp, 0.001);
    }

    @Test
    public void testCalculateNpGainPercentage_abigailWilliams() {
        final NpParameters.NpParametersBuilder npParametersBuilder = NpParameters.builder().npCharge(0.25);

        npParametersBuilder.defenderClass(BEAST_III_R)
                .useUndeadNpCorrection(false)
                .firstCardType(ARTS)
                .isCriticalStrike(true)
                .npGenerationBuff(0.3)
                .commandCardBuff(0.8)
                .isOverkill(true);
        double totalHitsNp = 0;
        npParametersBuilder.currentCardType(ARTS).chainIndex(2);
        for (int i = 0; i < 6; i++) {
            totalHitsNp += calculateNpGainPercentage(npParametersBuilder.build());
        }
        assertEquals(69, totalHitsNp, 0.001);
    }

    @Test
    public void testCalculateNpGainPercentage_vladIII() {
        final NpParameters.NpParametersBuilder npParametersBuilder = NpParameters.builder().npCharge(0.5);

        npParametersBuilder.defenderClass(LANCER)
                .useUndeadNpCorrection(false)
                .firstCardType(ARTS)
                .isCriticalStrike(true)
                .npGenerationBuff(0.3)
                .commandCardBuff(0.8)
                .isOverkill(true);
        double totalHitsNp = 0;
        npParametersBuilder.currentCardType(ARTS).chainIndex(2);
        for (int i = 0; i < 2; i++) {
            totalHitsNp += calculateNpGainPercentage(npParametersBuilder.build());
        }
        assertEquals(46, totalHitsNp, 0.001);
    }

    @Test
    public void testCalculateCritStarPercentage_kamaAvenger() {
        final CriticalStarParameters.CriticalStarParametersBuilder critStarParamsBuilder = CriticalStarParameters.builder()
                .servantCriticalStarGeneration(6.1)
                .defenderClass(LANCER);

        // QAB & ARTS 30%
        double totalStars = 0;
        critStarParamsBuilder.isCriticalStrike(false).firstCardType(QUICK).isOverkill(false);
        critStarParamsBuilder.currentCardType(QUICK).chainIndex(0).commandCardBuff(0.1);
        for (int i = 0; i < 4; i++) {
            totalStars += calculateCritStar(critStarParamsBuilder.build());
        }
        assertEquals(4.36, totalStars, 0.1);

        totalStars = 0;
        critStarParamsBuilder.currentCardType(ARTS).chainIndex(1).commandCardBuff(0.3);
        for (int i = 0; i < 3; i++) {
            totalStars += calculateCritStar(critStarParamsBuilder.build());
        }
        assertEquals(0.63, totalStars, 0.1);

        totalStars = 0;
        critStarParamsBuilder.isOverkill(true);
        critStarParamsBuilder.currentCardType(BUSTER).chainIndex(2).commandCardBuff(0);
        for (int i = 0; i < 3; i++) {
            totalStars += calculateCritStar(critStarParamsBuilder.build());
        }
        assertEquals(2.13, totalStars, 0.1);

        totalStars = 0;
        critStarParamsBuilder.currentCardType(EXTRA).chainIndex(3).commandCardBuff(0.5);
        for (int i = 0; i < 5; i++) {
            totalStars += calculateCritStar(critStarParamsBuilder.build());
        }
        assertEquals(10.05, totalStars, 0.1);

        // AQB & ARTS 30% & crit & 50% critStarGen
        totalStars = 0;
        critStarParamsBuilder.isCriticalStrike(true).firstCardType(ARTS).isOverkill(false).critStarGenerationBuff(0.5);
        critStarParamsBuilder.currentCardType(ARTS).chainIndex(0).commandCardBuff(0.3);
        for (int i = 0; i < 3; i++) {
            totalStars += calculateCritStar(critStarParamsBuilder.build());
        }
        assertEquals(2.13, totalStars, 0.1);

        totalStars = 0;
        critStarParamsBuilder.isOverkill(true);
        critStarParamsBuilder.currentCardType(QUICK).chainIndex(1).commandCardBuff(0.1);
        for (int i = 0; i < 4; i++) {
            totalStars += calculateCritStar(critStarParamsBuilder.build());
        }
        assertEquals(9.76, totalStars, 0.1);

        totalStars = 0;
        critStarParamsBuilder.currentCardType(BUSTER).chainIndex(2).commandCardBuff(0);
        for (int i = 0; i < 3; i++) {
            totalStars += calculateCritStar(critStarParamsBuilder.build());
        }
        assertEquals(3.63, totalStars, 0.1);

        totalStars = 0;
        critStarParamsBuilder.isCriticalStrike(false);
        critStarParamsBuilder.currentCardType(EXTRA).chainIndex(3).commandCardBuff(0.5);
        for (int i = 0; i < 5; i++) {
            totalStars += calculateCritStar(critStarParamsBuilder.build());
        }
        assertEquals(11.55, totalStars, 0.1);
    }
}
