package yome.fgo.data.writer.servants;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.util.JsonFormat;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillData;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillUpgrades;
import yome.fgo.data.proto.FgoStorageData.AppendSkillData;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmData;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmUpgrades;
import yome.fgo.data.proto.FgoStorageData.PassiveSkillData;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.data.proto.FgoStorageData.Traits;
import yome.fgo.simulator.models.conditions.BuffHasTrait;
import yome.fgo.simulator.models.conditions.BuffIsDebuff;
import yome.fgo.simulator.models.conditions.CardTypeEquals;
import yome.fgo.simulator.models.conditions.TargetsHaveClass;
import yome.fgo.simulator.models.conditions.TargetsHaveTrait;
import yome.fgo.simulator.models.effects.GrantBuff;
import yome.fgo.simulator.models.effects.NpChange;
import yome.fgo.simulator.models.effects.RemoveBuff;
import yome.fgo.simulator.models.effects.buffs.AttackBuff;
import yome.fgo.simulator.models.effects.buffs.CommandCardBuff;
import yome.fgo.simulator.models.effects.buffs.CriticalChanceResist;
import yome.fgo.simulator.models.effects.buffs.CriticalDamageBuff;
import yome.fgo.simulator.models.effects.buffs.DebuffResist;
import yome.fgo.simulator.models.effects.buffs.Invincible;
import yome.fgo.simulator.models.effects.buffs.NpGenerationBuff;
import yome.fgo.simulator.models.effects.buffs.SpecialInvincible;
import yome.fgo.simulator.models.effects.buffs.SpecificAttackBuff;

import java.io.IOException;
import java.io.StringReader;

import static yome.fgo.data.proto.FgoStorageData.Alignment.GOOD;
import static yome.fgo.data.proto.FgoStorageData.Alignment.NEUTRAL;
import static yome.fgo.data.proto.FgoStorageData.Attribute.STAR;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.NEGATIVE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.EXTRA;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.data.proto.FgoStorageData.FateClass.CASTER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.SABER;
import static yome.fgo.data.proto.FgoStorageData.Gender.FEMALE;
import static yome.fgo.data.proto.FgoStorageData.NoblePhantasmType.NON_DAMAGE;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_ALLIES;
import static yome.fgo.data.proto.FgoStorageData.Target.DEFENDER;
import static yome.fgo.data.proto.FgoStorageData.Target.SELF;
import static yome.fgo.data.proto.FgoStorageData.Target.TARGETED_ALLY;
import static yome.fgo.data.writer.DataWriter.generateSkillValues;
import static yome.fgo.data.writer.DataWriter.writeServant;

public class Servant284 {
    public static void main(String[] args) {
        final String levelStatusData = "{\"lv\": 1, \"HP\": 2112, \"ATK\": 1629},{\"lv\": 2, \"HP\": 2124, \"ATK\": 1637},{\"lv\": 3, \"HP\": 2136, \"ATK\": 1646},{\"lv\": 4, \"HP\": 2161, \"ATK\": 1664},{\"lv\": 5, \"HP\": 2198, \"ATK\": 1691},{\"lv\": 6, \"HP\": 2247, \"ATK\": 1727},{\"lv\": 7, \"HP\": 2296, \"ATK\": 1762},{\"lv\": 8, \"HP\": 2345, \"ATK\": 1798},{\"lv\": 9, \"HP\": 2419, \"ATK\": 1851},{\"lv\": 10, \"HP\": 2480, \"ATK\": 1896},{\"lv\": 11, \"HP\": 2566, \"ATK\": 1958},{\"lv\": 12, \"HP\": 2652, \"ATK\": 2021},{\"lv\": 13, \"HP\": 2738, \"ATK\": 2083},{\"lv\": 14, \"HP\": 2837, \"ATK\": 2155},{\"lv\": 15, \"HP\": 2947, \"ATK\": 2235},{\"lv\": 16, \"HP\": 3058, \"ATK\": 2315},{\"lv\": 17, \"HP\": 3181, \"ATK\": 2404},{\"lv\": 18, \"HP\": 3304, \"ATK\": 2493},{\"lv\": 19, \"HP\": 3439, \"ATK\": 2592},{\"lv\": 20, \"HP\": 3574, \"ATK\": 2690},{\"lv\": 21, \"HP\": 3722, \"ATK\": 2797},{\"lv\": 22, \"HP\": 3870, \"ATK\": 2904},{\"lv\": 23, \"HP\": 4017, \"ATK\": 3011},{\"lv\": 24, \"HP\": 4177, \"ATK\": 3127},{\"lv\": 25, \"HP\": 4349, \"ATK\": 3251},{\"lv\": 26, \"HP\": 4521, \"ATK\": 3376},{\"lv\": 27, \"HP\": 4693, \"ATK\": 3501},{\"lv\": 28, \"HP\": 4865, \"ATK\": 3626},{\"lv\": 29, \"HP\": 5050, \"ATK\": 3760},{\"lv\": 30, \"HP\": 5246, \"ATK\": 3902},{\"lv\": 31, \"HP\": 5431, \"ATK\": 4036},{\"lv\": 32, \"HP\": 5628, \"ATK\": 4179},{\"lv\": 33, \"HP\": 5824, \"ATK\": 4321},{\"lv\": 34, \"HP\": 6021, \"ATK\": 4464},{\"lv\": 35, \"HP\": 6230, \"ATK\": 4616},{\"lv\": 36, \"HP\": 6439, \"ATK\": 4767},{\"lv\": 37, \"HP\": 6648, \"ATK\": 4919},{\"lv\": 38, \"HP\": 6857, \"ATK\": 5070},{\"lv\": 39, \"HP\": 7066, \"ATK\": 5222},{\"lv\": 40, \"HP\": 7275, \"ATK\": 5374},{\"lv\": 41, \"HP\": 7496, \"ATK\": 5534},{\"lv\": 42, \"HP\": 7705, \"ATK\": 5686},{\"lv\": 43, \"HP\": 7927, \"ATK\": 5846},{\"lv\": 44, \"HP\": 8148, \"ATK\": 6007},{\"lv\": 45, \"HP\": 8357, \"ATK\": 6158},{\"lv\": 46, \"HP\": 8578, \"ATK\": 6319},{\"lv\": 47, \"HP\": 8799, \"ATK\": 6479},{\"lv\": 48, \"HP\": 9008, \"ATK\": 6631},{\"lv\": 49, \"HP\": 9230, \"ATK\": 6791},{\"lv\": 50, \"HP\": 9439, \"ATK\": 6943},{\"lv\": 51, \"HP\": 9648, \"ATK\": 7095},{\"lv\": 52, \"HP\": 9857, \"ATK\": 7246},{\"lv\": 53, \"HP\": 10066, \"ATK\": 7398},{\"lv\": 54, \"HP\": 10275, \"ATK\": 7549},{\"lv\": 55, \"HP\": 10484, \"ATK\": 7701},{\"lv\": 56, \"HP\": 10680, \"ATK\": 7844},{\"lv\": 57, \"HP\": 10877, \"ATK\": 7986},{\"lv\": 58, \"HP\": 11074, \"ATK\": 8129},{\"lv\": 59, \"HP\": 11258, \"ATK\": 8263},{\"lv\": 60, \"HP\": 11455, \"ATK\": 8405},{\"lv\": 61, \"HP\": 11639, \"ATK\": 8539},{\"lv\": 62, \"HP\": 11811, \"ATK\": 8664},{\"lv\": 63, \"HP\": 11984, \"ATK\": 8789},{\"lv\": 64, \"HP\": 12156, \"ATK\": 8914},{\"lv\": 65, \"HP\": 12328, \"ATK\": 9039},{\"lv\": 66, \"HP\": 12488, \"ATK\": 9154},{\"lv\": 67, \"HP\": 12635, \"ATK\": 9261},{\"lv\": 68, \"HP\": 12783, \"ATK\": 9368},{\"lv\": 69, \"HP\": 12930, \"ATK\": 9475},{\"lv\": 70, \"HP\": 13065, \"ATK\": 9574},{\"lv\": 71, \"HP\": 13201, \"ATK\": 9672},{\"lv\": 72, \"HP\": 13324, \"ATK\": 9761},{\"lv\": 73, \"HP\": 13447, \"ATK\": 9850},{\"lv\": 74, \"HP\": 13557, \"ATK\": 9930},{\"lv\": 75, \"HP\": 13668, \"ATK\": 10010},{\"lv\": 76, \"HP\": 13766, \"ATK\": 10082},{\"lv\": 77, \"HP\": 13852, \"ATK\": 10144},{\"lv\": 78, \"HP\": 13938, \"ATK\": 10207},{\"lv\": 79, \"HP\": 14024, \"ATK\": 10269},{\"lv\": 80, \"HP\": 14086, \"ATK\": 10314},{\"lv\": 81, \"HP\": 14160, \"ATK\": 10367},{\"lv\": 82, \"HP\": 14209, \"ATK\": 10403},{\"lv\": 83, \"HP\": 14233, \"ATK\": 10421},{\"lv\": 84, \"HP\": 14258, \"ATK\": 10438},{\"lv\": 85, \"HP\": 14283, \"ATK\": 10456},{\"lv\": 86, \"HP\": 14307, \"ATK\": 10474},{\"lv\": 87, \"HP\": 14332, \"ATK\": 10492},{\"lv\": 88, \"HP\": 14356, \"ATK\": 10510},{\"lv\": 89, \"HP\": 14381, \"ATK\": 10528},{\"lv\": 90, \"HP\": 14406, \"ATK\": 10546},{\"lv\": 91, \"HP\": 14541, \"ATK\": 10644},{\"lv\": 92, \"HP\": 14676, \"ATK\": 10742},{\"lv\": 93, \"HP\": 14811, \"ATK\": 10840},{\"lv\": 94, \"HP\": 14946, \"ATK\": 10938},{\"lv\": 95, \"HP\": 15094, \"ATK\": 11045},{\"lv\": 96, \"HP\": 15229, \"ATK\": 11143},{\"lv\": 97, \"HP\": 15364, \"ATK\": 11241},{\"lv\": 98, \"HP\": 15500, \"ATK\": 11339},{\"lv\": 99, \"HP\": 15647, \"ATK\": 11446},{\"lv\": 100, \"HP\": 15782, \"ATK\": 11544},{\"lv\": 101, \"HP\": 15918, \"ATK\": 11642},{\"lv\": 102, \"HP\": 16053, \"ATK\": 11740},{\"lv\": 103, \"HP\": 16200, \"ATK\": 11847},{\"lv\": 104, \"HP\": 16336, \"ATK\": 11945},{\"lv\": 105, \"HP\": 16471, \"ATK\": 12044},{\"lv\": 106, \"HP\": 16606, \"ATK\": 12142},{\"lv\": 107, \"HP\": 16754, \"ATK\": 12249},{\"lv\": 108, \"HP\": 16889, \"ATK\": 12347},{\"lv\": 109, \"HP\": 17024, \"ATK\": 12445},{\"lv\": 110, \"HP\": 17159, \"ATK\": 12543},{\"lv\": 111, \"HP\": 17295, \"ATK\": 12641},{\"lv\": 112, \"HP\": 17442, \"ATK\": 12748},{\"lv\": 113, \"HP\": 17577, \"ATK\": 12846},{\"lv\": 114, \"HP\": 17713, \"ATK\": 12944},{\"lv\": 115, \"HP\": 17848, \"ATK\": 13042},{\"lv\": 116, \"HP\": 17995, \"ATK\": 13149},{\"lv\": 117, \"HP\": 18131, \"ATK\": 13247},{\"lv\": 118, \"HP\": 18266, \"ATK\": 13345},{\"lv\": 119, \"HP\": 18401, \"ATK\": 13444},{\"lv\": 120, \"HP\": 18549, \"ATK\": 13551}";
        final ServantAscensionData.Builder status = ServantAscensionData.newBuilder();
        try {
            JsonFormat.parser().merge(new StringReader("{\"servantStatusData\":[" + levelStatusData + "]}"), status);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final String id = "servant284";
        final CombatantData.Builder commonCombatantData = CombatantData.newBuilder();
        commonCombatantData.setId(id);
        commonCombatantData.setRarity(5);
        commonCombatantData.setFateClass(CASTER);
        commonCombatantData.setGender(FEMALE);
        commonCombatantData.addAlignments(NEUTRAL);
        commonCombatantData.addAlignments(GOOD);
        commonCombatantData.setAttribute(STAR);
        commonCombatantData.addTraits(Traits.ARTHUR.name());
        commonCombatantData.addTraits(Traits.SABER_FACE.name());
        commonCombatantData.addTraits(Traits.KNIGHTS_OF_THE_ROUND.name());
        commonCombatantData.addTraits(Traits.HUMANOID.name());
        commonCombatantData.setDeathRate(0.36);
        final CombatantData firstAscensionCombatantData = commonCombatantData.build();
        final CombatantData secondAscensionCombatantData = commonCombatantData.addTraits(Traits.FAE.name()).build();

        final CommandCardData quickCard = CommandCardData.newBuilder()
                .setCommandCardType(QUICK)
                .addAllHitsData(ImmutableList.of(16, 33, 51))
                .setNpRate(0.0054)
                .setCriticalStarGen(0.11)
                .build();
        final CommandCardData artsCard = CommandCardData.newBuilder()
                .setCommandCardType(ARTS)
                .addAllHitsData(ImmutableList.of(16, 33, 51))
                .setNpRate(0.0054)
                .setCriticalStarGen(0.11)
                .build();
        final CommandCardData busterCard = CommandCardData.newBuilder()
                .setCommandCardType(BUSTER)
                .addAllHitsData(ImmutableList.of(16, 33, 51))
                .setNpRate(0.0054)
                .setCriticalStarGen(0.11)
                .build();
        final CommandCardData extraCard = CommandCardData.newBuilder()
                .setCommandCardType(EXTRA)
                .addAllHitsData(ImmutableList.of(6, 13, 20, 26, 35))
                .setNpRate(0.0054)
                .setCriticalStarGen(0.11)
                .build();

        final ActiveSkillData activeSkillData11 = ActiveSkillData.newBuilder()
                .setBaseCoolDown(8)
                .addEffects(
                        EffectData.newBuilder()
                                .setType(GrantBuff.class.getSimpleName())
                                .setTarget(ALL_ALLIES)
                                .addBuffData(
                                        BuffData.newBuilder()
                                                .setType(AttackBuff.class.getSimpleName())
                                                .addAllValues(generateSkillValues(0.1, 0.01))
                                                .setNumTurnsActive(3)
                                )
                )
                .addEffects(
                        EffectData.newBuilder()
                                .setType(NpChange.class.getSimpleName())
                                .setTarget(ALL_ALLIES)
                                .addAllValues(generateSkillValues(0.2, 0.01))
                )
                .build();
        final ActiveSkillUpgrades activeSkillUpgrades1 = ActiveSkillUpgrades.newBuilder()
                .addActiveSkillData(activeSkillData11)
                .build();

        final ActiveSkillData activeSkillData21 = ActiveSkillData.newBuilder()
                .setBaseCoolDown(7)
                .addEffects(
                        EffectData.newBuilder()
                                .setType(NpChange.class.getSimpleName())
                                .setTarget(TARGETED_ALLY)
                                .addAllValues(generateSkillValues(0.1, 0.01))
                )
                .addEffects(
                        EffectData.newBuilder()
                                .setType(GrantBuff.class.getSimpleName())
                                .setTarget(ALL_ALLIES)
                                .addBuffData(
                                        BuffData.newBuilder()
                                                .setType(NpGenerationBuff.class.getSimpleName())
                                                .addAllValues(generateSkillValues(0.2, 0.01))
                                                .setNumTurnsActive(3)
                                )
                )
                .build();
        final ActiveSkillUpgrades activeSkillUpgrades2 = ActiveSkillUpgrades.newBuilder()
                .addActiveSkillData(activeSkillData21)
                .build();

        final ActiveSkillData activeSkillData31 = ActiveSkillData.newBuilder()
                .setBaseCoolDown(8)
                .addEffects(
                        EffectData.newBuilder()
                                .setType(GrantBuff.class.getSimpleName())
                                .setTarget(TARGETED_ALLY)
                                .addBuffData(
                                        BuffData.newBuilder()
                                                .setType(CommandCardBuff.class.getSimpleName())
                                                .addAllValues(generateSkillValues(0.3, 0.02))
                                                .setNumTurnsActive(3)
                                                .setApplyCondition(
                                                        ConditionData.newBuilder()
                                                                .setType(CardTypeEquals.class.getSimpleName())
                                                                .setValue(ARTS.name())
                                                )
                                )
                )
                .addEffects(
                        EffectData.newBuilder()
                                .setType(GrantBuff.class.getSimpleName())
                                .setTarget(TARGETED_ALLY)
                                .addBuffData(
                                        BuffData.newBuilder()
                                                .setType(SpecificAttackBuff.class.getSimpleName())
                                                .addValues(0.1)
                                                .setNumTurnsActive(3)
                                                .setApplyCondition(
                                                        ConditionData.newBuilder()
                                                                .setType(TargetsHaveTrait.class.getSimpleName())
                                                                .setTarget(DEFENDER)
                                                                .setValue(Traits.THREAT_TO_HUMANITY.name())
                                                )
                                )
                )
                .addEffects(
                        EffectData.newBuilder()
                                .setType(GrantBuff.class.getSimpleName())
                                .setTarget(TARGETED_ALLY)
                                .addBuffData(
                                        BuffData.newBuilder()
                                                .setType(Invincible.class.getSimpleName())
                                                .setNumTurnsActive(1)
                                )
                )
                .build();
        final ActiveSkillUpgrades activeSkillUpgrades3 = ActiveSkillUpgrades.newBuilder()
                .addActiveSkillData(activeSkillData31)
                .build();

        final NoblePhantasmData noblePhantasmData1 = NoblePhantasmData.newBuilder()
                .setCommandCardData(CommandCardData.newBuilder().setCommandCardType(ARTS))
                .setNoblePhantasmType(NON_DAMAGE)
                .addEffects(
                        EffectData.newBuilder()
                                .setType(GrantBuff.class.getSimpleName())
                                .setTarget(ALL_ALLIES)
                                .addBuffData(
                                        BuffData.newBuilder()
                                                .setType(AttackBuff.class.getSimpleName())
                                                .addAllValues(ImmutableList.of(0.3, 0.4, 0.45, 0.475, 0.5))
                                                .setNumTurnsActive(3)
                                )
                )
                .addEffects(
                        EffectData.newBuilder()
                                .setType(RemoveBuff.class.getSimpleName())
                                .setTarget(ALL_ALLIES)
                                .setApplyCondition(
                                        ConditionData.newBuilder()
                                                .setType(BuffHasTrait.class.getSimpleName())
                                                .setValue(NEGATIVE_BUFF.name())
                                )
                )
                .addEffects(
                        EffectData.newBuilder()
                                .setType(GrantBuff.class.getSimpleName())
                                .setTarget(ALL_ALLIES)
                                .setIsOverchargedEffect(true)
                                .addBuffData(
                                        BuffData.newBuilder()
                                                .setType(SpecialInvincible.class.getSimpleName())
                                                .setNumTimesActive(1)
                                                .setNumTurnsActive(3)
                                )
                                .addBuffData(
                                        BuffData.newBuilder()
                                                .setType(SpecialInvincible.class.getSimpleName())
                                                .setNumTimesActive(2)
                                                .setNumTurnsActive(3)
                                )
                                .addBuffData(
                                        BuffData.newBuilder()
                                                .setType(SpecialInvincible.class.getSimpleName())
                                                .setNumTimesActive(3)
                                                .setNumTurnsActive(3)
                                )
                                .addBuffData(
                                        BuffData.newBuilder()
                                                .setType(SpecialInvincible.class.getSimpleName())
                                                .setNumTimesActive(4)
                                                .setNumTurnsActive(3)
                                )
                                .addBuffData(
                                        BuffData.newBuilder()
                                                .setType(SpecialInvincible.class.getSimpleName())
                                                .setNumTimesActive(5)
                                                .setNumTurnsActive(3)
                                )
                )
                .build();
        final NoblePhantasmUpgrades noblePhantasmUpgrades = NoblePhantasmUpgrades.newBuilder()
                .addNoblePhantasmData(noblePhantasmData1)
                .build();

        final ServantAscensionData.Builder commonAscensionData = ServantAscensionData.newBuilder()
                .setDefenseNpRate(0.03)
                .setCriticalStarWeight(29)
                .setCost(16)
                .addCommandCardData(quickCard)
                .addCommandCardData(artsCard)
                .addCommandCardData(artsCard)
                .addCommandCardData(artsCard)
                .addCommandCardData(busterCard)
                .setExtraCard(extraCard)
                .addActiveSkillUpgrades(activeSkillUpgrades1)
                .addActiveSkillUpgrades(activeSkillUpgrades2)
                .addActiveSkillUpgrades(activeSkillUpgrades3)
                .setNoblePhantasmUpgrades(noblePhantasmUpgrades)
                .addPassiveSkillData(
                        PassiveSkillData.newBuilder()
                                .addEffects(
                                        EffectData.newBuilder()
                                                .setType(GrantBuff.class.getSimpleName())
                                                .setTarget(SELF)
                                                .addBuffData(
                                                        BuffData.newBuilder()
                                                                .setType(DebuffResist.class.getSimpleName())
                                                                .addValues(0.2))
                                )
                )
                .addPassiveSkillData(
                        PassiveSkillData.newBuilder()
                                .addEffects(
                                        EffectData.newBuilder()
                                                .setType(GrantBuff.class.getSimpleName())
                                                .setTarget(SELF)
                                                .addBuffData(
                                                        BuffData.newBuilder()
                                                                .setType(CommandCardBuff.class.getSimpleName())
                                                                .addValues(0.12)
                                                                .setApplyCondition(
                                                                        ConditionData.newBuilder()
                                                                                .setType(CardTypeEquals.class.getSimpleName())
                                                                                .setValue(ARTS.name())
                                                                )
                                                )
                                )
                )
                .addPassiveSkillData(
                        PassiveSkillData.newBuilder()
                                .addEffects(
                                        EffectData.newBuilder()
                                                .setType(GrantBuff.class.getSimpleName())
                                                .setTarget(SELF)
                                                .addBuffData(
                                                        BuffData.newBuilder()
                                                                .setType(CriticalDamageBuff.class.getSimpleName())
                                                                .addValues(0.08)
                                                                .setApplyCondition(
                                                                        ConditionData.newBuilder()
                                                                                .setType(CardTypeEquals.class.getSimpleName())
                                                                                .setValue(ARTS.name())
                                                                )
                                                )
                                )
                )
                .addAppendSkillData(
                        AppendSkillData.newBuilder()
                                .addEffects(
                                        EffectData.newBuilder()
                                                .setType(GrantBuff.class.getSimpleName())
                                                .setTarget(SELF)
                                                .addBuffData(
                                                        BuffData.newBuilder()
                                                                .setType(CommandCardBuff.class.getSimpleName())
                                                                .addAllValues(generateSkillValues(0.3, 0.02))
                                                                .setApplyCondition(
                                                                        ConditionData.newBuilder()
                                                                                .setType(CardTypeEquals.class.getSimpleName())
                                                                                .setValue(EXTRA.name())
                                                                )
                                                )
                                )
                )
                .addAppendSkillData(
                        AppendSkillData.newBuilder()
                                .addEffects(
                                        EffectData.newBuilder()
                                                .setType(NpChange.class.getSimpleName())
                                                .setTarget(SELF)
                                                .addAllValues(generateSkillValues(0.1, 0.01))
                                )
                )
                .addAppendSkillData(
                        AppendSkillData.newBuilder()
                                .addEffects(
                                        EffectData.newBuilder()
                                                .setType(GrantBuff.class.getSimpleName())
                                                .setTarget(SELF)
                                                .addBuffData(
                                                        BuffData.newBuilder()
                                                                .setType(AttackBuff.class.getSimpleName())
                                                                .addAllValues(generateSkillValues(0.2, 0.01))
                                                                .setApplyCondition(
                                                                        ConditionData.newBuilder()
                                                                                .setType(TargetsHaveClass.class.getSimpleName())
                                                                                .setTarget(DEFENDER)
                                                                                .setValue(SABER.name())
                                                                )
                                                )
                                )
                )
                .mergeFrom(status.build());
        final ServantAscensionData firstAscension = commonAscensionData.setCombatantData(firstAscensionCombatantData)
                .build();
        final ServantAscensionData secondAscension = commonAscensionData.setCombatantData(secondAscensionCombatantData)
                .addPassiveSkillData(
                        PassiveSkillData.newBuilder()
                                .addEffects(
                                        EffectData.newBuilder()
                                                .setType(GrantBuff.class.getSimpleName())
                                                .setTarget(SELF)
                                                .addBuffData(
                                                        BuffData.newBuilder()
                                                                .setType(CriticalChanceResist.class.getSimpleName())
                                                                .addValues(0.2)
                                                )
                                )
                )
                .build();

        final ServantData servantData = ServantData.newBuilder()
                .addServantAscensionData(firstAscension)
                .addServantAscensionData(secondAscension)
                .build();

        writeServant(servantData);
    }
}
