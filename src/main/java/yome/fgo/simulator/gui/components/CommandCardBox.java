package yome.fgo.simulator.gui.components;

import com.google.common.collect.ImmutableList;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.simulator.utils.RoundUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.EXTRA;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.simulator.gui.components.DataPrinter.doubleToString;
import static yome.fgo.simulator.gui.helpers.ComponentMaker.COMMA_SPLIT_REGEX;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class CommandCardBox extends VBox {
    private final ChoiceBox<CommandCardType> cardType;
    private final TextField hits;
    private final TextField npRate;
    private final TextField critStarRate;

    public CommandCardBox(final int cardId) {
        super();
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setSpacing(10);
        setPadding(new Insets(5, 5, 5, 5));

        final List<Node> nodes = getChildren();

        final String idString;
        if (cardId == 0) {
            idString = getTranslation(APPLICATION_SECTION, "Command Card") + " " + "EX";
        } else if (cardId <= 5) {
            idString = getTranslation(APPLICATION_SECTION, "Command Card") + " " + cardId;
        } else {
            idString = getTranslation(APPLICATION_SECTION, "NP Card");
        }
        final Label idLabel = new Label(idString);
        nodes.add(idLabel);

        final HBox typeHBox = new HBox();
        typeHBox.setSpacing(10);
        typeHBox.setAlignment(Pos.CENTER_LEFT);
        typeHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final Label typeLabel = new Label(getTranslation(APPLICATION_SECTION, "Card Type"));
        final AnchorPane typeAnchor = new AnchorPane();
        HBox.setHgrow(typeAnchor, Priority.ALWAYS);
        cardType = new ChoiceBox<>();
        AnchorPane.setTopAnchor(cardType, 0.0);
        AnchorPane.setBottomAnchor(cardType, 0.0);
        AnchorPane.setLeftAnchor(cardType, 0.0);
        AnchorPane.setRightAnchor(cardType, 0.0);
        cardType.setConverter(new EnumConverter<>(COMMAND_CARD_TYPE_SECTION));
        final List<CommandCardType> cardTypeSelections = cardId > 0
                ? ImmutableList.of(QUICK, ARTS, BUSTER)
                : ImmutableList.of(EXTRA);
        cardType.setItems(FXCollections.observableArrayList(cardTypeSelections));
        cardType.setOnAction(e -> {
            switch (cardType.getValue()) {
                case BUSTER ->
                        setStyle("-fx-border-color:red; -fx-background-color: rgba(255,0,0,0.3); -fx-border-width: 2");
                case ARTS ->
                        setStyle("-fx-border-color:blue; -fx-background-color: rgba(0,0,255,0.3); -fx-border-width: 2");
                case QUICK ->
                        setStyle("-fx-border-color:green; -fx-background-color: rgba(0,255,0,0.3); -fx-border-width: 2");
                default -> setStyle(
                        "-fx-border-color:#bdbdbd; -fx-background-color: rgba(255,255,255,0.94); -fx-border-width: 2");
            }
        });
        cardType.getSelectionModel().selectFirst();
        typeAnchor.getChildren().add(cardType);
        typeHBox.getChildren().addAll(typeLabel, typeAnchor);

        nodes.add(typeHBox);

        final HBox hitsBox = new HBox();
        hitsBox.setSpacing(10);
        hitsBox.setAlignment(Pos.CENTER_LEFT);
        hitsBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final Label hitsLabel = new Label(getTranslation(APPLICATION_SECTION, "Hits (%)"));
        final AnchorPane hitsAnchor = new AnchorPane();
        HBox.setHgrow(hitsAnchor, Priority.ALWAYS);
        hits = new TextField();
        AnchorPane.setTopAnchor(hits, 0.0);
        AnchorPane.setBottomAnchor(hits, 0.0);
        AnchorPane.setLeftAnchor(hits, 0.0);
        AnchorPane.setRightAnchor(hits, 0.0);
        hitsAnchor.getChildren().add(hits);
        hitsBox.getChildren().addAll(hitsLabel, hitsAnchor);

        nodes.add(hitsBox);

        final HBox npRateBox = new HBox();
        npRateBox.setSpacing(10);
        npRateBox.setAlignment(Pos.CENTER_LEFT);
        npRateBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final Label npRateLabel = new Label(getTranslation(APPLICATION_SECTION, "NP (%)"));
        final AnchorPane npRateAnchor = new AnchorPane();
        HBox.setHgrow(npRateAnchor, Priority.ALWAYS);
        npRate = new TextField();
        AnchorPane.setTopAnchor(npRate, 0.0);
        AnchorPane.setBottomAnchor(npRate, 0.0);
        AnchorPane.setLeftAnchor(npRate, 0.0);
        AnchorPane.setRightAnchor(npRate, 0.0);
        npRateAnchor.getChildren().add(npRate);
        npRateBox.getChildren().addAll(npRateLabel, npRateAnchor);

        nodes.add(npRateBox);

        final HBox critStarRateBox = new HBox();
        critStarRateBox.setSpacing(10);
        critStarRateBox.setAlignment(Pos.CENTER_LEFT);
        critStarRateBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final Label critStarRateLabel = new Label(getTranslation(APPLICATION_SECTION, "Crit Star Rate (%)"));
        final AnchorPane critStarRateAnchor = new AnchorPane();
        HBox.setHgrow(critStarRateAnchor, Priority.ALWAYS);
        critStarRate = new TextField();
        AnchorPane.setTopAnchor(critStarRate, 0.0);
        AnchorPane.setBottomAnchor(critStarRate, 0.0);
        AnchorPane.setLeftAnchor(critStarRate, 0.0);
        AnchorPane.setRightAnchor(critStarRate, 0.0);
        critStarRateAnchor.getChildren().add(critStarRate);
        critStarRateBox.getChildren().addAll(critStarRateLabel, critStarRateAnchor);

        nodes.add(critStarRateBox);
    }

    public void setFrom(final CommandCardBox source) {
        if (this.cardType.getItems().contains(source.cardType.getValue())) {
            this.cardType.getSelectionModel().select(source.cardType.getValue());
        }
        this.hits.setText(source.hits.getText());
        this.npRate.setText(source.npRate.getText());
        this.critStarRate.setText(source.critStarRate.getText());
    }

    public CommandCardData build() {
        final List<Integer> hitInts = Arrays.stream(hits.getText().trim().split(COMMA_SPLIT_REGEX))
                    .sequential()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

        final double npRateVal = RoundUtils.roundNearest(Double.parseDouble(npRate.getText()) / 100);
        final double starRateVal = RoundUtils.roundNearest(Double.parseDouble(critStarRate.getText()) / 100);

        return CommandCardData.newBuilder()
                .setCommandCardType(cardType.getValue())
                .addAllHitsData(hitInts)
                .setNpRate(npRateVal)
                .setCriticalStarGen(starRateVal)
                .build();
    }

    public void setFrom(final CommandCardData commandCardData) {
        cardType.getSelectionModel().select(commandCardData.getCommandCardType());
        hits.setText(
                commandCardData.getHitsDataList()
                        .stream()
                        .map(i -> Integer.toString(i))
                        .collect(Collectors.joining(", "))
        );
        npRate.setText(doubleToString(commandCardData.getNpRate()));
        critStarRate.setText(doubleToString(commandCardData.getCriticalStarGen()));
    }
}
