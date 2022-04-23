package yome.fgo.simulator.models.craftessences;

import lombok.AllArgsConstructor;
import lombok.Getter;
import yome.fgo.simulator.models.effects.Effect;

import java.util.List;

@AllArgsConstructor
@Getter
public class CraftEssence {
    private int attack;
    private int hp;
    private List<Effect> effects;
}
