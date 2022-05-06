package yome.fgo.simulator.gui.components;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
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
                    AnchorPane.setTopAnchor(editButton, 0.0);
                    AnchorPane.setBottomAnchor(editButton, 0.0);
                    AnchorPane.setLeftAnchor(editButton, 65.0);

                    editButton.setOnAction(event -> {
                        try {
                            final EffectData.Builder builder = effectData.toBuilder();
                            createEffect(getListView().getScene().getWindow(), builder);

                            if (!builder.getType().isEmpty()) {
                                final int index = getListView().getItems().indexOf(effectData);
                                getListView().getItems().set(index, builder.build());
                            }

                        } catch (final IOException e) {
                            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!" + e));
                            errorLabel.setVisible(true);
                        }
                    });

                    final Button removeButton = new Button(getTranslation(APPLICATION_SECTION, "Remove"));
                    removeButton.setPrefWidth(55);
                    AnchorPane.setTopAnchor(removeButton, 0.0);
                    AnchorPane.setBottomAnchor(removeButton, 0.0);
                    AnchorPane.setLeftAnchor(removeButton, 0.0);

                    removeButton.setOnAction(event -> getListView().getItems().remove(effectData));

                    final Label label = new Label(printEffectData(effectData));
                    AnchorPane.setTopAnchor(label, 0.0);
                    AnchorPane.setBottomAnchor(label, 0.0);
                    AnchorPane.setLeftAnchor(label, 130.0);

                    final AnchorPane anchorPane = new AnchorPane();
                    anchorPane.getChildren().addAll(label, editButton, removeButton);

                    setGraphic(anchorPane);
                }
            }
        };
    }
}
