package yome.fgo.simulator.models.conditions;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.simulator.models.Simulation;

import java.util.Map;

import static lombok.AccessLevel.PRIVATE;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ANY;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.EXTRA;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;

@AllArgsConstructor(access = PRIVATE)
public class CommandCardTypeEquals implements Condition {
    public static CommandCardTypeEquals get(final CommandCardType commandCardType) {
        return COMMAND_CARD_TYPE_EQUALS.get(commandCardType);
    }

    private static final Map<CommandCardType, CommandCardTypeEquals> COMMAND_CARD_TYPE_EQUALS = ImmutableMap.of(
            ANY, new CommandCardTypeEquals(ANY),
            BUSTER, new CommandCardTypeEquals(BUSTER),
            ARTS, new CommandCardTypeEquals(ARTS),
            QUICK, new CommandCardTypeEquals(QUICK),
            EXTRA, new CommandCardTypeEquals(EXTRA)
    );
    private final CommandCardType commandCardType;
    @Override
    public boolean evaluate(final Simulation simulation) {
        return commandCardType == ANY || commandCardType == simulation.getCurrentCommandCard().getCommandCardType();
    }
}
