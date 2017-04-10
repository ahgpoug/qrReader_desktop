package ahgpoug.controllers;

import ahgpoug.Main;
import ahgpoug.dbx.DbxHelper;
import ahgpoug.objects.Task;
import ahgpoug.mySql.MySqlHelper;
import ahgpoug.mySql.MySqlTasks;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.dialog.ProgressDialog;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

            dialogStage.setTitle("Редактирование задания");
            taskNameField.setEditable(false);
            groupNameField.setEditable(false);
        } else {
            pdfLabel.setText("✘");
            dialogStage.setTitle("Добавление задания");
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        checkData();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void checkData(){
        MySqlTasks.GetAllTasks getAllTasks = new MySqlTasks().new GetAllTasks();
        Thread th = new Thread(getAllTasks);

        getAllTasks.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
            ObservableList<Task> list = getAllTasks.getValue();
            if (isInputValid() && correctFields(list)) {
                if (task == null) {
                    MySqlTasks.AddNewTask addNewTask = new MySqlTasks().new AddNewTask(taskNameField.getText(), groupNameField.getText(), dateField.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    Thread th1 = new Thread(addNewTask);

                    addNewTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, ee -> {
                        if (addNewTask.getValue()) {
                            okClicked = true;
                            dialogStage.close();
                        }
                    });

                    ProgressDialog progDiag = new ProgressDialog(addNewTask);
                    progDiag.setTitle("Загрузка");
                    progDiag.initOwner(Main.getStage());
                    progDiag.setHeaderText("Проверка данных...");
                    progDiag.initModality(Modality.WINDOW_MODAL);

                    th1.start();
                } else {
                    MySqlHelper.editExistingTask(task, taskNameField.getText(), groupNameField.getText(), dateField.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    okClicked = true;
                    dialogStage.close();
                }
            }
        });


        ProgressDialog progDiag = new ProgressDialog(getAllTasks);
        progDiag.setTitle("Загрузка");
        progDiag.initOwner(Main.getStage());
        progDiag.setHeaderText("Проверка данных...");
        progDiag.initModality(Modality.WINDOW_MODAL);

        th.start();
    }


    private boolean correctFields(ObservableList<Task> list){
        boolean result = true;
        if (list != null) {
            for (Task singleTask : list) {
                if (singleTask.getTaskName().getValue().toLowerCase().equals(taskNameField.getText().toLowerCase()) && singleTask.getGroupName().getValue().toLowerCase().equals(groupNameField.getText().toLowerCase())) {
                    result = false;
                }
            }
        }

        if (!result) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Задание уже существует");
            alert.setContentText("Пожулауйста, введите другие данные");
            alert.showAndWait();
        }

        return result;
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
            alert.setHeaderText("Все поля должны быть заполнены");
            alert.setContentText("Пожалуйста, заполните все поля");
            alert.showAndWait();
        }

        return result;
    }
}
