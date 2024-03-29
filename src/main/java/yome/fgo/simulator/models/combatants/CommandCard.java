package yome.fgo.simulator.models.combatants;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.data.proto.FgoStorageData.CommandCardOption;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.CommandCodeData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffFactory;
import yome.fgo.simulator.models.effects.buffs.BuffType;
import yome.fgo.simulator.utils.RoundUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.EXTRA;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.simulator.ResourceManager.COMMAND_CODE_DATA_ANCHOR_MAP;

@AllArgsConstructor
@Getter
public class CommandCard {
    public static final CommandCard ENEMY_DEFAULT_QUICK = new CommandCard(QUICK, List.of(100), 0, 0);
    public static final CommandCard ENEMY_DEFAULT_ARTS = new CommandCard(ARTS, List.of(100), 0, 0);
    public static final CommandCard ENEMY_DEFAULT_BUSTER = new CommandCard(BUSTER, List.of(100), 0, 0);
    public static final CommandCard ENEMY_DEFAULT_EXTRA = new CommandCard(EXTRA, List.of(100), 0, 0);

    private final CommandCardData commandCardData;
    private final CommandCodeData commandCodeData;
    private final List<Buff> commandCodeBuffs;
    private final int commandCardStrengthen;

    public CommandCard() {
        commandCardData = null;
        commandCodeData = null;
        commandCodeBuffs = ImmutableList.of();
        commandCardStrengthen = 0;
    }

    public CommandCard(
            final CommandCardData commandCardData
    ) {
        this.commandCardData = commandCardData;
        this.commandCodeData = null;
        this.commandCodeBuffs = ImmutableList.of();
        this.commandCardStrengthen = 0;
    }

    public CommandCard(
            final CommandCardData commandCardData,
            final CommandCardOption commandCardOption
    ) {
        this.commandCardData = commandCardData;

        if (commandCardOption.getHasCommandCode()) {
            final int id = Integer.parseInt(commandCardOption.getCommandCode().split("commandCode")[1]);
            final CommandCodeData commandCodeData = COMMAND_CODE_DATA_ANCHOR_MAP.get(id).getCommandCodeData();
            this.commandCodeData = commandCodeData;
            this.commandCodeBuffs = commandCodeData.getBuffsList()
                    .stream()
                    .map(data -> BuffFactory.buildBuff(data, 1))
                    .collect(Collectors.toList());
        } else {
            this.commandCodeData = null;
            this.commandCodeBuffs = ImmutableList.of();
        }

        this.commandCardStrengthen = commandCardOption.getStrengthen();
    }

    public CommandCard(
            final CommandCardType commandCardType,
            final List<Integer> hitPercentages,
            final double npCharge,
            final double starGeneration
    ) {
        this(CommandCardData.newBuilder()
                     .setCommandCardType(commandCardType)
                     .addAllHitsData(hitPercentages)
                     .setNpRate(npCharge)
                     .setCriticalStarGen(starGeneration)
                     .build());
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

    public double applyBuff(final Simulation simulation, final BuffType buffType) {
        double totalValue = 0;
        for (final Buff buff : commandCodeBuffs) {
            if (buff.getBuffType() == buffType && buff.shouldApply(simulation)) {
                totalValue += buff.getValue(simulation);
                // command code buffs should be permanent anyway so not setting applied
            }
        }
        return RoundUtils.roundNearest(totalValue);
    }

    // should only be called in damage step, so activator is already set
    public void activateEffectActivatingBuff(
            final Simulation simulation,
            final BuffType buffType
    ) {
        final List<Buff> buffsToActivate = new ArrayList<>();
        for (final Buff buff : commandCodeBuffs) {
            if (buff.getBuffType() == buffType) {
                buffsToActivate.add(buff);
            }
        }

        for (final Buff buff : buffsToActivate) {
            if (buff.shouldApply(simulation)) {

                if (simulation.getStatsLogger() != null) {
                    simulation.getStatsLogger().logEffectActivatingBuff(commandCodeData.getId(), buffType);
                }
                buff.activate(simulation);
            }
        }
    }

    public boolean consumeBuffIfExist(final Simulation simulation, final BuffType buffType) {
        for (final Buff buff : commandCodeBuffs) {
            if (buff.getBuffType() == buffType && buff.shouldApply(simulation)) {
                return true;
            }
        }
        return false;
    }
}
