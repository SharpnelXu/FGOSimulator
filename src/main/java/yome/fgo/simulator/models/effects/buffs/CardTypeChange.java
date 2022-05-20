package yome.fgo.simulator.models.effects.buffs;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;

import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@SuperBuilder
@Getter
public class CardTypeChange extends Buff {
    private final CommandCardType commandCardType;

    @Override
    public boolean commonBuffCondition() {
        return true;
    }

    @Override
    public boolean commonDebuffCondition() {
        return false;
    }

    @Override
    protected boolean commonStackableCondition() {
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + ": " + getTranslation(COMMAND_CARD_TYPE_SECTION, commandCardType.name());
    }
}
