package yome.fgo.simulator.gui.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.CommandCodeData;

import static yome.fgo.simulator.gui.helpers.ComponentUtils.CC_THUMBNAIL_STYLE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.SERVANT_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.wrapInAnchor;

@NoArgsConstructor
@Getter
public class CommandCodeDataAnchorPane extends AnchorPane {
    private CommandCodeData commandCodeData;
    private Image image;
    private ImageView imageView;

    public CommandCodeDataAnchorPane(final CommandCodeData commandCodeData, final Image image) {
        super();
        setFrom(commandCodeData, image);
    }

    public void setFrom(final CommandCodeData commandCodeData, final Image image) {
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setStyle(CC_THUMBNAIL_STYLE);
        this.commandCodeData = commandCodeData;
        this.image = image;
        imageView = new ImageView(image);
        imageView.setFitHeight(SERVANT_THUMBNAIL_SIZE);
        imageView.setFitWidth(SERVANT_THUMBNAIL_SIZE);
        getChildren().clear();
        wrapInAnchor(this, imageView);
    }
}
