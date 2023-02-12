package yome.fgo.simulator.models.combatants;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EnemyAction {
    List<Integer> targetHits;
    boolean isNp;
    CommandCardType commandCardType;
    int attack;
}
