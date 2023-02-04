package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableList;
import org.assertj.core.util.Lists;
import org.easymock.Capture;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.CommandCard;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.effects.CommandCardExecution.CriticalStarParameters;
import yome.fgo.simulator.models.effects.CommandCardExecution.DamageParameters;
import yome.fgo.simulator.models.effects.CommandCardExecution.NpParameters;
import yome.fgo.simulator.models.effects.buffs.Buff;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.captureInt;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.newCapture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import static yome.fgo.simulator.models.effects.CommandCardExecution.calculateNpGain;
import static yome.fgo.simulator.models.effects.CommandCardExecution.calculateTotalDamage;
import static yome.fgo.simulator.models.effects.CommandCardExecution.executeCommandCard;
import static yome.fgo.simulator.models.effects.CommandCardExecution.getHitsPercentages;
import static yome.fgo.simulator.models.effects.buffs.BuffType.CLASS_ADVANTAGE_CHANGE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.COMMAND_CARD_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DAMAGE_ADDITION_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DAMAGE_REDUCTION_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.EVADE;
import static yome.fgo.simulator.models.effects.buffs.BuffType.HITS_DOUBLED_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.IGNORE_INVINCIBLE;
import static yome.fgo.simulator.models.effects.buffs.BuffType.INVINCIBLE;
import static yome.fgo.simulator.models.effects.buffs.BuffType.PRE_ATTACK_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.PRE_DEFENSE_EFFECT;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SPECIAL_INVINCIBLE;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SURE_HIT;
import static yome.fgo.simulator.utils.FateClassUtils.getClassNpCorrection;

public class CommandCardExecutionTest extends EasyMockSupport {
    public static final CommandCard KAMA_AVENGER_BUSTER = new CommandCard(
            CommandCardData.newBuilder()
                    .setCommandCardType(BUSTER)
                    .addAllHitsData(ImmutableList.of(16, 33, 51))
                    .setNpRate(0.0052)
                    .setCriticalStarGen(0.061)
                    .build());
    public static final CommandCard KAMA_AVENGER_ARTS = new CommandCard(
            CommandCardData.newBuilder()
                    .setCommandCardType(ARTS)
                    .addAllHitsData(ImmutableList.of(16, 33, 51))
                    .setNpRate(0.0052)
                    .setCriticalStarGen(0.061)
                    .build());
    public static final CommandCard KAMA_AVENGER_QUICK = new CommandCard(
            CommandCardData.newBuilder()
                     .setCommandCardType(QUICK)
                     .addAllHitsData(ImmutableList.of(10, 20, 30, 40))
                     .setNpRate(0.0052)
                    .setCriticalStarGen(0.061)
                     .build());
    public static final CommandCard KAMA_AVENGER_EXTRA = new CommandCard(
            CommandCardData.newBuilder()
                     .setCommandCardType(EXTRA)
                     .addAllHitsData(ImmutableList.of(6, 13, 20, 26, 35))
                     .setNpRate(0.0052)
                    .setCriticalStarGen(0.061)
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
        expect(attacker.getBuffs()).andReturn(ImmutableList.of()).anyTimes();
        expect(defender.getBuffs()).andReturn(ImmutableList.of()).anyTimes();
        expect(defender.getFateClass()).andReturn(CASTER).anyTimes();
        expect(simulation.getCurrentCommandCard()).andReturn(KAMA_AVENGER_BUSTER);
        expect(attacker.fetchBuffs(HITS_DOUBLED_BUFF)).andReturn(Lists.newArrayList());
        expect(attacker.fetchBuffs(CLASS_ADVANTAGE_CHANGE_BUFF)).andReturn(Lists.newArrayList());
        expect(defender.fetchBuffs(CLASS_ADVANTAGE_CHANGE_BUFF)).andReturn(Lists.newArrayList());

        expect(attacker.applyBuff(simulation, DAMAGE_ADDITION_BUFF)).andReturn(225.0);

        expect(attacker.getAttack()).andReturn(20977);
        expect(attacker.getFateClass()).andReturn(AVENGER).anyTimes();
        expect(attacker.getAttribute()).andReturn(SKY);
        expect(defender.getAttribute()).andReturn(SKY);
        expect(simulation.getFixedRandom()).andReturn(0.9);

        expect(defender.consumeBuffIfExist(simulation, SPECIAL_INVINCIBLE)).andReturn(false);
        expect(attacker.consumeBuffIfExist(simulation, IGNORE_INVINCIBLE)).andReturn(true);
        expect(defender.consumeBuffIfExist(simulation, INVINCIBLE)).andReturn(false);

        final List<Capture<Integer>> captures = new ArrayList<>();
        for (int i = 0; i < KAMA_AVENGER_BUSTER.getHitPercentages().size(); i += 1) {
            final Capture<Integer> capturedDamage = newCapture();
            defender.receiveDamage(captureInt(capturedDamage));
            captures.add(capturedDamage);
        }
        expect(defender.isAlreadyDead()).andReturn(false);
        expect(defender.isBuggedOverkill()).andReturn(false);
        expect(defender.isAlreadyDead()).andReturn(true).times(2);
        expect(defender.getUndeadNpCorrection()).andReturn(false);
        expect(defender.getCombatantData()).andReturn(CombatantData.getDefaultInstance());
        attacker.changeNp(0.0062);
        attacker.changeNp(0.0093);
        expectLastCall().times(2);
        simulation.gainStar(1.683);
        simulation.checkBuffStatus();
        defender.addCumulativeTurnDamage(anyInt());
        expect(simulation.getStatsLogger()).andReturn(null).anyTimes();
        replayAll();

        executeCommandCard(simulation, 0, false, BUSTER, false, true);

        final int totalDamage = captures.stream().mapToInt(Capture::getValue).sum();
        assertEquals(9776, totalDamage, 10);
        verifyAll();
    }

    @Test
    public void testExecuteCommandCard_kamaAvenger_extraInBAQ() {
        simulation = mock(Simulation.class);
        attacker = niceMock(Servant.class);
        defender = niceMock(Combatant.class);

        expect(simulation.getAttacker()).andReturn(attacker);
        expect(simulation.getDefender()).andReturn(defender);
        expect(defender.getFateClass()).andReturn(CASTER).anyTimes();
        expect(simulation.getCurrentCommandCard()).andReturn(KAMA_AVENGER_EXTRA);
        expect(attacker.getBuffs()).andReturn(ImmutableList.of()).anyTimes();
        expect(defender.getBuffs()).andReturn(ImmutableList.of()).anyTimes();
        expect(attacker.fetchBuffs(HITS_DOUBLED_BUFF)).andReturn(Lists.newArrayList());
        expect(attacker.fetchBuffs(CLASS_ADVANTAGE_CHANGE_BUFF)).andReturn(Lists.newArrayList());
        expect(defender.fetchBuffs(CLASS_ADVANTAGE_CHANGE_BUFF)).andReturn(Lists.newArrayList());

        expect(attacker.applyBuff(simulation, COMMAND_CARD_BUFF)).andReturn(0.5);
        expect(attacker.applyBuff(simulation, DAMAGE_ADDITION_BUFF)).andReturn(225.0);

        expect(attacker.getAttack()).andReturn(20977);
        expect(attacker.getFateClass()).andReturn(AVENGER).anyTimes();
        expect(attacker.getAttribute()).andReturn(SKY);
        expect(defender.getAttribute()).andReturn(SKY);
        expect(simulation.getFixedRandom()).andReturn(0.9);

        expect(defender.consumeBuffIfExist(simulation, SPECIAL_INVINCIBLE)).andReturn(false);
        expect(attacker.consumeBuffIfExist(simulation, IGNORE_INVINCIBLE)).andReturn(true);
        expect(defender.consumeBuffIfExist(simulation, INVINCIBLE)).andReturn(false);

        final List<Capture<Integer>> captures = new ArrayList<>();
        for (int i = 0; i < KAMA_AVENGER_EXTRA.getHitPercentages().size(); i += 1) {
            final Capture<Integer> capturedDamage = newCapture();
            defender.receiveDamage(captureInt(capturedDamage));
            captures.add(capturedDamage);
        }
        expect(defender.isAlreadyDead()).andReturn(true).times(KAMA_AVENGER_EXTRA.getHitPercentages().size());
        expect(defender.getUndeadNpCorrection()).andReturn(false);
        attacker.changeNp(0.0234);
        expectLastCall().times(KAMA_AVENGER_EXTRA.getHitPercentages().size());
        simulation.gainStar(10.305);
        simulation.checkBuffStatus();
        defender.addCumulativeTurnDamage(anyInt());
        expect(simulation.getStatsLogger()).andReturn(null).anyTimes();
        expect(defender.getCombatantData()).andReturn(CombatantData.getDefaultInstance()).anyTimes();
        replayAll();

        executeCommandCard(simulation, 3, false, BUSTER, false, true);

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
        expect(defender.getFateClass()).andReturn(CASTER).anyTimes();
        expect(simulation.getCurrentCommandCard()).andReturn(KAMA_AVENGER_EXTRA);
        expect(attacker.getBuffs()).andReturn(ImmutableList.of()).anyTimes();
        expect(defender.getBuffs()).andReturn(ImmutableList.of()).anyTimes();
        expect(attacker.fetchBuffs(HITS_DOUBLED_BUFF)).andReturn(Lists.newArrayList());
        expect(attacker.fetchBuffs(CLASS_ADVANTAGE_CHANGE_BUFF)).andReturn(Lists.newArrayList());
        expect(defender.fetchBuffs(CLASS_ADVANTAGE_CHANGE_BUFF)).andReturn(Lists.newArrayList());

        expect(attacker.applyBuff(simulation, COMMAND_CARD_BUFF)).andReturn(0.5);
        expect(attacker.applyBuff(simulation, DAMAGE_ADDITION_BUFF)).andReturn(225.0);

        expect(attacker.getAttack()).andReturn(20977);
        expect(attacker.getFateClass()).andReturn(AVENGER).anyTimes();
        expect(attacker.getAttribute()).andReturn(SKY);
        expect(defender.getAttribute()).andReturn(SKY);
        expect(simulation.getFixedRandom()).andReturn(0.9);

        expect(defender.consumeBuffIfExist(simulation, SPECIAL_INVINCIBLE)).andReturn(false);
        expect(attacker.consumeBuffIfExist(simulation, IGNORE_INVINCIBLE)).andReturn(false);
        expect(defender.consumeBuffIfExist(simulation, INVINCIBLE)).andReturn(false);
        expect(attacker.consumeBuffIfExist(simulation, SURE_HIT)).andReturn(false);
        expect(defender.consumeBuffIfExist(simulation, EVADE)).andReturn(true);

        expect(defender.isAlreadyDead()).andReturn(false).times(KAMA_AVENGER_EXTRA.getHitPercentages().size());
        expect(defender.isBuggedOverkill()).andReturn(false).times(KAMA_AVENGER_EXTRA.getHitPercentages().size());
        expect(defender.getUndeadNpCorrection()).andReturn(false);
        attacker.changeNp(0.0093);
        expectLastCall().times(KAMA_AVENGER_EXTRA.getHitPercentages().size());
        simulation.gainStar(7.805);
        simulation.checkBuffStatus();
        defender.addCumulativeTurnDamage(0);
        expect(simulation.getStatsLogger()).andReturn(null).anyTimes();
        expect(defender.getCombatantData()).andReturn(CombatantData.getDefaultInstance()).anyTimes();
        replayAll();

        executeCommandCard(simulation, 3, false, BUSTER, false, false);

        verifyAll();
    }

    @Test
    public void testCalculateTotalDamage_kamaAvenger() {
        final DamageParameters.DamageParametersBuilder damageParametersBuilder = DamageParameters.builder()
                .attack(20977)
                .totalHits(100)
                .attackerClass(AVENGER)
                .classAdvantage(1)
                .attackerAttribute(SKY)
                .isCriticalStrike(false)
                .useFirstCardBoost(true)
                .isBusterChain(false)
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
        damageParametersBuilder.useFirstCardBoost(false);
        damageParametersBuilder.currentCardType(BUSTER).chainIndex(2).commandCardBuff(0);
        assertEquals(23695.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        // Buster Chain
        damageParametersBuilder.isTypeChain(true);
        damageParametersBuilder.useFirstCardBoost(true);
        damageParametersBuilder.isBusterChain(true);
        damageParametersBuilder.currentCardType(BUSTER).chainIndex(2).commandCardBuff(0);
        assertEquals(33478.0, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.isTypeChain(false);
        damageParametersBuilder.useFirstCardBoost(true);
        damageParametersBuilder.isBusterChain(false);
        damageParametersBuilder.percentDefenseBuff(2);
        assertEquals(225, calculateTotalDamage(damageParametersBuilder.build()), 10);

        damageParametersBuilder.isTypeChain(true);
        damageParametersBuilder.useFirstCardBoost(true);
        damageParametersBuilder.isBusterChain(true);
        damageParametersBuilder.percentDefenseBuff(2);
        assertEquals(4420, calculateTotalDamage(damageParametersBuilder.build()), 10);
    }

    @Test
    public void testCalculateNpGain_kamaAvenger() {
        final NpParameters.NpParametersBuilder npParametersBuilder = NpParameters.builder().npCharge(0.0052);

        // Kama (Avenger) AQB & ARTS 30%
        npParametersBuilder.defenderClass(LANCER);
        npParametersBuilder.useUndeadNpCorrection(false);
        npParametersBuilder.useFirstCardBoost(true);
        npParametersBuilder.isCriticalStrike(false);
        npParametersBuilder.currentCardType(ARTS).chainIndex(0).commandCardBuff(0.3);
        npParametersBuilder.classNpCorrection(getClassNpCorrection(LANCER));
        double totalHitsNp = 0;
        for (int i = 0; i < 3; i += 1) {
            totalHitsNp += calculateNpGain(npParametersBuilder.build(), false);
        }
        assertEquals(0.0762, totalHitsNp, 0.00001);

        totalHitsNp = 0;
        npParametersBuilder.currentCardType(QUICK).chainIndex(1).commandCardBuff(0.1);
        for (int i = 0; i < 4; i += 1) {
            totalHitsNp += calculateNpGain(npParametersBuilder.build(), false);
        }
        assertEquals(0.0548, totalHitsNp, 0.00001);

        totalHitsNp = 0;
        npParametersBuilder.currentCardType(BUSTER).chainIndex(2).commandCardBuff(0);
        for (int i = 0; i < 3; i += 1) {
            totalHitsNp += calculateNpGain(npParametersBuilder.build(), true);
        }
        assertEquals(0.0234, totalHitsNp, 0.00001);

        totalHitsNp = 0;
        npParametersBuilder.currentCardType(EXTRA).chainIndex(3).commandCardBuff(0.5);
        for (int i = 0; i < 5; i += 1) {
            totalHitsNp += calculateNpGain(npParametersBuilder.build(), true);
        }
        assertEquals(0.0975, totalHitsNp, 0.00001);

        // Kama (Avenger) BQA critical strike & ARTS 30%
        npParametersBuilder.useFirstCardBoost(false);
        npParametersBuilder.isCriticalStrike(true);
        npParametersBuilder.currentCardType(BUSTER).chainIndex(0).commandCardBuff(0);
        totalHitsNp = 0;
        for (int i = 0; i < 3; i += 1) {
            totalHitsNp += calculateNpGain(npParametersBuilder.build(), false);
        }
        assertEquals(0, totalHitsNp, 0.00001);

        totalHitsNp = 0;
        npParametersBuilder.currentCardType(QUICK).chainIndex(1).commandCardBuff(0.1);
        for (int i = 0; i < 4; i += 1) {
            totalHitsNp += calculateNpGain(npParametersBuilder.build(), true);
        }
        assertEquals(0.1024, totalHitsNp, 0.001);

        totalHitsNp = 0;
        npParametersBuilder.currentCardType(ARTS).chainIndex(2).commandCardBuff(0.3);
        for (int i = 0; i < 3; i += 1) {
            totalHitsNp += calculateNpGain(npParametersBuilder.build(), true);
        }
        assertEquals(0.3648, totalHitsNp, 0.001);

        totalHitsNp = 0;
        npParametersBuilder.isCriticalStrike(false);
        npParametersBuilder.currentCardType(EXTRA).chainIndex(3).commandCardBuff(0.5);
        for (int i = 0; i < 5; i += 1) {
            totalHitsNp += calculateNpGain(npParametersBuilder.build(), true);
        }
        assertEquals(0.0585, totalHitsNp, 0.001);
    }

    @Test
    public void testCalculateNpGain_minamotoNoRaikou() {
        final NpParameters.NpParametersBuilder npParametersBuilder = NpParameters.builder().npCharge(0.0046);

        npParametersBuilder.defenderClass(LANCER)
                .useUndeadNpCorrection(false)
                .useFirstCardBoost(false)
                .isCriticalStrike(true)
                .npGenerationBuff(0.45)
                .commandCardBuff(0)
                .classNpCorrection(getClassNpCorrection(LANCER));
        npParametersBuilder.currentCardType(ARTS).chainIndex(1);
        double totalHitsNp = 0;
        for (int i = 0; i < 4; i += 1) {
            totalHitsNp += calculateNpGain(npParametersBuilder.build(), true);
        }
        assertEquals(0.36, totalHitsNp, 0.00001);

        totalHitsNp = 0;
        npParametersBuilder.currentCardType(ARTS).chainIndex(2);
        for (int i = 0; i < 4; i += 1) {
            totalHitsNp += calculateNpGain(npParametersBuilder.build(), true);
        }
        assertEquals(0.48, totalHitsNp, 0.00001);

        totalHitsNp = 0;
        npParametersBuilder.currentCardType(EXTRA).chainIndex(3).isCriticalStrike(false);
        for (int i = 0; i < 5; i += 1) {
            totalHitsNp += calculateNpGain(npParametersBuilder.build(), true);
        }
        assertEquals(0.0495, totalHitsNp, 0.00001);
    }

    @Test
    public void testCalculateNpGain_abigailWilliams() {
        final NpParameters.NpParametersBuilder npParametersBuilder = NpParameters.builder().npCharge(0.0025);

        npParametersBuilder.defenderClass(BEAST_III_R)
                .useUndeadNpCorrection(false)
                .useFirstCardBoost(true)
                .isCriticalStrike(true)
                .npGenerationBuff(0.3)
                .commandCardBuff(0.8)
                .classNpCorrection(getClassNpCorrection(BEAST_III_R));
        double totalHitsNp = 0;
        npParametersBuilder.currentCardType(ARTS).chainIndex(2);
        for (int i = 0; i < 6; i += 1) {
            totalHitsNp += calculateNpGain(npParametersBuilder.build(), true);
        }
        assertEquals(0.69, totalHitsNp, 0.00001);
    }

    @Test
    public void testCalculateNpGain_vladIII() {
        final NpParameters.NpParametersBuilder npParametersBuilder = NpParameters.builder().npCharge(0.005);

        npParametersBuilder.defenderClass(LANCER)
                .useUndeadNpCorrection(false)
                .useFirstCardBoost(true)
                .isCriticalStrike(true)
                .npGenerationBuff(0.3)
                .commandCardBuff(0.8)
                .classNpCorrection(getClassNpCorrection(LANCER));
        double totalHitsNp = 0;
        npParametersBuilder.currentCardType(ARTS).chainIndex(2);
        for (int i = 0; i < 2; i += 1) {
            totalHitsNp += calculateNpGain(npParametersBuilder.build(), true);
        }
        assertEquals(0.4602, totalHitsNp, 0.00001);
    }

    @Test
    public void testCalculateCritStar_kamaAvenger() {
        final CriticalStarParameters.CriticalStarParametersBuilder critStarParamsBuilder = CriticalStarParameters.builder()
                .servantCriticalStarGeneration(0.061)
                .defenderClass(LANCER);

        // QAB & ARTS 30%
        double totalStars = 0;
        critStarParamsBuilder.isCriticalStrike(false).useFirstCardBoost(true);
        critStarParamsBuilder.currentCardType(QUICK).chainIndex(0).commandCardBuff(0.1);
        for (int i = 0; i < 4; i += 1) {
            totalStars += calculateCritStar(critStarParamsBuilder.build(), false);
        }
        assertEquals(4.36, totalStars, 0.1);

        totalStars = 0;
        critStarParamsBuilder.currentCardType(ARTS).chainIndex(1).commandCardBuff(0.3);
        for (int i = 0; i < 3; i += 1) {
            totalStars += calculateCritStar(critStarParamsBuilder.build(), false);
        }
        assertEquals(0.63, totalStars, 0.1);

        totalStars = 0;
        critStarParamsBuilder.currentCardType(BUSTER).chainIndex(2).commandCardBuff(0);
        for (int i = 0; i < 3; i += 1) {
            totalStars += calculateCritStar(critStarParamsBuilder.build(), true);
        }
        assertEquals(2.13, totalStars, 0.1);

        totalStars = 0;
        critStarParamsBuilder.currentCardType(EXTRA).chainIndex(3).commandCardBuff(0.5);
        for (int i = 0; i < 5; i += 1) {
            totalStars += calculateCritStar(critStarParamsBuilder.build(), true);
        }
        assertEquals(10.05, totalStars, 0.1);

        // AQB & ARTS 30% & crit & 50% critStarGen
        totalStars = 0;
        critStarParamsBuilder.isCriticalStrike(true).useFirstCardBoost(false).critStarGenerationBuff(0.5);
        critStarParamsBuilder.currentCardType(ARTS).chainIndex(0).commandCardBuff(0.3);
        for (int i = 0; i < 3; i += 1) {
            totalStars += calculateCritStar(critStarParamsBuilder.build(), false);
        }
        assertEquals(2.13, totalStars, 0.1);

        totalStars = 0;
        critStarParamsBuilder.currentCardType(QUICK).chainIndex(1).commandCardBuff(0.1);
        for (int i = 0; i < 4; i += 1) {
            totalStars += calculateCritStar(critStarParamsBuilder.build(), true);
        }
        assertEquals(9.76, totalStars, 0.1);

        totalStars = 0;
        critStarParamsBuilder.currentCardType(BUSTER).chainIndex(2).commandCardBuff(0);
        for (int i = 0; i < 3; i += 1) {
            totalStars += calculateCritStar(critStarParamsBuilder.build(), true);
        }
        assertEquals(3.63, totalStars, 0.1);

        totalStars = 0;
        critStarParamsBuilder.isCriticalStrike(false);
        critStarParamsBuilder.currentCardType(EXTRA).chainIndex(3).commandCardBuff(0.5);
        for (int i = 0; i < 5; i += 1) {
            totalStars += calculateCritStar(critStarParamsBuilder.build(), true);
        }
        assertEquals(11.55, totalStars, 0.1);
    }

    @Test
    public void testDoubleHits() {
        final Combatant combatant = new Combatant();
        combatant.addBuff(Buff.builder().buffType(HITS_DOUBLED_BUFF).build());
        final List<Double> doubledHits = getHitsPercentages(new Simulation(), combatant, KAMA_AVENGER_ARTS.getHitPercentages());
        assertThat(doubledHits).containsExactly(8.0, 8.0, 16.5, 16.5, 25.5, 25.5);
        final List<Double> doubledHits2 = getHitsPercentages(new Simulation(), combatant, KAMA_AVENGER_QUICK.getHitPercentages());
        assertThat(doubledHits2).containsExactly(5.0, 5.0, 10.0, 10.0, 15.0, 15.0, 20.0, 20.0);
    }
}
