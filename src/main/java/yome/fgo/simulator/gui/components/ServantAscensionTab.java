package yome.fgo.simulator.gui.components;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.util.JsonFormat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillData;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillUpgrades;
import yome.fgo.data.proto.FgoStorageData.Alignment;
import yome.fgo.data.proto.FgoStorageData.AppendSkillData;
import yome.fgo.data.proto.FgoStorageData.Attribute;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmData;
import yome.fgo.data.proto.FgoStorageData.NoblePhantasmUpgrades;
import yome.fgo.data.proto.FgoStorageData.PassiveSkillData;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.data.proto.FgoStorageData.Status;
import yome.fgo.simulator.translation.TranslationManager;
import yome.fgo.simulator.utils.RoundUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.simulator.ResourceManager.getServantThumbnail;
import static yome.fgo.simulator.gui.helpers.DataPrinter.doubleToString;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.COMMA_SPLIT_REGEX;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SERVANT_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.UNIT_THUMBNAIL_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.addSplitTraitListener;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createInfoImageView;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.fillAttribute;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.fillFateClass;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.fillGender;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.wrapInAnchor;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getKeyForTrait;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class ServantAscensionTab extends ScrollPane {
    private final ImageView thumbnail;
    private final ChoiceBox<Integer> rarityChoices;
    private final ChoiceBox<FateClass> classChoices;
    private final ChoiceBox<Gender> genderChoices;
    private final ChoiceBox<Attribute> attributeChoices;
    private final List<CheckBox> alignmentChecks;
    private final TextField defNpText;
    private final TextField critStarWeightText;
    private final TextField deathRateText;
    private final TextField costText;
    private final TextField traitsText;
    private final Label errorLabel;
    private final List<CommandCardBox> commandCardBoxes;
    private final CommandCardBox exCommandCardBox;
    private final TabPane npUpgradesTabs;
    private final List<TabPane> activeSkillUpgradeTabPanes;
    private final VBox passiveSkillsVBox;
    private final VBox appendSkillsVBox;
    private final TextArea servantStatus;
    private int ascension;

    public ServantAscensionTab(final int servantNo, final int ascension, final Label errorLabel) {
        super();
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.ALWAYS);
        setFitToWidth(true);
        setPadding(new Insets(10));

        this.errorLabel = errorLabel;
        this.ascension = ascension;

        final VBox rootVBox = new VBox(10);
        setContent(rootVBox);
        final List<Node> nodes = rootVBox.getChildren();

        final HBox baseDataBox = new HBox(10);
        baseDataBox.setFillHeight(false);
        baseDataBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        final String servantId = "servant" + servantNo;
        final File thumbnailFile = getServantThumbnail(servantId, ascension);
        Image image = null;
        try {
            image = new Image(new FileInputStream(thumbnailFile));
        } catch (final FileNotFoundException ignored) {
        }
        thumbnail = new ImageView();
        if (image != null) {
            thumbnail.setImage(image);
        }
        thumbnail.setFitWidth(SERVANT_THUMBNAIL_SIZE);
        thumbnail.setFitHeight(SERVANT_THUMBNAIL_SIZE);
        final AnchorPane imgAnchor = wrapInAnchor(thumbnail);
        imgAnchor.setStyle(UNIT_THUMBNAIL_STYLE);

        baseDataBox.getChildren().addAll(imgAnchor);

        final VBox combatantDataVBox = new VBox(10);
        combatantDataVBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        HBox.setHgrow(combatantDataVBox, Priority.ALWAYS);

        final HBox choicesHBox = new HBox(10);
        choicesHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        choicesHBox.setAlignment(Pos.CENTER_LEFT);
        final Label rarityLabel = new Label(getTranslation(APPLICATION_SECTION, "Rarity"));
        rarityChoices = new ChoiceBox<>();
        rarityChoices.setItems(FXCollections.observableArrayList(5, 4, 3, 2, 1, 0));
        rarityChoices.getSelectionModel().selectFirst();
        final Label classLabel = new Label(getTranslation(APPLICATION_SECTION, "Class"));
        classChoices = new ChoiceBox<>();
        fillFateClass(classChoices);
        final Label genderLabel = new Label(getTranslation(APPLICATION_SECTION, "Gender"));
        genderChoices = new ChoiceBox<>();
        fillGender(genderChoices);
        final Label attributeLabel = new Label(getTranslation(APPLICATION_SECTION, "Attribute"));
        attributeChoices = new ChoiceBox<>();
        fillAttribute(attributeChoices);
        choicesHBox.getChildren().addAll(rarityLabel, rarityChoices, classLabel, classChoices, genderLabel, genderChoices, attributeLabel, attributeChoices);

        combatantDataVBox.getChildren().add(choicesHBox);

        final HBox alignmentHBox = new HBox(10);
        alignmentHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final Label alignmentLabel = new Label(getTranslation(APPLICATION_SECTION, "Alignments"));
        final VBox alignmentGroupVBox = new VBox(5);
        alignmentGroupVBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final HBox alignmentGroup1 = new HBox(10);
        alignmentGroup1.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        alignmentChecks = new ArrayList<>();
        final List<Alignment> firstAlignmentRow = ImmutableList.of(Alignment.LAWFUL, Alignment.NEUTRAL, Alignment.CHAOTIC);
        for (final Alignment alignment : firstAlignmentRow) {
            final CheckBox checkBox = new CheckBox(getTranslation(TRAIT_SECTION, alignment.name()));
            alignmentGroup1.getChildren().add(checkBox);
            alignmentChecks.add(checkBox);
        }
        final HBox alignmentGroup2 = new HBox(10);
        alignmentGroup2.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final List<Alignment> secondAlignmentRow = ImmutableList.of(
                Alignment.GOOD, Alignment.BALANCED, Alignment.EVIL, Alignment.INSANE, Alignment.SUMMER, Alignment.BRIDE
        );
        for (final Alignment alignment : secondAlignmentRow) {
            final CheckBox checkBox = new CheckBox(getTranslation(TRAIT_SECTION, alignment.name()));
            alignmentGroup2.getChildren().add(checkBox);
            alignmentChecks.add(checkBox);
        }
        alignmentGroupVBox.getChildren().addAll(alignmentGroup1, alignmentGroup2);
        alignmentHBox.getChildren().addAll(alignmentLabel, alignmentGroupVBox);

        combatantDataVBox.getChildren().add(alignmentHBox);

        final HBox textsHBox = new HBox(10);
        textsHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        textsHBox.setAlignment(Pos.CENTER_LEFT);
        final Label defNpRateLabel = new Label(getTranslation(APPLICATION_SECTION, "Def Np Rate (%)"));
        defNpText = new TextField();
        defNpText.setMaxWidth(65);
        final Label critStarWeightLabel = new Label(getTranslation(APPLICATION_SECTION, "Crit Star Weight"));
        critStarWeightText = new TextField();
        critStarWeightText.setMaxWidth(65);
        final Label deathRateLabel = new Label(getTranslation(APPLICATION_SECTION, "Death Rate (%)"));
        deathRateText = new TextField();
        deathRateText.setMaxWidth(65);
        final Label servantCostLabel = new Label(getTranslation(APPLICATION_SECTION, "Servant Cost"));
        costText = new TextField();
        costText.setMaxWidth(65);
        textsHBox.getChildren().addAll(defNpRateLabel, defNpText, critStarWeightLabel, critStarWeightText, deathRateLabel, deathRateText, servantCostLabel, costText);

        combatantDataVBox.getChildren().add(textsHBox);

        final HBox traitsHBox = new HBox(10);
        traitsHBox.setAlignment(Pos.CENTER_LEFT);
        traitsHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final Label traitsLabel = new Label(getTranslation(APPLICATION_SECTION, "Traits"));
        traitsText = new TextField();
        final AnchorPane traitsAnchor = wrapInAnchor(traitsText);
        HBox.setHgrow(traitsAnchor, Priority.ALWAYS);
        addSplitTraitListener(traitsText, errorLabel);
        traitsHBox.getChildren().addAll(traitsLabel, traitsAnchor);

        combatantDataVBox.getChildren().add(traitsHBox);

        baseDataBox.getChildren().add(combatantDataVBox);

        nodes.add(baseDataBox);
        nodes.add(new Separator());

        final Label cardsLabel = new Label(getTranslation(APPLICATION_SECTION, "Command Card"));
        cardsLabel.setFont(new Font(18));

        final HBox quickCardDataHBox = new HBox(10);
        quickCardDataHBox.setAlignment(Pos.CENTER_LEFT);
        final Label npRateLabel = new Label(getTranslation(APPLICATION_SECTION, "NP (%)"));
        final TextField npRateText = new TextField();
        final Label critStarRateLabel = new Label(getTranslation(APPLICATION_SECTION, "Crit Star Rate (%)"));
        final TextField critStarRateText = new TextField();
        final Button quickCardDataButton = new Button(getTranslation(APPLICATION_SECTION, "Autofill"));
        quickCardDataHBox.getChildren().addAll(cardsLabel, npRateLabel, npRateText, critStarRateLabel, critStarRateText, quickCardDataButton);
        nodes.add(quickCardDataHBox);

        commandCardBoxes = new ArrayList<>();
        for (int i = 1; i <= 5; i += 1) {
            final CommandCardBox commandCardBox = new CommandCardBox(i);
            commandCardBoxes.add(commandCardBox);
            HBox.setHgrow(commandCardBox, Priority.ALWAYS);
        }
        exCommandCardBox = new CommandCardBox(0);
        HBox.setHgrow(exCommandCardBox, Priority.ALWAYS);
        final HBox cardDataHBox1 = new HBox(5);
        cardDataHBox1.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        cardDataHBox1.getChildren().addAll(commandCardBoxes.get(0), commandCardBoxes.get(1), commandCardBoxes.get(2));
        final HBox cardDataHBox2 = new HBox(5);
        cardDataHBox2.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        cardDataHBox2.getChildren().addAll(commandCardBoxes.get(3), commandCardBoxes.get(4), exCommandCardBox);

        nodes.add(cardDataHBox1);
        nodes.add(cardDataHBox2);
        nodes.add(new Separator());

        final Label npLabel = new Label(getTranslation(APPLICATION_SECTION, "NP Card"));
        npLabel.setFont(new Font(18));

        final HBox npButtonsHBox = new HBox(10);
        npButtonsHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        npButtonsHBox.setAlignment(Pos.CENTER_LEFT);

        npUpgradesTabs = new TabPane(new Tab(getTranslation(APPLICATION_SECTION, "Base NP"), new NpUpgrade(errorLabel)));
        npUpgradesTabs.setStyle("-fx-border-color:grey");
        npUpgradesTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        final Button addNpUpgradeButton = new Button();
        addNpUpgradeButton.setGraphic(createInfoImageView("add"));
        addNpUpgradeButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Add NP Upgrade")));
        addNpUpgradeButton.setOnAction(e -> {
            final List<Tab> tabs = npUpgradesTabs.getTabs();
            if (tabs.isEmpty()) {
                tabs.add(new Tab(getTranslation(APPLICATION_SECTION, "Base NP"), new NpUpgrade(errorLabel)));
            } else {
                final NpUpgrade base = (NpUpgrade) npUpgradesTabs.getSelectionModel().getSelectedItem().getContent();
                final String tabName = getTranslation(APPLICATION_SECTION, "NP Upgrade") + " " + tabs.size();
                tabs.add(new Tab(tabName, new NpUpgrade(base, errorLabel)));
            }
            npUpgradesTabs.getSelectionModel().selectLast();
        });

        final Button leftNpUpgradeButton = new Button();
        leftNpUpgradeButton.setGraphic(createInfoImageView("left"));
        leftNpUpgradeButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Move item left")));
        leftNpUpgradeButton.setOnAction(e -> {
            final ObservableList<Tab> tabs = npUpgradesTabs.getTabs();
            if (tabs.isEmpty() || tabs.size() == 1) {
                return;
            }

            final int index = tabs.indexOf(npUpgradesTabs.getSelectionModel().getSelectedItem());
            if (index > 0) {
                final ObservableList<Tab> workingCollection = FXCollections.observableArrayList(tabs);
                Collections.swap(workingCollection, index - 1, index);
                syncNpUpgradeTab(workingCollection.get(index - 1), index - 1);
                syncNpUpgradeTab(workingCollection.get(index), index);
                tabs.setAll(workingCollection);
            }
        });

        final Button rightNpUpgradeButton = new Button();
        rightNpUpgradeButton.setGraphic(createInfoImageView("right"));
        rightNpUpgradeButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Move item right")));
        rightNpUpgradeButton.setOnAction(e -> {
            final ObservableList<Tab> tabs = npUpgradesTabs.getTabs();
            if (tabs.isEmpty() || tabs.size() == 1) {
                return;
            }

            final int index = tabs.indexOf(npUpgradesTabs.getSelectionModel().getSelectedItem());
            if (index < tabs.size() - 1 && index >= 0) {
                final ObservableList<Tab> workingCollection = FXCollections.observableArrayList(tabs);
                Collections.swap(workingCollection, index + 1, index);
                syncNpUpgradeTab(workingCollection.get(index + 1), index + 1);
                syncNpUpgradeTab(workingCollection.get(index), index);
                tabs.setAll(workingCollection);
            }
        });

        final Button removeNpUpgradeButton = new Button();
        removeNpUpgradeButton.setGraphic(createInfoImageView("remove"));
        removeNpUpgradeButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Remove NP Upgrade")));
        removeNpUpgradeButton.setOnAction(e -> {
            final ObservableList<Tab> tabs = npUpgradesTabs.getTabs();
            final List<Tab> remainingNodes = new ArrayList<>(tabs);
            remainingNodes.remove(npUpgradesTabs.getSelectionModel().getSelectedItem());
            for (int i = 0; i < remainingNodes.size(); i += 1) {
                syncNpUpgradeTab(remainingNodes.get(i), i);
            }
            tabs.setAll(remainingNodes);
        });

        npButtonsHBox.getChildren().addAll(npLabel, addNpUpgradeButton, leftNpUpgradeButton, rightNpUpgradeButton, removeNpUpgradeButton);
        nodes.add(npButtonsHBox);
        nodes.add(npUpgradesTabs);

        quickCardDataButton.setOnAction(e -> {
            for (final CommandCardBox cardBox : commandCardBoxes) {
                cardBox.quickFillData(npRateText.getText(), critStarRateText.getText());
            }
            exCommandCardBox.quickFillData(npRateText.getText(), critStarRateText.getText());
            for (final Tab tab : npUpgradesTabs.getTabs()) {
                final NpUpgrade npUpgrade = (NpUpgrade) tab.getContent();
                npUpgrade.quickFillData(npRateText.getText(), critStarRateText.getText());
            }
        });

        nodes.add(new Separator());

        activeSkillUpgradeTabPanes = new ArrayList<>();
        for (int i = 1; i <= 3; i += 1) {
            final Label activeSkillsLabel = new Label(getTranslation(APPLICATION_SECTION, "Active Skill") + " " + i);
            activeSkillsLabel.setFont(new Font(18));

            final TabPane activeSkillTabPane = new TabPane(
                    new Tab(
                            getTranslation(APPLICATION_SECTION, "Base Active Skill"),
                            new ActiveSkillUpgrade(errorLabel)
                    )
            );
            activeSkillTabPane.setStyle("-fx-border-color:grey");
            activeSkillTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            final Button addActiveSkillUpgradeButton = new Button();
            addActiveSkillUpgradeButton.setGraphic(createInfoImageView("add"));
            addActiveSkillUpgradeButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Add NP Upgrade")));
            addActiveSkillUpgradeButton.setOnAction(e -> {
                final List<Tab> tabs = activeSkillTabPane.getTabs();
                if (tabs.isEmpty()) {
                    tabs.add(new Tab(getTranslation(APPLICATION_SECTION, "Base Active Skill"), new ActiveSkillUpgrade(errorLabel)));
                } else {
                    final ActiveSkillUpgrade base = (ActiveSkillUpgrade) activeSkillTabPane.getSelectionModel().getSelectedItem().getContent();
                    final String tabName = getTranslation(APPLICATION_SECTION, "Active Skill Upgrade") + " " + tabs.size();
                    tabs.add(new Tab(tabName, new ActiveSkillUpgrade(base, errorLabel)));
                }
                activeSkillTabPane.getSelectionModel().selectLast();
            });

            final Button leftActiveSkillUpgradeButton = new Button();
            leftActiveSkillUpgradeButton.setGraphic(createInfoImageView("left"));
            leftActiveSkillUpgradeButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Move item left")));
            leftActiveSkillUpgradeButton.setOnAction(e -> {
                final ObservableList<Tab> tabs = activeSkillTabPane.getTabs();
                if (tabs.isEmpty() || tabs.size() == 1) {
                    return;
                }

                final int index = tabs.indexOf(activeSkillTabPane.getSelectionModel().getSelectedItem());
                if (index > 0) {
                    final ObservableList<Tab> workingCollection = FXCollections.observableArrayList(tabs);
                    Collections.swap(workingCollection, index - 1, index);
                    syncActiveSkillUpgrade(workingCollection.get(index - 1), index - 1);
                    syncActiveSkillUpgrade(workingCollection.get(index), index);
                    tabs.setAll(workingCollection);
                }
            });

            final Button rightActiveSkillUpgradeButton = new Button();
            rightActiveSkillUpgradeButton.setGraphic(createInfoImageView("right"));
            rightActiveSkillUpgradeButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Move item right")));
            rightActiveSkillUpgradeButton.setOnAction(e -> {
                final ObservableList<Tab> tabs = activeSkillTabPane.getTabs();
                if (tabs.isEmpty() || tabs.size() == 1) {
                    return;
                }

                final int index = tabs.indexOf(activeSkillTabPane.getSelectionModel().getSelectedItem());
                if (index < tabs.size() - 1 && index >= 0) {
                    final ObservableList<Tab> workingCollection = FXCollections.observableArrayList(tabs);
                    Collections.swap(workingCollection, index + 1, index);
                    syncActiveSkillUpgrade(workingCollection.get(index + 1), index + 1);
                    syncActiveSkillUpgrade(workingCollection.get(index), index);
                    tabs.setAll(workingCollection);
                }
            });

            final Button removeSkillUpgradeButton = new Button();
            removeSkillUpgradeButton.setGraphic(createInfoImageView("remove"));
            removeSkillUpgradeButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Remove Active Skill Upgrade")));
            removeSkillUpgradeButton.setOnAction(e -> {
                final ObservableList<Tab> tabs = activeSkillTabPane.getTabs();
                final List<Tab> remainingNodes = new ArrayList<>(tabs);
                remainingNodes.remove(activeSkillTabPane.getSelectionModel().getSelectedItem());
                for (int j = 0; j < remainingNodes.size(); j += 1) {
                    syncActiveSkillUpgrade(remainingNodes.get(j), j);
                }
                tabs.setAll(remainingNodes);
            });

            final HBox activeSkillButtonHBox = new HBox(10);
            activeSkillButtonHBox.setAlignment(Pos.CENTER_LEFT);
            activeSkillButtonHBox.getChildren().addAll(
                    activeSkillsLabel,
                    addActiveSkillUpgradeButton,
                    leftActiveSkillUpgradeButton,
                    rightActiveSkillUpgradeButton,
                    removeSkillUpgradeButton
            );
            activeSkillUpgradeTabPanes.add(activeSkillTabPane);
            nodes.add(activeSkillButtonHBox);
            nodes.add(activeSkillTabPane);
        }
        nodes.add(new Separator());

        final Label passiveSkillLabel = new Label(getTranslation(APPLICATION_SECTION, "Passive Skill"));
        passiveSkillLabel.setFont(new Font(18));
        nodes.add(passiveSkillLabel);

        passiveSkillsVBox = new VBox(10);

        final HBox passiveHBox = new HBox(10);
        passiveHBox.setAlignment(Pos.CENTER);

        final Button addPassiveButton = new Button();
        addPassiveButton.setGraphic(createInfoImageView("add"));
        addPassiveButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Add Passive Skill")));
        addPassiveButton.setOnAction(e -> passiveSkillsVBox.getChildren().add(new NonActiveSkill(passiveSkillsVBox, errorLabel)));

        nodes.add(passiveSkillsVBox);
        passiveHBox.getChildren().addAll(addPassiveButton);
        nodes.add(passiveHBox);
        nodes.add(new Separator());

        final Label appendSkillLabel = new Label(getTranslation(APPLICATION_SECTION, "Append Skill"));
        appendSkillLabel.setFont(new Font(18));
        nodes.add(appendSkillLabel);

        appendSkillsVBox = new VBox(10);

        final HBox appendHBox = new HBox(10);
        appendHBox.setAlignment(Pos.CENTER);
        final Button addAppendButton = new Button();
        addAppendButton.setGraphic(createInfoImageView("add"));
        addAppendButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Add Append Skill")));
        addAppendButton.setOnAction(e -> appendSkillsVBox.getChildren().add(new NonActiveSkill(appendSkillsVBox, errorLabel)));

        nodes.add(appendSkillsVBox);
        appendHBox.getChildren().addAll(addAppendButton);
        nodes.add(appendHBox);
        nodes.add(new Separator());

        final HBox statusHBox = new HBox(10);
        statusHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final Label statusLabel = new Label(getTranslation(APPLICATION_SECTION, "Servant Status"));
        servantStatus = new TextArea();
        servantStatus.setMaxHeight(200);
        servantStatus.setWrapText(true);
        HBox.setHgrow(servantStatus, Priority.ALWAYS);
        statusHBox.getChildren().addAll(statusLabel, servantStatus);

        nodes.add(statusHBox);
        nodes.add(new Separator());
    }

    public ServantAscensionTab(final ServantAscensionTab source, final int servantNo, final int ascension, final Label errorLabel) {
        this(servantNo, ascension, errorLabel);

        this.rarityChoices.getSelectionModel().select(source.rarityChoices.getValue());
        this.classChoices.getSelectionModel().select(source.classChoices.getValue());
        this.genderChoices.getSelectionModel().select(source.genderChoices.getValue());
        this.attributeChoices.getSelectionModel().select(source.attributeChoices.getValue());
        for (int i = 0; i < this.alignmentChecks.size(); i += 1) {
            this.alignmentChecks.get(i).setSelected(source.alignmentChecks.get(i).isSelected());
        }
        this.defNpText.setText(source.defNpText.getText());
        this.critStarWeightText.setText(source.critStarWeightText.getText());
        this.deathRateText.setText(source.deathRateText.getText());
        this.costText.setText(source.costText.getText());
        this.traitsText.setText(source.traitsText.getText());
        for (int i = 0; i < this.commandCardBoxes.size(); i += 1) {
            this.commandCardBoxes.get(i).setFrom(source.commandCardBoxes.get(i));
        }
        this.exCommandCardBox.setFrom(source.exCommandCardBox);
        final NpUpgrade baseNpUpgrade = (NpUpgrade) this.npUpgradesTabs.getTabs().get(0).getContent();
        baseNpUpgrade.setFrom((NpUpgrade) source.npUpgradesTabs.getTabs().get(0).getContent());
        for (int i = 1; i < source.npUpgradesTabs.getTabs().size(); i += 1) {
            final Tab newTab = new Tab();
            newTab.setText(getTranslation(APPLICATION_SECTION, "NP Upgrade") + " " + i);
            newTab.setContent(new NpUpgrade((NpUpgrade) source.npUpgradesTabs.getTabs().get(i).getContent(), errorLabel));
            this.npUpgradesTabs.getTabs().add(newTab);
        }
        for (int i = 0; i < this.activeSkillUpgradeTabPanes.size(); i += 1) {
            final TabPane thisActiveSkillPane = this.activeSkillUpgradeTabPanes.get(i);
            final TabPane sourceActiveSkillPane = source.activeSkillUpgradeTabPanes.get(i);

            final ActiveSkillUpgrade baseActiveSkillUpgrade = (ActiveSkillUpgrade) thisActiveSkillPane.getTabs().get(0).getContent();
            baseActiveSkillUpgrade.setFrom((ActiveSkillUpgrade) sourceActiveSkillPane.getTabs().get(0).getContent());

            for (int j = 1; j < sourceActiveSkillPane.getTabs().size(); j += 1) {
                final Tab newTab = new Tab();
                newTab.setText(getTranslation(APPLICATION_SECTION, "Active Skill Upgrade") + " " + j);
                newTab.setContent(
                        new ActiveSkillUpgrade(
                                (ActiveSkillUpgrade) sourceActiveSkillPane.getTabs().get(j).getContent(), errorLabel
                        )
                );
                thisActiveSkillPane.getTabs().add(newTab);
            }
        }

        for (final Node node : source.passiveSkillsVBox.getChildren()) {
            this.passiveSkillsVBox.getChildren().add(new NonActiveSkill((NonActiveSkill) node, passiveSkillsVBox, errorLabel));
        }

        for (final Node node : source.appendSkillsVBox.getChildren()) {
            this.appendSkillsVBox.getChildren().add(new NonActiveSkill((NonActiveSkill) node, appendSkillsVBox, errorLabel));
        }
        this.servantStatus.setText(source.servantStatus.getText());
    }

    public ServantAscensionTab(final ServantAscensionData servantAscensionData, final int servantNum, final int ascension, final Label errorLabel) {
        this(servantNum, ascension, errorLabel);

        final CombatantData combatantData = servantAscensionData.getCombatantData();
        this.rarityChoices.getSelectionModel().select(Integer.valueOf(combatantData.getRarity()));
        this.classChoices.getSelectionModel().select(combatantData.getFateClass());
        this.genderChoices.getSelectionModel().select(combatantData.getGender());
        this.attributeChoices.getSelectionModel().select(combatantData.getAttribute());

        final List<String> selectedAlignments = combatantData.getAlignmentsList()
                .stream()
                .map(alignment -> getTranslation(TRAIT_SECTION, alignment.name()))
                .collect(Collectors.toList());

        for (final CheckBox checkBox : alignmentChecks) {
            checkBox.setSelected(selectedAlignments.stream().anyMatch(checkBox.getText()::equalsIgnoreCase));
        }

        this.defNpText.setText(doubleToString(servantAscensionData.getDefenseNpRate()));
        this.critStarWeightText.setText(Integer.toString(servantAscensionData.getCriticalStarWeight()));
        this.deathRateText.setText(doubleToString(combatantData.getDeathRate()));
        this.costText.setText(Integer.toString(servantAscensionData.getCost()));
        this.traitsText.setText(
                combatantData.getTraitsList()
                        .stream()
                        .map(s -> getTranslation(TRAIT_SECTION, s))
                        .collect(Collectors.joining(", "))
        );

        for (int i = 0; i < this.commandCardBoxes.size(); i += 1) {
            this.commandCardBoxes.get(i).setFrom(servantAscensionData.getCommandCardData(i));
        }
        this.exCommandCardBox.setFrom(servantAscensionData.getExtraCard());

        final NpUpgrade baseNpUpgrade = (NpUpgrade) this.npUpgradesTabs.getTabs().get(0).getContent();
        final NoblePhantasmUpgrades npUpgrades = servantAscensionData.getNoblePhantasmUpgrades();
        baseNpUpgrade.setFrom(npUpgrades.getNoblePhantasmData(0));
        for (int i = 1; i < npUpgrades.getNoblePhantasmDataCount(); i += 1) {
            final Tab newTab = new Tab();
            newTab.setText(getTranslation(APPLICATION_SECTION, "NP Upgrade") + " " + i);
            newTab.setContent(new NpUpgrade(npUpgrades.getNoblePhantasmData(i), errorLabel));
            this.npUpgradesTabs.getTabs().add(newTab);
        }

        for (int i = 0; i < this.activeSkillUpgradeTabPanes.size(); i += 1) {
            final TabPane thisActiveSkillPane = this.activeSkillUpgradeTabPanes.get(i);
            final ActiveSkillUpgrades activeSkillUpgrades = servantAscensionData.getActiveSkillUpgrades(i);

            final ActiveSkillUpgrade baseActiveSkillUpgrade = (ActiveSkillUpgrade) thisActiveSkillPane.getTabs().get(0).getContent();
            baseActiveSkillUpgrade.setFrom(activeSkillUpgrades.getActiveSkillData(0));

            for (int j = 1; j < activeSkillUpgrades.getActiveSkillDataCount(); j += 1) {
                final Tab newTab = new Tab();
                newTab.setText(getTranslation(APPLICATION_SECTION, "Active Skill Upgrade") + " " + j);
                newTab.setContent(new ActiveSkillUpgrade(activeSkillUpgrades.getActiveSkillData(j), errorLabel));
                thisActiveSkillPane.getTabs().add(newTab);
            }
        }

        for (final PassiveSkillData passiveSkillData : servantAscensionData.getPassiveSkillDataList()) {
            this.passiveSkillsVBox.getChildren().add(new NonActiveSkill(passiveSkillData, passiveSkillsVBox, errorLabel));
        }

        for (final AppendSkillData appendSkillData : servantAscensionData.getAppendSkillDataList()) {
            this.appendSkillsVBox.getChildren().add(new NonActiveSkill(appendSkillData, appendSkillsVBox, errorLabel));
        }

        final List<String> statusStrings = new ArrayList<>();
        final JsonFormat.Printer printer = JsonFormat.printer();
        try {
            for (final Status status : servantAscensionData.getServantStatusDataList()) {
                statusStrings.add(printer.print(status).replaceAll("[\\n\\t ]", ""));
            }
        } catch (final Exception ignored) {
        }
        this.servantStatus.setText(String.join(", ", statusStrings));
    }

    private void syncActiveSkillUpgrade(final Tab tab, final int index) {
        if (index == 0) {
            tab.setText(getTranslation(APPLICATION_SECTION, "Base Active Skill"));
        } else {
            tab.setText(getTranslation(APPLICATION_SECTION, "Active Skill Upgrade") + " " + index);
        }
    }

    private void syncNpUpgradeTab(final Tab tab, final int index) {
        if (index == 0) {
            tab.setText(getTranslation(APPLICATION_SECTION, "Base NP"));
        } else {
            tab.setText(getTranslation(APPLICATION_SECTION, "NP Upgrade") + " " + index);
        }
    }

    public void setServantNo(final int servantNo, final int ascension) {
        final String servantId = "servant" + servantNo;
        final File thumbnailFile = getServantThumbnail(servantId, ascension);
        try {
            thumbnail.setImage(new Image(new FileInputStream(thumbnailFile)));
        } catch (final FileNotFoundException ignored) {
        }
        this.ascension = ascension;
    }

    public ServantAscensionData build(final int servantNo) {
        final double deathRate;
        try {
            deathRate = RoundUtils.roundNearest(Double.parseDouble(deathRateText.getText()) / 100);
        } catch (final Exception e) {
            errorLabel.setText(
                    getTranslation(APPLICATION_SECTION, "Death Rate is not a valid double") + ": " +
                            getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension
            );
            errorLabel.setVisible(true);
            return null;
        }

        if (deathRate < 0) {
            errorLabel.setText(
                    getTranslation(APPLICATION_SECTION, "Death Rate is negative") + ": " +
                            getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension
            );
            errorLabel.setVisible(true);
            return null;
        }

        final List<Alignment> checkedAlignments = alignmentChecks.stream()
                .filter(CheckBox::isSelected)
                .map(checkBox -> Alignment.valueOf(getKeyForTrait(checkBox.getText())))
                .collect(Collectors.toList());

        final List<String> traits = Arrays.stream(traitsText.getText().trim().split(COMMA_SPLIT_REGEX))
                .filter(s -> !s.isEmpty())
                .map(TranslationManager::getKeyForTrait)
                .collect(Collectors.toList());

        final CombatantData combatantData = CombatantData.newBuilder()
                .setId("servant" + servantNo)
                .setRarity(rarityChoices.getValue())
                .setFateClass(classChoices.getValue())
                .setGender(genderChoices.getValue())
                .addAllAlignments(checkedAlignments)
                .setAttribute(attributeChoices.getValue())
                .addAllTraits(traits)
                .setDeathRate(deathRate)
                .build();

        final double defNpRate;
        try {
            defNpRate = RoundUtils.roundNearest(Double.parseDouble(defNpText.getText()) / 100);
        } catch (final Exception e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Defense NP rate is not a valid double") + ": " +
                                       getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension);
            errorLabel.setVisible(true);
            return null;
        }

        if (defNpRate < 0) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Defense NP rate is negative") + ": " +
                                       getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension);
            defNpText.setVisible(true);
            return null;
        }

        final int starWeight;
        try {
            starWeight = Integer.parseInt(critStarWeightText.getText());
        } catch (final Exception e ) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Critical star weight is not a valid integer") + ": " +
                                       getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension);
            errorLabel.setVisible(true);
            return null;
        }

        final int cost;
        try {
            cost = Integer.parseInt(costText.getText());
        } catch (final Exception e ) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cost is not a valid integer") + ": " +
                                       getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension);
            errorLabel.setVisible(true);
            return null;
        }

        final List<CommandCardData> commandCardData = new ArrayList<>();
        for (int i = 1; i <= commandCardBoxes.size(); i += 1) {
            final CommandCardBox cardBox = commandCardBoxes.get(i - 1);
            try {
                final CommandCardData built = cardBox.build(errorLabel, ascension, i);
                if (built == null) {
                    return null;
                }
                commandCardData.add(cardBox.build(errorLabel, ascension, i));
            } catch (final Exception e) {
                return null;
            }
        }
        final CommandCardData exCommandCardData;
        try {
            exCommandCardData = exCommandCardBox.build(errorLabel, ascension, 0);
        } catch (final Exception e) {
            return null;
        }

        final List<NoblePhantasmData> noblePhantasmData = new ArrayList<>();
        for (int i = 0; i < npUpgradesTabs.getTabs().size(); i += 1) {
            final NpUpgrade npUpgrade = (NpUpgrade) npUpgradesTabs.getTabs().get(i).getContent();
            try {
                noblePhantasmData.add(npUpgrade.build(ascension, i + 6));
            } catch (final Exception e) {
                return null;
            }
        }
        if (noblePhantasmData.isEmpty()) {
            errorLabel.setVisible(true);
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "No NP data") + ": " +
                                       getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension);
            return null;
        }

        final List<ActiveSkillUpgrades> activeSkillUpgrades = new ArrayList<>();
        for (int i = 0; i < activeSkillUpgradeTabPanes.size(); i += 1) {
            final TabPane tabPane = activeSkillUpgradeTabPanes.get(i);
            final List<ActiveSkillData> activeSkillData = new ArrayList<>();
            for (int j = 0; j < tabPane.getTabs().size(); j += 1) {
                final ActiveSkillUpgrade activeSkillUpgrade = (ActiveSkillUpgrade) tabPane.getTabs().get(j).getContent();
                try {
                    activeSkillData.add(activeSkillUpgrade.build());
                } catch (final Exception e) {
                    final String skillUpgradeText = j == 0
                            ? getTranslation(APPLICATION_SECTION, "Base Active Skill")
                            : getTranslation(APPLICATION_SECTION, "Active Skill Upgrade") + " " + j;
                    errorLabel.setVisible(true);
                    errorLabel.setText(
                            getTranslation(APPLICATION_SECTION, "Activate skill not parsable") + ": " +
                                    getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension + " " +
                                    getTranslation(APPLICATION_SECTION, "Active Skill") + " " + (i + 1) + " " +
                                    skillUpgradeText
                    );
                    return null;
                }
            }
            activeSkillUpgrades.add(ActiveSkillUpgrades.newBuilder().addAllActiveSkillData(activeSkillData).build());
        }

        final List<PassiveSkillData> passiveSkillData = new ArrayList<>();
        for (int i = 1; i <= passiveSkillsVBox.getChildren().size(); i += 1) {
            final NonActiveSkill nonActiveSkill = (NonActiveSkill) passiveSkillsVBox.getChildren().get(i - 1);
            try {
                passiveSkillData.add(nonActiveSkill.buildPassive());
            } catch (final Exception e) {
                errorLabel.setVisible(true);
                errorLabel.setText(
                        getTranslation(APPLICATION_SECTION, "Passive skill not parsable") + ": " +
                                getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension + " " +
                                getTranslation(APPLICATION_SECTION, "Passive Skill") + " " + i
                );
                return null;
            }
        }

        final List<AppendSkillData> appendSkillData = new ArrayList<>();
        for (int i = 1; i <= appendSkillsVBox.getChildren().size(); i += 1) {
            final NonActiveSkill nonActiveSkill = (NonActiveSkill) appendSkillsVBox.getChildren().get(i - 1);
            try {
                appendSkillData.add(nonActiveSkill.buildAppend());
            } catch (final Exception e) {
                errorLabel.setVisible(true);
                errorLabel.setText(
                        getTranslation(APPLICATION_SECTION, "Append skill not parsable") + ": " +
                                getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension + " " +
                                getTranslation(APPLICATION_SECTION, "Append Skill") + " " + i
                );
                return null;
            }
        }

       final ServantAscensionData.Builder status = ServantAscensionData.newBuilder();
        try {
            JsonFormat.parser().merge(new StringReader("{\"servantStatusData\":[" + servantStatus.getText().trim() + "]}"), status);
        } catch (IOException e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Servant status not parsable") + ": " +
                                       getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension);
            errorLabel.setVisible(true);
            return null;
        }

        final ServantAscensionData.Builder builder = ServantAscensionData.newBuilder()
                .setCombatantData(combatantData)
                .setDefenseNpRate(defNpRate)
                .setCriticalStarWeight(starWeight)
                .setCost(cost)
                .addAllCommandCardData(commandCardData)
                .setExtraCard(exCommandCardData)
                .setNoblePhantasmUpgrades(NoblePhantasmUpgrades.newBuilder().addAllNoblePhantasmData(noblePhantasmData))
                .addAllActiveSkillUpgrades(activeSkillUpgrades)
                .addAllPassiveSkillData(passiveSkillData)
                .addAllAppendSkillData(appendSkillData);

        builder.addAllServantStatusData(status.getServantStatusDataList());
        return builder.build();
    }

    public void clearError() {
        errorLabel.setVisible(false);
    }
}
