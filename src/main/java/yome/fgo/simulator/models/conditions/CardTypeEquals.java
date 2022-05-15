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
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@AllArgsConstructor(access = PRIVATE)
public class CardTypeEquals extends Condition {
    public static CardTypeEquals get(final CommandCardType commandCardType) {
        return CARD_TYPE_EQUALS.get(commandCardType);
    }

    private static final Map<CommandCardType, CardTypeEquals> CARD_TYPE_EQUALS = ImmutableMap.of(
            ANY, new CardTypeEquals(ANY),
            BUSTER, new CardTypeEquals(BUSTER),
            ARTS, new CardTypeEquals(ARTS),
            QUICK, new CardTypeEquals(QUICK),
            EXTRA, new CardTypeEquals(EXTRA)
    );
    private final CommandCardType commandCardType;
    @Override
    public boolean evaluate(final Simulation simulation) {
        return commandCardType == ANY || commandCardType == simulation.getCurrentCommandCard().getCommandCardType();
    }

    @Override
    public String toString() {
        return getTranslation(COMMAND_CARD_TYPE_SECTION, commandCardType.name());
    }
}
