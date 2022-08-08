package yome.fgo.simulator.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import yome.fgo.data.proto.FgoStorageData.FateClass;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static yome.fgo.simulator.gui.helpers.ComponentUtils.SPECIAL_INFO_BOX_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createBoldLabel;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.getClassIcon;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.FateClassUtils.ALL_CLASSES;
import static yome.fgo.simulator.utils.FateClassUtils.getActionCount;
import static yome.fgo.simulator.utils.FateClassUtils.getActionPriority;
import static yome.fgo.simulator.utils.FateClassUtils.getBaseDeathRate;
import static yome.fgo.simulator.utils.FateClassUtils.getBaseStarGen;
import static yome.fgo.simulator.utils.FateClassUtils.getBaseStarWeight;
import static yome.fgo.simulator.utils.FateClassUtils.getClassAdvantage;
import static yome.fgo.simulator.utils.FateClassUtils.getClassAttackCorrection;
import static yome.fgo.simulator.utils.FateClassUtils.getClassCritStarCorrection;
import static yome.fgo.simulator.utils.FateClassUtils.getClassMaxNpGauge;
import static yome.fgo.simulator.utils.FateClassUtils.getClassNpCorrection;

public class FateClassInfoVBox extends VBox {
    private final ImageView icon;
    private final Label name;
    private final Label damageCorrection;
    private final Label criticalStarWeight;
    private final Label deathRate;
    private final Label starGen;
    private final Label npCorrection;
    private final Label starCorrection;
    private final Label npGauge;
    private final Label actionCount;
    private final Label actionPriority;
    private final VBox atkClassAdvVBox;
    private final VBox defClassAdvVBox;

    public FateClassInfoVBox(final VBox contentVBox) {
        super();

        setSpacing(10);
        setAlignment(Pos.TOP_LEFT);
        setStyle(SPECIAL_INFO_BOX_STYLE);
        setPadding(new Insets(30));
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setEffect(new DropShadow());

        final Button closeButton = new Button(getTranslation(APPLICATION_SECTION, "Close"));
        closeButton.setOnAction(e -> {
            contentVBox.setDisable(false);
            setVisible(false);
        });
        final HBox closeHBox = new HBox();
        closeHBox.setAlignment(Pos.CENTER);
        closeHBox.getChildren().add(closeButton);
        getChildren().add(closeHBox);

        icon = new ImageView();
        icon.setFitWidth(80);
        icon.setFitHeight(80);

        name = new Label();
        name.setFont(new Font("Georgia", 50));

        final HBox nameHBox = new HBox(10);
        nameHBox.getChildren().addAll(icon, name);
        nameHBox.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(nameHBox);

        damageCorrection = new Label();
        criticalStarWeight = new Label();
        deathRate = new Label();
        starGen = new Label();
        npCorrection = new Label();
        starCorrection = new Label();
        npGauge = new Label();
        actionCount = new Label();
        actionPriority = new Label();
        atkClassAdvVBox = new VBox(10);
        defClassAdvVBox = new VBox(10);

        final HBox line1 = new HBox(10);

        line1.getChildren().addAll(
                createBoldLabel(getTranslation(APPLICATION_SECTION, "damageCorrection:")), damageCorrection,
                createBoldLabel(getTranslation(APPLICATION_SECTION, "criticalStarWeight:")), criticalStarWeight,
                createBoldLabel(getTranslation(APPLICATION_SECTION, "starGen:")), starGen,
                createBoldLabel(getTranslation(APPLICATION_SECTION, "deathRate:")), deathRate
        );

        final HBox line2 = new HBox(10);

        line2.getChildren().addAll(
                createBoldLabel(getTranslation(APPLICATION_SECTION, "npCorrection:")), npCorrection,
                createBoldLabel(getTranslation(APPLICATION_SECTION, "starCorrection:")), starCorrection,
                createBoldLabel(getTranslation(APPLICATION_SECTION, "npGauge:")), npGauge,
                createBoldLabel(getTranslation(APPLICATION_SECTION, "actionCount:")), actionCount,
                createBoldLabel(getTranslation(APPLICATION_SECTION, "actionPriority:")), actionPriority
        );
        getChildren().addAll(
                line1,
                line2,
                createBoldLabel(getTranslation(APPLICATION_SECTION, "Attack Class Advantages:")),
                atkClassAdvVBox,
                createBoldLabel(getTranslation(APPLICATION_SECTION, "Defense Class Advantages:")),
                defClassAdvVBox
        );
    }

    public void renderClass(final FateClass fateClass, final SimulationWindow simulationWindow) {
        icon.setImage(simulationWindow.getClassImage(getClassIcon(fateClass)));
        name.setText(getTranslation(CLASS_SECTION, fateClass.name()));
        damageCorrection.setText(Double.toString(getClassAttackCorrection(fateClass)));
        criticalStarWeight.setText(getBaseStarWeight(fateClass) + "%");
        starGen.setText(getBaseStarGen(fateClass) + "%");
        deathRate.setText(getBaseDeathRate(fateClass) + "%");
        npCorrection.setText(Double.toString(getClassNpCorrection(fateClass)));
        starCorrection.setText(((int) (getClassCritStarCorrection(fateClass) * 100)) + "%");
        npGauge.setText(Integer.toString(getClassMaxNpGauge(fateClass)));
        actionCount.setText(Integer.toString(getActionCount(fateClass)));
        actionPriority.setText(Integer.toString(getActionPriority(fateClass)));
        atkClassAdvVBox.getChildren().clear();
        defClassAdvVBox.getChildren().clear();

        final Map<Double, Set<FateClass>> atkAdvMap = new TreeMap<>(Collections.reverseOrder());
        final Map<Double, Set<FateClass>> defAdvMap = new TreeMap<>();
        for (final FateClass other : ALL_CLASSES) {
            final double atkAdv = getClassAdvantage(fateClass, other);
            if (!atkAdvMap.containsKey(atkAdv)) {
                atkAdvMap.put(atkAdv, new TreeSet<>());
            }
            atkAdvMap.get(atkAdv).add(other);

            final double defAdv = getClassAdvantage(other, fateClass);
            if (!defAdvMap.containsKey(defAdv)) {
                defAdvMap.put(defAdv, new TreeSet<>());
            }
            defAdvMap.get(defAdv).add(other);
        }

        fillAdvBox(atkClassAdvVBox, atkAdvMap);
        fillAdvBox(defClassAdvVBox, defAdvMap);
    }

    private static void fillAdvBox(final VBox advVBox, final Map<Double, Set<FateClass>> advMap) {
        for (final double adv : advMap.keySet()) {
            final HBox advHBox = new HBox(10);
            final Label advLabel = new Label("x" + adv + ":");
            if (adv > 1) {
                advLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold");
            } else if (adv < 1) {
                advLabel.setStyle("-fx-text-fill: #00afff; -fx-font-weight: bold");
            } else {
                advLabel.setStyle("-fx-text-fill: #737373; -fx-font-weight: bold");
            }
            final Label classes = new Label(
                    advMap.get(adv).stream()
                            .map(c -> getTranslation(CLASS_SECTION, c.name()))
                            .collect(Collectors.joining(", "))
            );
            advHBox.getChildren().addAll(advLabel, classes);
            advVBox.getChildren().add(advHBox);
        }
    }
}
