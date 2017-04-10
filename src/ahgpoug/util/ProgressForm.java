package ahgpoug.util;

import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressForm {
    private final Stage dialogStage;
    private final ProgressIndicator pin = new ProgressIndicator();

    public ProgressForm() {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        final Label label = new Label();
        label.setFont(new Font("Arial", 30));
        label.setMinSize(80, 80);
        label.setText("Загрузка...");

        pin.setMinSize(5, 5);
        final HBox hb = new HBox();
        hb.setMinSize(100, 50);
        hb.setSpacing(10);
        hb.setAlignment(Pos.CENTER);
        hb.setStyle("-fx-background: #FFFFFF;");
        hb.getChildren().addAll(pin, label);

        Scene scene = new Scene(hb);
        dialogStage.setScene(scene);
    }

    public void activateProgressBar(final Task<?> task)  {
        pin.progressProperty().bind(task.progressProperty());
        dialogStage.show();
    }

    public Stage getDialogStage() {
        return dialogStage;
    }
}