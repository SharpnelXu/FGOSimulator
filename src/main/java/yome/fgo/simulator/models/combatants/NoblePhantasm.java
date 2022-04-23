package yome.fgo.simulator.models.combatants;

import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmData;
import yome.fgo.simulator.models.effects.Effect;

import java.util.ArrayList;
import java.util.List;

@Getter
public class NoblePhantasm extends CommandCard {
    private final List<Effect> effects;

    public NoblePhantasm(final NoblePhantasmData noblePhantasmData, final int level) {
        super(noblePhantasmData.getCommandCardData());
        this.effects = new ArrayList<>();
    }
}
