package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.SpecialActivationParams;
import yome.fgo.simulator.models.Simulation;

import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.SpecialActivationTarget.CARD_TYPE;

@SuperBuilder
public class CardTypeChangeSelect extends GrantBuff {
    private CommandCardType selectedCardType;
    private List<CommandCardType> allowedCardTypes;

    @Override
    protected BuffData getBuffData(final int level) {
        final BuffData buffDataToUse = isBuffOvercharged ?
                buffData.get(level - 1) :
                buffData.get(0);
        return buffDataToUse.toBuilder().setStringValue(selectedCardType.name()).build();
    }

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        final SpecialActivationParams specialActivationParams = SpecialActivationParams.newBuilder()
                .setSpecialTarget(CARD_TYPE)
                .addAllCardTypeSelections(allowedCardTypes)
                .build();
        simulation.requestSpecialActivationTarget(specialActivationParams);
        selectedCardType = simulation.selectCommandCardType();
        super.internalApply(simulation, level);
    }
}
