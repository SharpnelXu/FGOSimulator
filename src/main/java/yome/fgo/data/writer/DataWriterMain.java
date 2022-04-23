package yome.fgo.data.writer;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillData;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillUpgrades;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.data.proto.FgoStorageData.EffectData;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.simulator.models.effects.EffectString;
import yome.fgo.simulator.models.traits.Traits;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Arrays;

import static yome.fgo.data.proto.FgoStorageData.Alignment.EVIL;
import static yome.fgo.data.proto.FgoStorageData.Alignment.NEUTRAL;
import static yome.fgo.data.proto.FgoStorageData.Attribute.SKY;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.EXTRA;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_ENEMIES;
import static yome.fgo.data.proto.FgoStorageData.Target.DEFENDER;
import static yome.fgo.data.proto.FgoStorageData.Target.SELF;
import static yome.fgo.data.proto.FgoStorageData.FateClass.AVENGER;
import static yome.fgo.data.proto.FgoStorageData.Gender.FEMALE;

public class DataWriterMain {
    public static final String ENEMY_DIRECTORY_PATH = System.getProperty("user.dir") + "/enemies";
    public static final String SERVANT_DIRECTORY_PATH = System.getProperty("user.dir") + "/servants";

    public static void main(String[] args) {
//        final String levelStatusData = "{\"lv\": 1, \"HP\": 1729, \"ATK\": 2042},{\"lv\": 2, \"HP\": 1900, \"ATK\": 2231},{\"lv\": 3, \"HP\": 2081, \"ATK\": 2432},{\"lv\": 4, \"HP\": 2252, \"ATK\": 2622},{\"lv\": 5, \"HP\": 2433, \"ATK\": 2823},{\"lv\": 6, \"HP\": 2604, \"ATK\": 3013},{\"lv\": 7, \"HP\": 2785, \"ATK\": 3214},{\"lv\": 8, \"HP\": 2956, \"ATK\": 3404},{\"lv\": 9, \"HP\": 3127, \"ATK\": 3594},{\"lv\": 10, \"HP\": 3298, \"ATK\": 3784},{\"lv\": 11, \"HP\": 3459, \"ATK\": 3963},{\"lv\": 12, \"HP\": 3630, \"ATK\": 4153},{\"lv\": 13, \"HP\": 3791, \"ATK\": 4332},{\"lv\": 14, \"HP\": 3952, \"ATK\": 4510},{\"lv\": 15, \"HP\": 4113, \"ATK\": 4689},{\"lv\": 16, \"HP\": 4264, \"ATK\": 4857},{\"lv\": 17, \"HP\": 4415, \"ATK\": 5024},{\"lv\": 18, \"HP\": 4566, \"ATK\": 5192},{\"lv\": 19, \"HP\": 4707, \"ATK\": 5348},{\"lv\": 20, \"HP\": 4847, \"ATK\": 5505},{\"lv\": 21, \"HP\": 4988, \"ATK\": 5661},{\"lv\": 22, \"HP\": 5119, \"ATK\": 5806},{\"lv\": 23, \"HP\": 5250, \"ATK\": 5951},{\"lv\": 24, \"HP\": 5371, \"ATK\": 6085},{\"lv\": 25, \"HP\": 5491, \"ATK\": 6219},{\"lv\": 26, \"HP\": 5612, \"ATK\": 6354},{\"lv\": 27, \"HP\": 5723, \"ATK\": 6476},{\"lv\": 28, \"HP\": 5823, \"ATK\": 6588},{\"lv\": 29, \"HP\": 5924, \"ATK\": 6700},{\"lv\": 30, \"HP\": 6025, \"ATK\": 6812},{\"lv\": 31, \"HP\": 6105, \"ATK\": 6901},{\"lv\": 32, \"HP\": 6196, \"ATK\": 7001},{\"lv\": 33, \"HP\": 6276, \"ATK\": 7091},{\"lv\": 34, \"HP\": 6346, \"ATK\": 7169},{\"lv\": 35, \"HP\": 6417, \"ATK\": 7247},{\"lv\": 36, \"HP\": 6477, \"ATK\": 7314},{\"lv\": 37, \"HP\": 6528, \"ATK\": 7370},{\"lv\": 38, \"HP\": 6578, \"ATK\": 7426},{\"lv\": 39, \"HP\": 6618, \"ATK\": 7471},{\"lv\": 40, \"HP\": 6658, \"ATK\": 7515},{\"lv\": 41, \"HP\": 6689, \"ATK\": 7549},{\"lv\": 42, \"HP\": 6719, \"ATK\": 7582},{\"lv\": 43, \"HP\": 6739, \"ATK\": 7605},{\"lv\": 44, \"HP\": 6749, \"ATK\": 7616},{\"lv\": 45, \"HP\": 6759, \"ATK\": 7627},{\"lv\": 46, \"HP\": 6769, \"ATK\": 7638},{\"lv\": 47, \"HP\": 6779, \"ATK\": 7649},{\"lv\": 48, \"HP\": 6789, \"ATK\": 7661},{\"lv\": 49, \"HP\": 6799, \"ATK\": 7672},{\"lv\": 50, \"HP\": 6819, \"ATK\": 7694},{\"lv\": 51, \"HP\": 6850, \"ATK\": 7728},{\"lv\": 52, \"HP\": 6890, \"ATK\": 7772},{\"lv\": 53, \"HP\": 6930, \"ATK\": 7817},{\"lv\": 54, \"HP\": 6980, \"ATK\": 7873},{\"lv\": 55, \"HP\": 7031, \"ATK\": 7929},{\"lv\": 56, \"HP\": 7091, \"ATK\": 7996},{\"lv\": 57, \"HP\": 7161, \"ATK\": 8074},{\"lv\": 58, \"HP\": 7232, \"ATK\": 8152},{\"lv\": 59, \"HP\": 7312, \"ATK\": 8241},{\"lv\": 60, \"HP\": 7403, \"ATK\": 8342},{\"lv\": 61, \"HP\": 7483, \"ATK\": 8431},{\"lv\": 62, \"HP\": 7584, \"ATK\": 8543},{\"lv\": 63, \"HP\": 7685, \"ATK\": 8655},{\"lv\": 64, \"HP\": 7785, \"ATK\": 8766},{\"lv\": 65, \"HP\": 7896, \"ATK\": 8889},{\"lv\": 66, \"HP\": 8017, \"ATK\": 9023},{\"lv\": 67, \"HP\": 8137, \"ATK\": 9157},{\"lv\": 68, \"HP\": 8258, \"ATK\": 9291},{\"lv\": 69, \"HP\": 8389, \"ATK\": 9437},{\"lv\": 70, \"HP\": 8520, \"ATK\": 9582},{\"lv\": 71, \"HP\": 8661, \"ATK\": 9738},{\"lv\": 72, \"HP\": 8801, \"ATK\": 9895},{\"lv\": 73, \"HP\": 8942, \"ATK\": 10051},{\"lv\": 74, \"HP\": 9093, \"ATK\": 10219},{\"lv\": 75, \"HP\": 9244, \"ATK\": 10386},{\"lv\": 76, \"HP\": 9395, \"ATK\": 10554},{\"lv\": 77, \"HP\": 9556, \"ATK\": 10733},{\"lv\": 78, \"HP\": 9717, \"ATK\": 10911},{\"lv\": 79, \"HP\": 9878, \"ATK\": 11090},{\"lv\": 80, \"HP\": 10049, \"ATK\": 11280},{\"lv\": 81, \"HP\": 10210, \"ATK\": 11459},{\"lv\": 82, \"HP\": 10381, \"ATK\": 11649},{\"lv\": 83, \"HP\": 10552, \"ATK\": 11838},{\"lv\": 84, \"HP\": 10723, \"ATK\": 12028},{\"lv\": 85, \"HP\": 10904, \"ATK\": 12229},{\"lv\": 86, \"HP\": 11075, \"ATK\": 12419},{\"lv\": 87, \"HP\": 11256, \"ATK\": 12620},{\"lv\": 88, \"HP\": 11427, \"ATK\": 12810},{\"lv\": 89, \"HP\": 11608, \"ATK\": 13011},{\"lv\": 90, \"HP\": 11790, \"ATK\": 13213},{\"lv\": 91, \"HP\": 11900, \"ATK\": 13335},{\"lv\": 92, \"HP\": 12011, \"ATK\": 13458},{\"lv\": 93, \"HP\": 12122, \"ATK\": 13581},{\"lv\": 94, \"HP\": 12232, \"ATK\": 13704},{\"lv\": 95, \"HP\": 12353, \"ATK\": 13838},{\"lv\": 96, \"HP\": 12464, \"ATK\": 13961},{\"lv\": 97, \"HP\": 12574, \"ATK\": 14084},{\"lv\": 98, \"HP\": 12685, \"ATK\": 14207},{\"lv\": 99, \"HP\": 12806, \"ATK\": 14341},{\"lv\": 100, \"HP\": 12916, \"ATK\": 14464},{\"lv\": 101, \"HP\": 13027, \"ATK\": 14587},{\"lv\": 102, \"HP\": 13138, \"ATK\": 14709},{\"lv\": 103, \"HP\": 13258, \"ATK\": 14843},{\"lv\": 104, \"HP\": 13369, \"ATK\": 14966},{\"lv\": 105, \"HP\": 13480, \"ATK\": 15089},{\"lv\": 106, \"HP\": 13590, \"ATK\": 15212},{\"lv\": 107, \"HP\": 13711, \"ATK\": 15346},{\"lv\": 108, \"HP\": 13822, \"ATK\": 15469},{\"lv\": 109, \"HP\": 13932, \"ATK\": 15592},{\"lv\": 110, \"HP\": 14043, \"ATK\": 15715},{\"lv\": 111, \"HP\": 14154, \"ATK\": 15838},{\"lv\": 112, \"HP\": 14275, \"ATK\": 15972},{\"lv\": 113, \"HP\": 14385, \"ATK\": 16095},{\"lv\": 114, \"HP\": 14496, \"ATK\": 16217},{\"lv\": 115, \"HP\": 14607, \"ATK\": 16340},{\"lv\": 116, \"HP\": 14727, \"ATK\": 16474},{\"lv\": 117, \"HP\": 14838, \"ATK\": 16597},{\"lv\": 118, \"HP\": 14949, \"ATK\": 16720},{\"lv\": 119, \"HP\": 15059, \"ATK\": 16843},{\"lv\": 120, \"HP\": 15180, \"ATK\": 16977}";
//        final ServantAscensionData.Builder test = ServantAscensionData.newBuilder();
//        try {
//            JsonFormat.parser()
//                    .merge(new StringReader("{\"servantStatusData\":[" + levelStatusData + "]}"), test);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        final String id = "servant321";
//        final CombatantData.Builder commonCombatantData = CombatantData.newBuilder()
//                .setId(id)
//                .setRarity(5)
//                .setFateClass(AVENGER)
//                .setGender(FEMALE)
//                .addAlignments(NEUTRAL)
//                .addAlignments(EVIL)
//                .setAttribute(SKY)
//                .addTraits(Traits.DEMONIC)
//                .addTraits(Traits.DIVINE)
//                .addTraits(Traits.HOMINIDAE_SERVANT)
//                .addTraits(Traits.HUMANOID)
//                .addTraits(Traits.PSEUDO_SERVANT)
//                .addTraits(Traits.RIDING)
//                .addTraits(Traits.WEAK_TO_ENUMA_ELISH)
//                .setDeathRate(5);
//        final CombatantData secondAscensionData = commonCombatantData.build();
//        final CombatantData firstAscensionData = commonCombatantData.addTraits(Traits.CHILD_SERVANT).build();
//
//        final CommandCardData quickCard = CommandCardData.newBuilder()
//                .setCommandCardType(QUICK)
//                .addAllHitsData(ImmutableList.of(10, 20, 30, 40))
//                .setNpRate(0.52)
//                .setCriticalStarGen(6.1)
//                .build();
//        final CommandCardData artsCard = CommandCardData.newBuilder()
//                .setCommandCardType(ARTS)
//                .addAllHitsData(ImmutableList.of(16, 33, 51))
//                .setNpRate(0.52)
//                .setCriticalStarGen(6.1)
//                .build();
//        final CommandCardData busterCard = CommandCardData.newBuilder()
//                .setCommandCardType(BUSTER)
//                .addAllHitsData(ImmutableList.of(16, 33, 51))
//                .setNpRate(0.52)
//                .setCriticalStarGen(6.1)
//                .build();
//        final ActiveSkillData activeSkill1Upgrade1 = ActiveSkillData.newBuilder()
//                .setBaseCoolDown(8)
//                .addEffects(EffectData.newBuilder()
//                                    .setType(EffectString.ARTS_CHANGE)
//                                    .setTarget(SELF)
//                                    .setNumTurns(3)
//                                    .addAllValues(Arrays.asList(
//                                            20.0,
//                                            21.0,
//                                            22.0,
//                                            23.0,
//                                            24.0,
//                                            25.0,
//                                            26.0,
//                                            27.0,
//                                            28.0,
//                                            30.0
//                                    )))
//                .addEffects(EffectData.newBuilder()
//                                    .setType(EffectString.EVADE)
//                                    .setTarget(SELF)
//                                    .setNumTimes(2)
//                                    .setNumTurns(3))
//                .build();
//        final ActiveSkillData activeSkill2Upgrade1 = ActiveSkillData.newBuilder()
//                .setBaseCoolDown(9)
//                .addEffects(EffectData.newBuilder()
//                                    .setType(EffectString.NP_DAMAGE_CHANGE)
//                                    .setTarget(SELF)
//                                    .setNumTurns(3)
//                                    .addAllValues(Arrays.asList(
//                                            10.0,
//                                            11.0,
//                                            12.0,
//                                            13.0,
//                                            14.0,
//                                            15.0,
//                                            16.0,
//                                            17.0,
//                                            18.0,
//                                            20.0
//                                    )))
//                .addEffects(EffectData.newBuilder()
//                                    .setType(EffectString.CHARM)
//                                    .setTarget(ALL_ENEMIES)
//                                    .setNumTurns(1)
//                                    .addAllProbabilities(Arrays.asList(
//                                            50.0,
//                                            55.0,
//                                            60.0,
//                                            65.0,
//                                            70.0,
//                                            75.0,
//                                            80.0,
//                                            85.0,
//                                            90.0,
//                                            100.0
//                                    )))
//                .build();
//        final ActiveSkillData activeSkill3Upgrade1 = ActiveSkillData.newBuilder()
//                .setBaseCoolDown(8)
//                .addEffects(EffectData.newBuilder()
//                                    .setType(EffectString.NP_CHANGE)
//                                    .setTarget(SELF)
//                                    .addAllValues(Arrays.asList(
//                                            30.0,
//                                            32.0,
//                                            34.0,
//                                            36.0,
//                                            38.0,
//                                            40.0,
//                                            42.0,
//                                            44.0,
//                                            46.0,
//                                            50.0
//                                    )))
//                .addEffects(EffectData.newBuilder()
//                                    .setType(EffectString.POST_NORMAL_ATTACK)
//                                    .setTarget(SELF)
//                                    .setNumTurns(3)
//                                    .addSubEffects(EffectData.newBuilder()
//                                                           .setType(EffectString.HEARTFELT_BLAZE)
//                                                           .setTarget(DEFENDER)
//                                                           .setNumTurns(3)))
//                .build();
//        final ServantAscensionData.Builder commonAscensionData = ServantAscensionData.newBuilder()
//                .setDefenseNpRate(5)
//                .setCriticalStarWeight(29)
//                .addCommandCardData(quickCard)
//                .addCommandCardData(quickCard)
//                .addCommandCardData(artsCard)
//                .addCommandCardData(artsCard)
//                .addCommandCardData(busterCard)
//                .addActiveSkillUpgrades(ActiveSkillUpgrades.newBuilder()
//                                            .addActiveSkillData(ActiveSkillData.newBuilder()
//                                                                            .addEffects(EffectData.newBuilder()
//                                                                                                .setType(EffectString.ARTS_CHANGE))))
//                .setExtraCard(CommandCardData.newBuilder()
//                                      .setCommandCardType(EXTRA)
//                                      .addAllHitsData(ImmutableList.of(6, 13, 20, 26, 35))
//                                      .setNpRate(0.52));
//        final ServantAscensionData firstAscension = commonAscensionData.setCombatantData(firstAscensionData).build();

        final ServantData test = ServantData.newBuilder()
                .addServantAscensionData(ServantAscensionData.newBuilder().setCombatantData(CombatantData.newBuilder().setId("test")).setCriticalStarWeight(50).build())
                .build();
        writeServant(test);
    }

    public static void writeEnemy(final CombatantData combatantData) {
        writeMessage(combatantData, ENEMY_DIRECTORY_PATH, combatantData.getId());
    }

    public static void writeServant(final ServantData servantData) {
        final String id = servantData.getServantAscensionData(0).getCombatantData().getId();
        writeMessage(servantData, SERVANT_DIRECTORY_PATH, id);
    }

    public static void writeMessage(final Message message, final String directoryPath, final String id) {
        final File newDirectory = new File(directoryPath + "/" + id);
        if (!newDirectory.exists()) {
            newDirectory.mkdir();
        }

        final File newFile = new File(directoryPath + "/" + id + "/" + id + ".json");
        final Printer printer = JsonFormat.printer();
        try (PrintStream printStream = new PrintStream(newFile)) {
            printStream.println(printer.print(message));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
