package yome.fgo.simulator.models.combatants;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.Attribute;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.GrantTrait;

import java.util.ArrayList;
import java.util.List;

import static yome.fgo.simulator.utils.FateClassUtils.getClassMaxNpGauge;

@NoArgsConstructor
@Getter
public class Combatant {
    // enemy only data
    private int maxNpGauge;
    private int currentNpGauge;
    private List<Integer> hpBars;
    private int currentHpBar;
    private int cumulativeTurnDamage;

    protected CombatantData combatantData;

    protected String id;
    protected int currentHp;
    protected List<Buff> buffs = new ArrayList<>();

    // for testing
    public Combatant(final String id) {
        this.id = id;
    }

    // for testing
    public Combatant(final String id, final List<Integer> hpBars) {
        if (hpBars.isEmpty()) {
            throw new IllegalArgumentException("Empty hpBars");
        }
        for (final int hpBar : hpBars) {
            if (hpBar <= 0) {
                throw new IllegalArgumentException("Invalid hpBar: " + hpBar + " in " + hpBars);
            }
        }

        this.id = id;
        this.hpBars = new ArrayList<>(hpBars);
        this.currentHp = this.hpBars.get(this.currentHpBar);
    }

    // for servant
    public Combatant(final String id, final CombatantData combatantData) {
        this.id = id;
        this.combatantData = combatantData;
    }

    public Combatant(final CombatantData combatantData, final EnemyData enemyData) {
        this(enemyData.getEnemyBaseId(), enemyData.getHpBarsList());

        if (enemyData.hasCombatantDataOverride()) {
            final CombatantData override = enemyData.getCombatantDataOverride();
            final CombatantData.Builder builder = combatantData.toBuilder();
            builder.mergeFrom(override);
            builder.clearAlignments();
            builder.addAllAlignments(override.getAlignmentsCount() == 0
                                             ? combatantData.getAlignmentsList()
                                             : override.getAlignmentsList());
            builder.clearTraits();
            builder.addAllTraits(override.getTraitsCount() == 0
                                         ? combatantData.getTraitsList()
                                         : override.getTraitsList());
            this.combatantData = builder.build();
        } else {
            this.combatantData = combatantData;
        }
        if (enemyData.getHasCustomMaxNpGauge()) {
            this.maxNpGauge = enemyData.getCustomMaxNpGauge();
        } else {
            this.maxNpGauge = getClassMaxNpGauge(this.combatantData.getFateClass());
        }
    }

    public void initiate(final Simulation simulation) {
    }

    public FateClass getFateClass() {
        return combatantData.getFateClass();
    }

    public Attribute getAttribute() {
        return combatantData.getAttribute();
    }

    public List<String> getAllTraits(final Simulation simulation) {
        final ImmutableList.Builder<String> allTraits = ImmutableList.builder();
        allTraits.addAll(combatantData.getTraitsList());
        for (final Buff buff : buffs) {
            if (buff instanceof GrantTrait && buff.shouldApply(simulation)) {
                allTraits.add(((GrantTrait) buff).getTrait());
                buff.applyOnce();
            }
        }

        return allTraits.build();
    }

    public boolean getUndeadNpCorrection() {
        return combatantData.getUndeadNpCorrection();
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public boolean hasNextHpBar() {
        return currentHpBar < hpBars.size() - 1;
    }

    public void addBuff(final Buff buff) {
        buffs.add(buff);
    }

    public double applyBuff(final Simulation simulation, final Class<? extends Buff> buffClass) {
        double totalValue = 0;
        for (final Buff buff : buffs) {
            if (buffClass.isInstance(buff) && buff.shouldApply(simulation)) {
                totalValue += buff.getValue();
                buff.applyOnce();
            }
        }
        return totalValue;
    }

    public boolean isBuggedOverkill() {
        return getCumulativeTurnDamage() > getCurrentHp() && !hasNextHpBar();
    }

    public void clearCumulativeTurnDamage() {
        cumulativeTurnDamage = 0;
    }

    public void enterField(final Simulation simulation) {
        // TODO: implement
    }

    public void leaveField(final Simulation simulation) {
        // TODO: implement
    }

    public void receiveDamage(final int damage) {
        currentHp -= damage;
    }

    public void addCumulativeTurnDamage(final int damage) {
        cumulativeTurnDamage += damage;
    }

    public boolean isAlreadyDead() {
        return currentHp <= 0;
    }

    public void hpBarBreak() {
        currentHpBar++;
        currentHp = hpBars.get(currentHpBar);
    }

    public boolean activateGuts(final Simulation simulation) {
        // TODO: implement
        return false;
    }

    public void endOfTurn(final Simulation simulation) {

    }

    public void changeNp(final double percentNpChange) {
//        final int gaugeChange = (int) Math.ceil(Math.abs(percentNpChange) / 50);
//        final int changeSign = (int) Math.signum(percentNpChange);
//        currentNpGauge += gaugeChange * changeSign;
    }
}
