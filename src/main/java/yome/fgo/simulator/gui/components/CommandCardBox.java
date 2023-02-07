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
import static yome.fgo.simulator.gui.helpers.DataPrinter.doubleToString;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.COMMA_SPLIT_REGEX;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.wrapInAnchor;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class CommandCardBox extends VBox {
    private final ChoiceBox<CommandCardType> cardType;
    private final TextField hits;
    private final TextField npRate;
    private final TextField critStarRate;

    public CommandCardBox(final int cardId) {
        super(10);
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setPadding(new Insets(5));

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

        final HBox typeHBox = new HBox(10);
        typeHBox.setAlignment(Pos.CENTER_LEFT);
        typeHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final Label typeLabel = new Label(getTranslation(APPLICATION_SECTION, "Card Type"));
        cardType = new ChoiceBox<>();
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
        final AnchorPane typeAnchor = wrapInAnchor(cardType);
        HBox.setHgrow(typeAnchor, Priority.ALWAYS);
        typeHBox.getChildren().addAll(typeLabel, typeAnchor);

        nodes.add(typeHBox);

        final HBox hitsBox = new HBox(10);
        hitsBox.setAlignment(Pos.CENTER_LEFT);
        hitsBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final Label hitsLabel = new Label(getTranslation(APPLICATION_SECTION, "Hits (%)"));
        hits = new TextField();
        final AnchorPane hitsAnchor = wrapInAnchor(hits);
        HBox.setHgrow(hitsAnchor, Priority.ALWAYS);
        hitsBox.getChildren().addAll(hitsLabel, hitsAnchor);

        nodes.add(hitsBox);

        final HBox npRateBox = new HBox(10);
        npRateBox.setAlignment(Pos.CENTER_LEFT);
        npRateBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final Label npRateLabel = new Label(getTranslation(APPLICATION_SECTION, "NP (%)"));
        npRate = new TextField();
        final AnchorPane npRateAnchor = wrapInAnchor(npRate);
        HBox.setHgrow(npRateAnchor, Priority.ALWAYS);
        npRateBox.getChildren().addAll(npRateLabel, npRateAnchor);

        nodes.add(npRateBox);

        final HBox critStarRateBox = new HBox(10);
        critStarRateBox.setAlignment(Pos.CENTER_LEFT);
        critStarRateBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        final Label critStarRateLabel = new Label(getTranslation(APPLICATION_SECTION, "Crit Star Rate (%)"));
        critStarRate = new TextField();
        final AnchorPane critStarRateAnchor = wrapInAnchor(critStarRate);
        HBox.setHgrow(critStarRateAnchor, Priority.ALWAYS);
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

    public CommandCardData build(final Label errorLabel, final int ascension, final int cardId) {
        final String cardIndexText;

        if (cardId == 0) {
            cardIndexText = getTranslation(APPLICATION_SECTION, "Command Card") + " " + "EX";
        } else if (cardId <= 5) {
            cardIndexText = getTranslation(APPLICATION_SECTION, "Command Card") + " " + cardId;
        } else if (cardId == 6) {
            cardIndexText = getTranslation(APPLICATION_SECTION, "Base NP");
        } else {
            cardIndexText = getTranslation(APPLICATION_SECTION, "NP Upgrdae") + " " + (cardId - 6);
        }

        final List<Integer> hitInts;
        try {
            hitInts = Arrays.stream(hits.getText().trim().split(COMMA_SPLIT_REGEX))
                    .sequential()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (final Exception e) {
            errorLabel.setVisible(true);
            errorLabel.setText(
                    getTranslation(APPLICATION_SECTION, "Hits not integer") + ": " +
                            getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension + ", " +
                            cardIndexText
            );
            return null;
        }

        final double npRateVal;
        try {
            npRateVal = RoundUtils.roundNearest(Double.parseDouble(npRate.getText()) / 100);
        } catch (final Exception e) {
            errorLabel.setVisible(true);
            errorLabel.setText(
                    getTranslation(APPLICATION_SECTION, "Np Rate not integer") + ": " +
                            getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension + ", " +
                            cardIndexText
            );
            return null;
        }


        final double starRateVal;
        try {
            starRateVal = RoundUtils.roundNearest(Double.parseDouble(critStarRate.getText()) / 100);
        } catch (final Exception e) {
            errorLabel.setVisible(true);
            errorLabel.setText(
                    getTranslation(APPLICATION_SECTION, "Crit star rate not integer") + ": " +
                            getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension + ", " +
                            cardIndexText
            );
            return null;
        }

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

    public void quickFillData(final String npRateString, final String critStarRateString) {
        npRate.setText(npRateString);
        critStarRate.setText(critStarRateString);
    }
}
