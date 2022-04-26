package yome.fgo.simulator.models.combatants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.data.proto.FgoStorageData.CommandCardOption;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class CommandCard {
    private final CommandCardData commandCardData;
    private final CommandCode commandCode;
    private final int commandCardStrengthen;

    public CommandCard(
            final CommandCardData commandCardData
    ) {
        this(commandCardData, null, 0);
    }

    public CommandCard(
            final CommandCardData commandCardData,
            final CommandCardOption commandCardOption
    ) {
        this(commandCardData, null, commandCardOption.getStrengthen());
    }

    public CommandCard(
            final CommandCardType commandCardType,
            final List<Integer> hitPercentages,
            final double npCharge,
            final double starGeneration
    ) {
        this(CommandCardData.newBuilder()
                     .setCommandCardType(commandCardType)
                     .addAllHitsData(hitPercentages)
                     .setNpRate(npCharge)
                     .setCriticalStarGen(starGeneration)
                     .build(), null, 0);
    }

    public double getCriticalStarGeneration() {
        return commandCardData.getCriticalStarGen();
    }

    public double getNpCharge() {
        return commandCardData.getNpRate();
    }

    public CommandCardType getCommandCardType() {
        return commandCardData.getCommandCardType();
    }

    public List<Integer> getHitPercentages() {
        return commandCardData.getHitsDataList();
    }

    public int getTotalHits() {
        return commandCardData.getHitsDataList().stream().mapToInt(i -> i).sum();
    }
}
