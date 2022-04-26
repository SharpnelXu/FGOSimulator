package yome.fgo.simulator.models.effects;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
public abstract class IntValuedEffect extends Effect {
    @Builder.Default
    protected final List<Integer> values = Lists.newArrayList(0);
}
