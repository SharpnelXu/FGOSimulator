package yome.fgo.simulator.gui.components;

import lombok.AllArgsConstructor;
import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.Target;

@AllArgsConstructor
@Getter
public class TargetWrapper {
    private final Target target;
    private final String translation;

    public String toString() {
        return translation;
    }
}
