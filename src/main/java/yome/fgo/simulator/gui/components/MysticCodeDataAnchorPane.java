package yome.fgo.simulator.gui.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.data.proto.FgoStorageData.MysticCodeData;

import java.util.List;

import static yome.fgo.simulator.gui.helpers.ComponentUtils.SERVANT_THUMBNAIL_SIZE;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.wrapInAnchor;

@NoArgsConstructor
@Getter
public class MysticCodeDataAnchorPane extends AnchorPane {
    private MysticCodeData mysticCodeData;
    private List<Image> images;
    private ImageView imageView;
    private Gender gender;

    public MysticCodeDataAnchorPane(final MysticCodeData mysticCodeData, final List<Image> images) {
        super();
        setFrom(mysticCodeData, images, Gender.MALE);
    }

    public void setFrom(final MysticCodeData mysticCodeData, final List<Image> images, final Gender gender) {
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        this.mysticCodeData = mysticCodeData;
        this.images = images;
        this.gender = gender;
        imageView = new ImageView(images.get(0));
        imageView.setFitHeight(SERVANT_THUMBNAIL_SIZE);
        imageView.setFitWidth(SERVANT_THUMBNAIL_SIZE);
        getChildren().clear();
        wrapInAnchor(this, imageView);
        setFromGender(gender);
    }

    public void setFromGender(final Gender gender) {
        this.gender = gender;
        final Image imageToSet = gender == Gender.MALE ?
                images.get(0) :
                images.get(1);
        imageView.setImage(imageToSet);
    }
}
