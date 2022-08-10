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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.EffectData;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static yome.fgo.simulator.gui.components.DataPrinter.printBuffData;
import static yome.fgo.simulator.gui.components.DataPrinter.printEffectData;
import static yome.fgo.simulator.gui.creators.BuffBuilder.createBuff;
import static yome.fgo.simulator.gui.creators.EffectBuilder.createEffect;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.LIST_ITEM_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createInfoImageView;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class ListContainerVBox extends VBox {
    private final boolean isBuff;
    private final VBox itemListVBox;
    private final ToggleGroup toggleGroup;
    private final Label errorLabel;

    public ListContainerVBox(final String labelText, final Label errorLabel) {
        this(labelText, errorLabel, false);
    }

    public ListContainerVBox(final String labelText, final Label errorLabel, final boolean isBuff) {
        super(5);
        setPadding(new Insets(5));
        setStyle("-fx-border-color: grey; -fx-border-width: 2");

        this.isBuff = isBuff;
        this.errorLabel = errorLabel;

        final Label label = new Label(getTranslation(APPLICATION_SECTION, labelText));

        itemListVBox = new VBox(5);
        toggleGroup = new ToggleGroup();

        final Button addButton = new Button();
        addButton.setGraphic(createInfoImageView("add"));
        addButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Add new item")));
        addButton.setOnAction(e -> addNewItem());

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
        editButton.setOnAction(e -> editSelectedItem());

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

        getChildren().addAll(buttonsHBox, itemListVBox);
    }

    public List<EffectData> buildEffect() {
        final ImmutableList.Builder<EffectData> builder = ImmutableList.builder();
        for (final Node node : itemListVBox.getChildren()) {
            final ItemRadio itemRadio = (ItemRadio) node;
            builder.add(itemRadio.storedEffect);
        }
        return builder.build();
    }

    public List<BuffData> buildBuff() {
        final ImmutableList.Builder<BuffData> builder = ImmutableList.builder();
        for (final Node node : itemListVBox.getChildren()) {
            final ItemRadio itemRadio = (ItemRadio) node;
            builder.add(itemRadio.storedBuff);
        }
        return builder.build();
    }

    public void loadEffect(final List<EffectData> effectDataList) {
        for (final EffectData effectData : effectDataList) {
            addToItemVBox(new ItemRadio(toggleGroup, effectData));
        }
    }

    public void loadBuff(final List<BuffData> buffDataList) {
        for (final BuffData buffData : buffDataList) {
            addToItemVBox(new ItemRadio(toggleGroup, buffData));
        }
    }

    public void clear() {
        itemListVBox.getChildren().clear();
    }

    private void addNewItem() {
        try {
            if (isBuff) {
                final BuffData.Builder builder = BuffData.newBuilder();
                createBuff(getScene().getWindow(), builder);

                if (!builder.getType().isEmpty()) {
                    addToItemVBox(new ItemRadio(toggleGroup, builder.build()));
                }
            } else {
                final EffectData.Builder builder = EffectData.newBuilder();
                createEffect(getScene().getWindow(), builder);

                if (!builder.getType().isEmpty()) {
                    addToItemVBox(new ItemRadio(toggleGroup, builder.build()));
                }
            }
        } catch (final IOException exception) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + exception);
            errorLabel.setVisible(true);
        }
    }

    private void addToItemVBox(final ItemRadio itemRadio) {
        itemListVBox.getChildren().add(itemRadio);
    }

    private void editSelectedItem() {
        try {
            final ItemRadio selectedItem = (ItemRadio) toggleGroup.getSelectedToggle();
            if (selectedItem == null || !itemListVBox.getChildren().contains(selectedItem)) {
                return;
            }

            if (isBuff) {
                final BuffData.Builder builder = selectedItem.storedBuff.toBuilder();
                createBuff(getScene().getWindow(), builder);

                if (!builder.getType().isEmpty()) {
                    selectedItem.storedBuff = builder.build();
                    selectedItem.setText(printBuffData(selectedItem.storedBuff));
                }
            } else {
                final EffectData.Builder builder = selectedItem.storedEffect.toBuilder();
                createEffect(getScene().getWindow(), builder);

                if (!builder.getType().isEmpty()) {
                    selectedItem.storedEffect = builder.build();
                    selectedItem.setText(printEffectData(selectedItem.storedEffect));
                }
            }
        } catch (final IOException e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + e);
            errorLabel.setVisible(true);
        }
    }

    private static class ItemRadio extends RadioButton {
        private EffectData storedEffect;
        private BuffData storedBuff;

        public ItemRadio(final ToggleGroup toggleGroup) {
            super();

            setMaxWidth(Double.MAX_VALUE);
            setToggleGroup(toggleGroup);
            setWrapText(true);
            setStyle(LIST_ITEM_STYLE);
            setPadding(new Insets(3));
        }

        public ItemRadio(final ToggleGroup toggleGroup, final EffectData effectData) {
            this(toggleGroup);
            this.storedEffect = effectData;
            setText(printEffectData(effectData));
        }

        public ItemRadio(final ToggleGroup toggleGroup, final BuffData buffData) {
            this(toggleGroup);
            this.storedBuff = buffData;
            setText(printBuffData(buffData));
        }
    }
}
