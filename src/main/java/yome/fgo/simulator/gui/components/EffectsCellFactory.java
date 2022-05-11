package yome.fgo.simulator.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import lombok.AllArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.EffectData;

import java.io.IOException;

import static yome.fgo.simulator.gui.components.DataPrinter.printEffectData;
import static yome.fgo.simulator.gui.creators.EffectBuilder.createEffect;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@AllArgsConstructor
public class EffectsCellFactory implements Callback<ListView<EffectData>, ListCell<EffectData>> {
    private final Label errorLabel;
    @Override
    public ListCell<EffectData> call(final ListView<EffectData> param) {
        return new ListCell<>() {
            @Override
            public void updateItem(final EffectData effectData, final boolean empty) {
                super.updateItem(effectData, empty);

                if (empty || effectData == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    final Button editButton = new Button(getTranslation(APPLICATION_SECTION, "Edit"));
                    editButton.setPrefWidth(55);

                    editButton.setOnAction(event -> {
                        try {
                            final EffectData.Builder builder = effectData.toBuilder();
                            createEffect(getListView().getScene().getWindow(), builder);

                            if (!builder.getType().isEmpty()) {
                                final int index = getListView().getItems().indexOf(effectData);
                                getListView().getItems().set(index, builder.build());
                            }

                        } catch (final IOException e) {
                            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + e);
                            errorLabel.setVisible(true);
                        }
                        getListView().requestFocus();
                    });

                    final Button removeButton = new Button(getTranslation(APPLICATION_SECTION, "Remove"));
                    removeButton.setPrefWidth(55);

                    removeButton.setOnAction(event -> {
                        getListView().getItems().remove(effectData);
                        getListView().requestFocus();
                    });

                    final Label label = new Label(printEffectData(effectData));
                    label.setWrapText(true);
                    HBox.setHgrow(label, Priority.ALWAYS);
                    label.setMaxWidth(Math.max(param.getWidth() - 140, 500));

                    final HBox hBox = new HBox();
                    hBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    hBox.setSpacing(10);
                    hBox.getChildren().addAll(removeButton, editButton, label);

                    setGraphic(hBox);
                }
            }
        };
    }
}
