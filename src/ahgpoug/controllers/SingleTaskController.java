package ahgpoug.controllers;

import ahgpoug.Main;
import ahgpoug.objects.Task;
import ahgpoug.util.DbxHelper;
import ahgpoug.util.MaskField;
import ahgpoug.util.MySQLhelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class SingleTaskController {
    @FXML
    private Label pdfLabel;

    @FXML
    private TextField taskNameField;
    @FXML
    private TextField groupNameField;
    @FXML
    private MaskField dateField;

    private Stage dialogStage;
    private Task task;
    private boolean okClicked = false;

    @FXML
    private void initialize() {
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setData(Task task) {
        this.task = task;

        if (task != null) {
            taskNameField.setText(task.getTaskName().getValue());
            groupNameField.setText(task.getGroupName().getValue());
            dateField.setText(task.getExpDate().getValue());
            pdfLabel.setText(task.getPDFstate().getValue());
        } else {
            pdfLabel.setText("✘");
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            if (task == null)
                MySQLhelper.addNewTask(taskNameField.getText(), groupNameField.getText(), dateField.getText());
            else
                MySQLhelper.editExistingTask(task, taskNameField.getText(), groupNameField.getText(), dateField.getText());

            okClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    @FXML
    private void handleQRshow() {
        showQrCodeDialog(task);
    }

    private boolean isInputValid(){
        boolean result = true;
        if (taskNameField.getText() == null || taskNameField.getText().length() == 0) {
            result = false;
        }

        if (groupNameField.getText() == null || groupNameField.getText().length() == 0) {
            result = false;
        }

        if (dateField.getText() == null || dateField.getText().length() == 0) {
            result = false;
        }

        if (!result) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Ошибка");
            alert.setContentText("Все поля должны быть заполнены");
            alert.showAndWait();
        }

        return result;
    }

    private void showQrCodeDialog(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("views/QRcodeLayout.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("QR code");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(Main.getStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            QRcodeFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setData(task);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
