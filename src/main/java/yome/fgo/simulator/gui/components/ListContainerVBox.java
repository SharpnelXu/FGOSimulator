package yome.fgo.simulator.gui.components;

import com.google.common.collect.ImmutableList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.EffectData;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static yome.fgo.simulator.gui.components.DataPrinter.printEffectData;
import static yome.fgo.simulator.gui.creators.EffectBuilder.createEffect;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.LIST_ITEM_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.wrapInAnchor;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.SIMULATION_ICON_DIRECTORY_PATH;

public class ListContainerVBox extends VBox {
    private final VBox itemListVBox;
    private final ToggleGroup toggleGroup;
    private final Label errorLabel;

    public ListContainerVBox(final String labelText, final Label errorLabel) {
        super(5);

        this.errorLabel = errorLabel;

        final Label label = new Label(getTranslation(APPLICATION_SECTION, labelText));

        itemListVBox = new VBox(5);
        itemListVBox.setFillWidth(true);
        toggleGroup = new ToggleGroup();

        final Button addButton = new Button();
        addButton.setGraphic(createInfoImageView("add"));
        addButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Add new item")));
        addButton.setOnAction(e -> addNewEffect());

        final Button upButton = new Button();
        upButton.setGraphic(createInfoImageView("up"));
        upButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Move selected item up")));
        upButton.setOnAction(e -> {
            final ObservableList<Node> items = itemListVBox.getChildren();
            if (items.isEmpty() || items.size() == 1) {
                return;
            }

            final int index = items.indexOf(toggleGroup.getSelectedToggle());
            if (index > 0) {
                final ObservableList<Node> workingCollection = FXCollections.observableArrayList(items);
                Collections.swap(workingCollection, index - 1, index);
                items.setAll(workingCollection);
            }
        });

        final Button downButton = new Button();
        downButton.setGraphic(createInfoImageView("down"));
        downButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Move selected item down")));
        downButton.setOnAction(e -> {
            final ObservableList<Node> items = itemListVBox.getChildren();
            if (items.isEmpty() || items.size() == 1) {
                return;
            }

            final int index = items.indexOf(toggleGroup.getSelectedToggle());
            if (index < items.size() - 1 && index >= 0 ) {
                final ObservableList<Node> workingCollection = FXCollections.observableArrayList(items);
                Collections.swap(workingCollection, index + 1, index);
                items.setAll(workingCollection);
            }
        });

        final Button editButton = new Button();
        editButton.setGraphic(createInfoImageView("edit"));
        editButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Edit selected item")));
        editButton.setOnAction(e -> editSelectedEffect());

        final Button removeButton = new Button();
        removeButton.setGraphic(createInfoImageView("remove"));
        removeButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Remove selected item")));
        removeButton.setOnAction(e -> {
            final Toggle selected = toggleGroup.getSelectedToggle();
            if (selected == null) {
                return;
            }
            itemListVBox.getChildren().remove(selected);
        });

        final HBox buttonsHBox = new HBox(5);
        buttonsHBox.setAlignment(Pos.CENTER_LEFT);
        buttonsHBox.getChildren().addAll(label, addButton, upButton, downButton, editButton, removeButton);

        getChildren().addAll(buttonsHBox, wrapInAnchor(itemListVBox));
    }

    public List<EffectData> build() {
        final ImmutableList.Builder<EffectData> builder = ImmutableList.builder();
        for (final Node node : itemListVBox.getChildren()) {
            final EffectRadio effectRadio = (EffectRadio) node;
            builder.add(effectRadio.storedEffect);
        }
        return builder.build();
    }

    public void load(final List<EffectData> effectDataList) {
        for (final EffectData effectData : effectDataList) {
            addToItemVBox(effectData);
        }
    }

    private ImageView createInfoImageView(final String iconName) {
        final ImageView imageView = new ImageView();
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        final String path = String.format("%s/%s.png", SIMULATION_ICON_DIRECTORY_PATH, iconName);
        imageView.setImage(getImage(path));
        return imageView;
    }

    private Image getImage(final String path) {
        Image image = null;
        try {
            image = new Image(new FileInputStream(path));
        } catch (final FileNotFoundException ignored) {
        }
        return image;
    }

    private void addNewEffect() {
        try {
            final EffectData.Builder builder = EffectData.newBuilder();
            createEffect(getScene().getWindow(), builder);

            if (!builder.getType().isEmpty()) {
                addToItemVBox(builder.build());
            }
        } catch (final IOException exception) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + exception);
            errorLabel.setVisible(true);
        }
    }

    private void addToItemVBox(final EffectData effectData) {
        final RadioButton newItem = new EffectRadio(toggleGroup, effectData);
        errorLabel.getScene().widthProperty().addListener(e -> {
            final double width = errorLabel.getScene().getWidth() - 55;
            newItem.setPrefWidth(width);
        });
        newItem.setPrefWidth(errorLabel.getScene().getWidth() - 55);
        itemListVBox.getChildren().add(newItem);
    }

    private void editSelectedEffect() {
        try {
            final EffectRadio selectedEffect = (EffectRadio) toggleGroup.getSelectedToggle();
            if (selectedEffect == null || !itemListVBox.getChildren().contains(selectedEffect)) {
                return;
            }

            final EffectData.Builder builder = selectedEffect.storedEffect.toBuilder();
            createEffect(getScene().getWindow(), builder);

            if (!builder.getType().isEmpty()) {
                selectedEffect.storedEffect = builder.build();
                selectedEffect.setText(printEffectData(selectedEffect.storedEffect));
            }
        } catch (final IOException e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + e);
            errorLabel.setVisible(true);
        }
    }

    private static class EffectRadio extends RadioButton {
        private EffectData storedEffect;

        public EffectRadio(final ToggleGroup toggleGroup, final EffectData effectData) {
            super();

            this.storedEffect = effectData;
            setToggleGroup(toggleGroup);
            setText(printEffectData(effectData));
            setWrapText(true);
            setStyle(LIST_ITEM_STYLE);
            setPadding(new Insets(3));
        }
    }
}
