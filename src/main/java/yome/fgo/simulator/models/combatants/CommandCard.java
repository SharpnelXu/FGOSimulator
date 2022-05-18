package yome.fgo.simulator.models.combatants;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.data.proto.FgoStorageData.CommandCardOption;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.CommandCodeData;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffFactory;
import yome.fgo.simulator.models.effects.buffs.EffectActivatingBuff;
import yome.fgo.simulator.models.effects.buffs.ValuedBuff;
import yome.fgo.simulator.utils.RoundUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class CommandCard {
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
            final CommandCodeData commandCodeData = ResourceManager.getCommandCodeData(commandCardOption.getCommandCode());
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

    public double applyBuff(final Simulation simulation, final Class<? extends ValuedBuff> buffClass) {
        double totalValue = 0;
        for (final Buff buff : commandCodeBuffs) {
            if (buffClass.isInstance(buff) && buff.shouldApply(simulation)) {
                totalValue += buffClass.cast(buff).getValue(simulation);
                // command code buffs should be permanent anyway so not setting applied
            }
        }
        return RoundUtils.roundNearest(totalValue);
    }

    // should only be called in damage step, so activator is already set
    public void activateEffectActivatingBuff(
            final Simulation simulation,
            final Class<? extends EffectActivatingBuff> buffClass
    ) {
        final List<EffectActivatingBuff> buffsToActivate = new ArrayList<>();
        for (final Buff buff : commandCodeBuffs) {
            if (buffClass.isInstance(buff)) {
                buffsToActivate.add((EffectActivatingBuff) buff);
            }
        }

        for (final EffectActivatingBuff buff : buffsToActivate) {
            if (buff.shouldApply(simulation)) {

                if (simulation.getStatsLogger() != null) {
                    simulation.getStatsLogger().logEffectActivatingBuff(commandCodeData.getId(), buffClass);
                }
                buff.activate(simulation);
            }
        }
    }

    public boolean consumeBuffIfExist(final Simulation simulation, final Class<? extends Buff> buffClass) {
        for (final Buff buff : commandCodeBuffs) {
            if (buffClass.isInstance(buff) && buff.shouldApply(simulation)) {
                return true;
            }
        }
        return false;
    }
}
