package ahgpoug.controllers;

import ahgpoug.objects.Task;
import ahgpoug.mySql.MySqlHelper;
import ahgpoug.mySql.MySqlTasks;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SingleTaskController {
    @FXML
    private Label pdfLabel;

    @FXML
    private TextField taskNameField;
    @FXML
    private TextField groupNameField;
    @FXML
    private DatePicker dateField;

    private Stage dialogStage;
    private Task task;
    private boolean okClicked = false;

    @FXML
    private void initialize() {
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;

        dateField.setConverter(new StringConverter<LocalDate>()
        {
            private DateTimeFormatter dateTimeFormatter= DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate localDate)
            {
                if(localDate==null)
                    return "";
                return dateTimeFormatter.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString)
            {
                if(dateString==null || dateString.trim().isEmpty())
                {
                    return null;
                }
                return LocalDate.parse(dateString,dateTimeFormatter);
            }
        });
    }

    public void setData(Task task) {
        this.task = task;
        if (task != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            taskNameField.setText(task.getTaskName().getValue());
            groupNameField.setText(task.getGroupName().getValue());
            pdfLabel.setText(task.getPDFstate().getValue());
            dateField.setValue(LocalDate.parse(task.getExpDate().getValue(), formatter));
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
            if (task == null) {
                MySqlTasks.AddNewTask addNewTask = new MySqlTasks().new AddNewTask(taskNameField.getText(), groupNameField.getText(), dateField.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                Thread th = new Thread(addNewTask);
                th.start();

                addNewTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
                    if (addNewTask.getValue()) {
                        okClicked = true;
                        dialogStage.close();
                    }
                });
            } else {
                MySqlHelper.editExistingTask(task, taskNameField.getText(), groupNameField.getText(), dateField.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                okClicked = true;
                dialogStage.close();
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }


    private boolean isInputValid(){
        boolean result = true;
        if (taskNameField.getText() == null || taskNameField.getText().length() == 0) {
            result = false;
        }

        if (groupNameField.getText() == null || groupNameField.getText().length() == 0) {
            result = false;
        }

        if (dateField.getValue() == null || dateField.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).length() == 0) {
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
}
