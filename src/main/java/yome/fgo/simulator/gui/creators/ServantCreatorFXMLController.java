package yome.fgo.simulator.gui.creators;

import com.google.protobuf.util.JsonFormat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import yome.fgo.data.proto.FgoStorageData.ServantAscensionData;
import yome.fgo.data.proto.FgoStorageData.ServantData;
import yome.fgo.data.writer.DataWriter;
import yome.fgo.simulator.gui.components.ServantAscensionTab;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static yome.fgo.simulator.ResourceManager.readFile;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createInfoImageView;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.translation.TranslationManager.hasTranslation;
import static yome.fgo.simulator.utils.FilePathUtils.SERVANT_DIRECTORY_PATH;

public class ServantCreatorFXMLController implements Initializable {
    @FXML
    private Label errorLabel;

    @FXML
    private Label idLabel;

    @FXML
    private TextField idText;

    @FXML
    private Button loadButton;

    @FXML
    private Button saveButton;

    @FXML
    private TabPane servantAscTabPane;

    @FXML
    private Label servantNameLabel;

    @FXML
    private HBox actionHBox;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        idLabel.setText(getTranslation(APPLICATION_SECTION, "Servant Number"));
        servantNameLabel.setText(null);
        idText.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    final String servantId = "servant" + newValue;
                    if (hasTranslation(ENTITY_NAME_SECTION, servantId)) {
                        servantNameLabel.setText(getTranslation(ENTITY_NAME_SECTION, servantId));
                        servantNameLabel.setVisible(true);
                    } else {
                        servantNameLabel.setVisible(false);
                        servantNameLabel.setText(null);
                    }
                    try {
                        final int servantNo = Integer.parseInt(idText.getText().trim());
                        for (int i = 1; i <= servantAscTabPane.getTabs().size(); i += 1) {
                            final ServantAscensionTab ascensionTab = (ServantAscensionTab) servantAscTabPane.getTabs().get(i - 1).getContent();
                            ascensionTab.setServantNo(servantNo, i);
                        }
                    } catch (final Exception e) {
                        errorLabel.setVisible(true);
                        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot parse servant ID"));
                    }
                }
        );

        final Button addAscensionButton = new Button();
        addAscensionButton.setGraphic(createInfoImageView("add"));
        addAscensionButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Add Ascension")));
        addAscensionButton.setOnAction(e -> {
            final List<Tab> tabs = servantAscTabPane.getTabs();
            final int ascension = tabs.size() + 1;
            final String tabName = getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension;
            final int servantNo = parseServantNo();
            if (tabs.isEmpty()) {
                tabs.add(new Tab(tabName, new ServantAscensionTab(servantNo, ascension)));
            } else {
                final ServantAscensionTab base = (ServantAscensionTab) servantAscTabPane.getSelectionModel().getSelectedItem().getContent();
                tabs.add(new Tab(tabName, new ServantAscensionTab(base, servantNo, ascension)));
            }
            servantAscTabPane.getSelectionModel().selectLast();
        });

        final Button leftButton = new Button();
        leftButton.setGraphic(createInfoImageView("left"));
        leftButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Move ascension left")));
        leftButton.setOnAction(e -> {
            final ObservableList<Tab> tabs = servantAscTabPane.getTabs();
            if (tabs.isEmpty() || tabs.size() == 1) {
                return;
            }

            final int index = tabs.indexOf(servantAscTabPane.getSelectionModel().getSelectedItem());
            if (index > 0) {
                final ObservableList<Tab> workingCollection = FXCollections.observableArrayList(tabs);
                Collections.swap(workingCollection, index - 1, index);
                final int servantNo = parseServantNo();
                syncTab(workingCollection.get(index - 1), servantNo, index);
                syncTab(workingCollection.get(index), servantNo, index + 1);
                tabs.setAll(workingCollection);
            }
        });

        final Button rightButton = new Button();
        rightButton.setGraphic(createInfoImageView("right"));
        rightButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Move ascension right")));
        rightButton.setOnAction(e -> {
            final ObservableList<Tab> tabs = servantAscTabPane.getTabs();
            if (tabs.isEmpty() || tabs.size() == 1) {
                return;
            }

            final int index = tabs.indexOf(servantAscTabPane.getSelectionModel().getSelectedItem());
            if (index < tabs.size() - 1 && index >= 0) {
                final ObservableList<Tab> workingCollection = FXCollections.observableArrayList(tabs);
                Collections.swap(workingCollection, index + 1, index);
                final int servantNo = parseServantNo();
                syncTab(workingCollection.get(index + 1), servantNo, index + 2);
                syncTab(workingCollection.get(index), servantNo, index + 1);
                tabs.setAll(workingCollection);
            }
        });

        final Button removeButton = new Button();
        removeButton.setGraphic(createInfoImageView("remove"));
        removeButton.setTooltip(new Tooltip(getTranslation(APPLICATION_SECTION, "Remove Ascension")));
        removeButton.setOnAction(e -> {
            final ObservableList<Tab> tabs = servantAscTabPane.getTabs();
            final List<Tab> remainingNodes = new ArrayList<>(tabs);
            remainingNodes.remove(servantAscTabPane.getSelectionModel().getSelectedItem());
            final int servantNo = parseServantNo();
            for (int i = 0; i < remainingNodes.size(); i += 1) {
                syncTab(remainingNodes.get(i), servantNo, i + 1);
            }
            tabs.setAll(remainingNodes);
        });

        servantAscTabPane.getTabs().add(new Tab(getTranslation(APPLICATION_SECTION, "Servant Asc") + 1, new ServantAscensionTab(0, 1)));

        actionHBox.getChildren().addAll(addAscensionButton, leftButton, rightButton, removeButton);

        errorLabel.setVisible(false);
        loadButton.setText(getTranslation(APPLICATION_SECTION, "Load From"));
        loadButton.setOnAction(e -> loadFrom());
        saveButton.setText(getTranslation(APPLICATION_SECTION, "Save To"));
        saveButton.setOnAction(e -> saveTo());
    }

    private int parseServantNo() {
        int servantNo = 0;
        try {
            servantNo = Integer.parseInt(idText.getText().trim());
        } catch (final Exception ignored) {
        }
        return servantNo;
    }

    private void syncTab(final Tab tab, final int servantNo, final int ascension) {
        tab.setText(getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + ascension);
        final ServantAscensionTab ascensionTab = (ServantAscensionTab) tab.getContent();
        ascensionTab.setServantNo(servantNo, ascension);
    }

    private void loadFrom() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(SERVANT_DIRECTORY_PATH));
        fileChooser.setTitle(getTranslation(APPLICATION_SECTION, "Load Servant Data"));
        final File servantDataFile = fileChooser.showOpenDialog(null);
        if (servantDataFile == null) {
            return;
        }

        final JsonFormat.Parser parser = JsonFormat.parser();
        final ServantData.Builder servantDataBuilder = ServantData.newBuilder();
        try {
            parser.merge(readFile(servantDataFile), servantDataBuilder);
        } catch (final Exception e) {
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error loading file!") + " " + e.getMessage());
            errorLabel.setVisible(true);
            return;
        }

        loadFrom(servantDataBuilder);
    }

    private void loadFrom(final ServantData.Builder servantDataBuilder) {
        idText.setText(Integer.toString(servantDataBuilder.getServantNum()));

        servantAscTabPane.getTabs().clear();
        for (int i = 0; i < servantDataBuilder.getServantAscensionDataList().size(); i += 1) {
            final List<Tab> tabs = servantAscTabPane.getTabs();
            final String tabName = getTranslation(APPLICATION_SECTION, "Servant Asc") + " " + (tabs.size() + 1);
            final ServantAscensionTab ascensionTab = new ServantAscensionTab(
                    servantDataBuilder.getServantAscensionData(i),
                    servantDataBuilder.getServantNum(),
                    tabs.size() + 1
            );
            tabs.add(new Tab(tabName, ascensionTab));
        }
        errorLabel.setVisible(true);
        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Load success!"));
        for (final Tab tab : servantAscTabPane.getTabs()) {
            final ServantAscensionTab servantAscensionTab = (ServantAscensionTab) tab.getContent();
            servantAscensionTab.clearError();
        }
    }

    private void saveTo() {
        final int servantNo;
        try {
            servantNo = Integer.parseInt(idText.getText().trim());
        } catch (final Exception e) {
            errorLabel.setVisible(true);
            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot parse servant ID"));
            return;
        }

        final List<ServantAscensionData> servantAscensionData = new ArrayList<>();
        for (final Tab tab : servantAscTabPane.getTabs()) {
            final ServantAscensionTab servantAscensionTab = (ServantAscensionTab) tab.getContent();
            final ServantAscensionData data = servantAscensionTab.build(servantNo);
            if (data == null) {
                errorLabel.setVisible(true);
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Servant ascension data error") + tab.getText());
                return;
            }
            servantAscensionData.add(data);
        }

        final ServantData servantData = ServantData.newBuilder()
                .setServantNum(servantNo)
                .addAllServantAscensionData(servantAscensionData)
                .build();

        DataWriter.writeServant(servantData);
        errorLabel.setVisible(true);
        errorLabel.setText(getTranslation(APPLICATION_SECTION, "Save success!"));
        for (final Tab tab : servantAscTabPane.getTabs()) {
            final ServantAscensionTab servantAscensionTab = (ServantAscensionTab) tab.getContent();
            servantAscensionTab.clearError();
        }
    }

    public void setPreviewMode(final ServantData servantData) {
        saveButton.setDisable(true);
        loadButton.setDisable(true);
        loadFrom(servantData.toBuilder());
    }
}
