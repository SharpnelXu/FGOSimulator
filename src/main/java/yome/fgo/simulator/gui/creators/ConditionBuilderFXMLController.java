package yome.fgo.simulator.gui.creators;

import com.google.common.collect.Lists;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.gui.components.SubConditionCellFactory;
import yome.fgo.simulator.translation.TranslationManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static yome.fgo.simulator.gui.creators.ConditionBuilder.createCondition;
import static yome.fgo.simulator.models.conditions.ConditionFactory.getAllAvailableConditionOptions;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CONDITION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class ConditionBuilderFXMLController implements Initializable {
    @FXML
    private Button buildButton;

    @FXML
    private Button cancelButton;

    @FXML
    private ChoiceBox<String> conditionChoices;

    @FXML
    private Label conditionLabel;

    @FXML
    private Label subConditionLabel;

    @FXML
    private AnchorPane subConditionPane;

    @FXML
    private ListView<ConditionData> subConditionList;

    @FXML
    private ChoiceBox<Target> targetChoices;

    @FXML
    private Label targetLabel;

    @FXML
    private AnchorPane targetPane;

    @FXML
    private Label valueLabel;

    @FXML
    private AnchorPane valuePane;

    @FXML
    private TextField valueText;

    @FXML
    private Label errorLabel;

    @FXML
    private Button addListItemButton;

    private ConditionData.Builder conditionDataBuilder;

    public void setParentBuilder(final ConditionData.Builder builder) {
        this.conditionDataBuilder = builder;

        if (!builder.getType().isEmpty()) {
            conditionChoices.getSelectionModel().select(builder.getType());

            // TODO: populate ConditionBuilder with this builder as default
        }
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        conditionLabel.setText(getTranslation(APPLICATION_SECTION, "Condition Type"));

        conditionChoices.setConverter(new StringConverter<>() {
            @Override
            public String toString(final String object) {
                return getTranslation(CONDITION_SECTION, object);
            }

            @Override
            public String fromString(final String string) {
                return null;
            }
        });
        conditionChoices.setItems(FXCollections.observableArrayList(getAllAvailableConditionOptions()));
        conditionChoices.getSelectionModel().select( "Always");

        conditionChoices.setOnAction(e -> onConditionChoicesChange());

        valueLabel.setText(getTranslation(APPLICATION_SECTION, "Value"));

        targetLabel.setText(getTranslation(APPLICATION_SECTION, "Target"));

        final List<Target> targets = Lists.newArrayList(Target.values());
        targets.remove(Target.UNRECOGNIZED);
        targets.remove(Target.SERVANT_EXCHANGE);
        targetChoices.setConverter(new StringConverter<>() {
            @Override
            public String toString(final Target object) {
                return getTranslation(TARGET_SECTION, object.name());
            }

            @Override
            public Target fromString(final String string) {
                return null;
            }
        });
        targetChoices.setItems(FXCollections.observableArrayList(targets));
        targetChoices.getSelectionModel().selectFirst();

        subConditionLabel.setText(getTranslation(APPLICATION_SECTION, "Sub-conditions"));

        cancelButton.setText(getTranslation(APPLICATION_SECTION, "Cancel"));

        buildButton.setText(getTranslation(APPLICATION_SECTION, "Build"));

        valuePane.setVisible(false);
        valuePane.setManaged(false);

        targetPane.setVisible(false);
        targetPane.setManaged(false);

        subConditionPane.setVisible(false);
        subConditionPane.setManaged(false);

        subConditionList.setCellFactory(new SubConditionCellFactory(errorLabel));
        subConditionList.setItems(FXCollections.observableArrayList());

        addListItemButton.setText(getTranslation(APPLICATION_SECTION, "Add sub condition"));

        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Select a condition type to start"));
        errorLabel.setVisible(true);
    }

    @FXML
    public void onConditionChoicesChange() {
        // TODO show field on change
        System.out.println(conditionChoices.getValue());
    }

    @FXML
    public void onAddListItemButtonClick() {
        try {
            final ConditionData.Builder builder = ConditionData.newBuilder();
            createCondition(addListItemButton.getScene().getWindow(), builder);

            if (!builder.getType().isEmpty()) {
                subConditionList.getItems().add(builder.build());
            }
        } catch (final IOException e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!" + e));
            errorLabel.setVisible(true);
        }
    }

    @FXML
    public void onCancelButtonClick() {
        if (conditionDataBuilder != null) {
            conditionDataBuilder.clearType();
        }
        final Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onBuildButtonClick() {
        if (conditionDataBuilder != null) {
            conditionDataBuilder.setType(conditionChoices.getValue());
            // TODO: build condition
        }
        final Stage stage = (Stage) buildButton.getScene().getWindow();
        stage.close();
    }
}
