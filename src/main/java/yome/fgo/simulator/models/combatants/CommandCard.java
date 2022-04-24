package yome.fgo.simulator.models.combatants;

import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.data.proto.FgoStorageData.CommandCardOption;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;

import java.util.List;

public class CommandCard {

    public CommandCardData commandCardData;
    public CommandCode commandCode;
    public int commandCardStrengthen;

    public CommandCard(
            CommandCardData commandCardData
    ) {
        this.commandCardData = commandCardData;
    }

    public CommandCard(
            CommandCardData commandCardData,
            CommandCardOption commandCardOption
    ) {
        this(commandCardData);
        this.commandCardStrengthen = commandCardOption.getStrengthen();
    }

    public CommandCard(
            CommandCardType commandCardType,
            List<Integer> hitPercentages,
            double npCharge,
            double starGeneration
    ) {
        this.commandCardData = CommandCardData.newBuilder()
                     .setCommandCardType(commandCardType)
                     .addAllHitsData(hitPercentages)
                     .setNpRate(npCharge)
                     .setCriticalStarGen(starGeneration)
                     .build();
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
