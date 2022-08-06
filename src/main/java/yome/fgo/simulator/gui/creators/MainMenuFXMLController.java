package yome.fgo.simulator.gui.creators;

import com.google.common.collect.ImmutableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class MainMenuFXMLController implements Initializable {
    @FXML
    private VBox mainMenuVBox;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        final Button simulationButton = new Button(getTranslation(APPLICATION_SECTION, "Simulation"));
        simulationButton.setOnAction(e -> {
            simulationButton.setDisable(true);
            try {
                LevelCreator.simulationPreviewMode();
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                simulationButton.setDisable(false);
            }
        });
        final Button servantCreatorButton = new Button(getTranslation(APPLICATION_SECTION, "ServantCreator"));
        servantCreatorButton.setOnAction(e -> {
            try {
                MainMenu.launch("ServantCreator", "servantCreator");
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        final Button craftEssenceCreatorButton = new Button(getTranslation(APPLICATION_SECTION, "CraftEssenceCreator"));
        craftEssenceCreatorButton.setOnAction(e -> {
            try {
                MainMenu.launch("CraftEssenceCreator", "craftEssenceCreator");
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        final Button levelCreatorButton = new Button(getTranslation(APPLICATION_SECTION, "LevelCreator"));
        levelCreatorButton.setOnAction(e -> {
            try {
                MainMenu.launch("LevelCreator", "levelCreator", true);
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        final Button enemyCreatorButton = new Button(getTranslation(APPLICATION_SECTION, "EnemyCreator"));
        enemyCreatorButton.setOnAction(e -> {
            try {
                MainMenu.launch("EnemyCreator", "enemyCreator");
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        final Button commandCodeCreatorButton = new Button(getTranslation(APPLICATION_SECTION, "CommandCodeCreator"));
        commandCodeCreatorButton.setOnAction(e -> {
            try {
                MainMenu.launch("CommandCodeCreator", "commandCodeCreator");
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        final Button mysticCodeCreatorButton = new Button(getTranslation(APPLICATION_SECTION, "MysticCodeCreator"));
        mysticCodeCreatorButton.setOnAction(e -> {
            try {
                MainMenu.launch("MysticCodeCreator", "mysticCodeCreator");
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        final List<Button> buttons = ImmutableList.of(
                simulationButton,
                servantCreatorButton,
                craftEssenceCreatorButton,
                levelCreatorButton,
                enemyCreatorButton,
                commandCodeCreatorButton,
                mysticCodeCreatorButton
        );
        for (final Button button : buttons) {
            final AnchorPane wrapper = new AnchorPane();
            wrapper.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            AnchorPane.setBottomAnchor(button, 0.0);
            AnchorPane.setTopAnchor(button, 0.0);
            AnchorPane.setLeftAnchor(button, 0.0);
            AnchorPane.setRightAnchor(button, 0.0);

            VBox.setVgrow(wrapper, Priority.ALWAYS);
            button.setPrefSize(550, 50);
            wrapper.getChildren().add(button);
            mainMenuVBox.getChildren().addAll(wrapper);
        }
    }
}
