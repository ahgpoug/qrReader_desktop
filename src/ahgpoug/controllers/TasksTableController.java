package ahgpoug.controllers;

import ahgpoug.Main;
import ahgpoug.objects.Task;
import ahgpoug.util.DbxHelper;
import ahgpoug.util.MySQLhelper;
import ahgpoug.util.ProgressForm;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class TasksTableController {
    @FXML
    private TableView<Task> tasksTable;
    @FXML
    private TableColumn<Task, String> taskNameColumn;
    @FXML
    private TableColumn<Task, String> groupNameColumn;
    @FXML
    private TableColumn<Task, String> dateColumn;
    @FXML
    private TableColumn<Task, String> pdfColumn;

    @FXML
    private Label taskNameLabel;
    @FXML
    private Label groupNameLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label pdfLabel;

    @FXML
    private void initialize() {
        tasksTable.setItems(MySQLhelper.getAllTasks());

        taskNameColumn.setCellValueFactory(cellData -> cellData.getValue().getTaskName());
        groupNameColumn.setCellValueFactory(cellData -> cellData.getValue().getGroupName());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().getExpDate());
        pdfColumn.setCellValueFactory(cellData -> cellData.getValue().getPDFstate());

        showData(null);
        tasksTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showData(newValue));
    }

    private void showData(Task task){
        if (task != null) {
            taskNameLabel.setText(task.getTaskName().getValue());
            groupNameLabel.setText(task.getGroupName().getValue());
            dateLabel.setText(task.getExpDate().getValue());
            pdfLabel.setText(task.getPDFstate().getValue());
        } else {
            taskNameLabel.setText("");
            groupNameLabel.setText("");
            dateLabel.setText("");
            pdfLabel.setText("");
        }
    }

    @FXML
    private void handleDeleteTask() {
        int selectedIndex = tasksTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            MySQLhelper.removeTask(tasksTable.getItems().get(selectedIndex));
            tasksTable.getItems().remove(selectedIndex);
        }
    }

    @FXML
    private void handleAddTask() {
        boolean okClicked = showTaskDialog(null);
        if (okClicked) {
            tasksTable.setItems(MySQLhelper.getAllTasks());
        }
    }

    @FXML
    private void handleEditTask() {
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            boolean okClicked = showTaskDialog(selectedTask);
            if (okClicked) {
                tasksTable.setItems(MySQLhelper.getAllTasks());
            }
        }
    }

    @FXML
    private void handlePDFupload() {
        int selectedIndex = tasksTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Task task = tasksTable.getItems().get(selectedIndex);
            FileChooser chooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
            chooser.getExtensionFilters().add(extFilter);
            chooser.setTitle("Выбрать файл");
            File file = chooser.showOpenDialog(new Stage());
            executeUploadTask(file, task);
        }
    }

    @FXML
    private void handlePDFshow() {
        int selectedIndex = tasksTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Task task = tasksTable.getItems().get(selectedIndex);
            if (task.isHasPDF())
                executeShowTask(task);
        }
    }

    @FXML
    private void handlePDFremove() {
        int selectedIndex = tasksTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Task task = tasksTable.getItems().get(selectedIndex);
            if (task.isHasPDF())
                executeRemoveTask(task);
        }
    }

    private void executeUploadTask(File file, Task task){
        ProgressForm pForm = new ProgressForm();

        javafx.concurrent.Task<Boolean> task1 = new javafx.concurrent.Task<Boolean>() {
            @Override public Boolean call() {
                DbxHelper.uploadFile(file, task);
                return null;
            }
        };
        task1.setOnRunning((e) -> {
            pForm.activateProgressBar(task1);
        });
        task1.setOnSucceeded((e) -> {
            int selectedIndex = tasksTable.getSelectionModel().getSelectedIndex();
            tasksTable.setItems(MySQLhelper.getAllTasks());
            tasksTable.getSelectionModel().select(selectedIndex);
            pForm.getDialogStage().close();
        });
        new Thread(task1).start();
    }

    private void executeShowTask(Task task){
        ProgressForm pForm = new ProgressForm();

        javafx.concurrent.Task<Boolean> task1 = new javafx.concurrent.Task<Boolean>() {
            @Override public Boolean call() {
                DbxHelper.showFile(task);
                return null;
            }
        };
        task1.setOnRunning((e) -> {
            pForm.activateProgressBar(task1);
        });
        task1.setOnSucceeded((e) -> {
            pForm.getDialogStage().close();
        });
        new Thread(task1).start();
    }

    private void executeRemoveTask(Task task){
        ProgressForm pForm = new ProgressForm();

        javafx.concurrent.Task<Boolean> task1 = new javafx.concurrent.Task<Boolean>() {
            @Override public Boolean call() {
                DbxHelper.removeFile(task);

                return null;
            }
        };
        task1.setOnRunning((e) -> {
            pForm.activateProgressBar(task1);
        });
        task1.setOnSucceeded((e) -> {
            int selectedIndex = tasksTable.getSelectionModel().getSelectedIndex();
            tasksTable.setItems(MySQLhelper.getAllTasks());
            tasksTable.getSelectionModel().select(selectedIndex);
            pForm.getDialogStage().close();
        });
        new Thread(task1).start();
    }

    private boolean showTaskDialog(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("views/EditTaskLayout.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("qrReader");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(Main.getStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.getIcons().add(new Image("file:resources/images/icon.png"));


            SingleTaskController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setData(task);

            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
