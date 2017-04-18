package ahgpoug.controllers;

import ahgpoug.Main;
import ahgpoug.dbx.DbxHelper;
import ahgpoug.objects.Task;
import ahgpoug.sqlite.SqliteHelper;
import ahgpoug.sqlite.SqliteTasks;
import ahgpoug.util.Crypto;
import ahgpoug.util.Globals;
import com.sun.jna.platform.win32.Advapi32Util;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.dialog.ProgressDialog;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static com.sun.jna.platform.win32.WinReg.HKEY_CURRENT_USER;

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
    private Label infoLabel;

    @FXML
    private GridPane infoPane;

    private boolean isValidToken = true;

    @FXML
    private void initialize() {
        checkToken();
        updateDb();

        taskNameColumn.setCellValueFactory(cellData -> cellData.getValue().getTaskName());
        groupNameColumn.setCellValueFactory(cellData -> cellData.getValue().getGroupName());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().getExpDate());
        pdfColumn.setCellValueFactory(cellData -> cellData.getValue().getPDFstate());

        showData(null);
        tasksTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showData(newValue));
    }

    private void showData(Task task){
        if (task != null) {
            infoPane.setVisible(true);
            infoLabel.setVisible(true);

            taskNameLabel.setText(task.getTaskName().getValue());
            groupNameLabel.setText(task.getGroupName().getValue());
            dateLabel.setText(task.getExpDate().getValue());
            pdfLabel.setText(task.getPDFstate().getValue());
        } else {
            infoPane.setVisible(false);
            infoLabel.setVisible(false);
        }
    }

    private void updateTable(int index){
        tasksTable.setPlaceholder(new Label("Загрузка данных..."));
        if (!DbxHelper.Token.checkToken(Globals.dbxToken)) {
            tasksTable.setItems(null);
            tasksTable.setPlaceholder(new Label("Не удалось получить данные.\nВозможно, введен неправильный токен\nили отсутствует подключение к интернету"));
            isValidToken = false;
        } else {
            isValidToken = true;

            SqliteTasks.GetAllTasks getAllTasks = new SqliteTasks().new GetAllTasks();
            Thread th = new Thread(getAllTasks);
            th.start();

            getAllTasks.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
                ObservableList<Task> list = getAllTasks.getValue();
                if (list != null && list.size() > 0) {
                    tasksTable.setItems(list);
                    if (index != -1) {
                        tasksTable.getSelectionModel().select(index);
                        infoPane.setVisible(true);
                        infoLabel.setVisible(true);
                    } else {
                        infoPane.setVisible(false);
                        infoLabel.setVisible(false);
                    }
                } else {
                    tasksTable.setPlaceholder(new Label("Данных нет"));

                    infoPane.setVisible(false);
                    infoLabel.setVisible(false);
                }
            });

            getAllTasks.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, e -> {
                System.out.println("Failed");
                tasksTable.setPlaceholder(new Label("Не удалось получить данные.\nВозможно, введен неправильный токен\nили отсутствует подключение к интернету"));
            });
        }
    }

    private void updateDb(){
        javafx.concurrent.Task<Boolean> task1 = new javafx.concurrent.Task<Boolean>() {
            @Override public Boolean call() {
                DbxHelper.Files.downloadDb();
                return null;
            }
        };

        task1.setOnSucceeded((e) -> {
            updateTable(-1);
        });

        tasksTable.setItems(null);
        tasksTable.setPlaceholder(new Label("Загрузка данных..."));

        new Thread(task1).start();
    }

    private void checkToken(){
        try {
            if (Advapi32Util.registryKeyExists(HKEY_CURRENT_USER, Globals.registryPath) && Advapi32Util.registryValueExists(HKEY_CURRENT_USER, Globals.registryPath, "token")) {
                String token = Advapi32Util.registryGetStringValue(HKEY_CURRENT_USER, Globals.registryPath, "token");
                Globals.dbxToken = Crypto.decrypt(token);
            } else {
                showDbxTokenDialog();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteTask() {
        int index = tasksTable.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Удаление данных");
            alert.setHeaderText("Удаление всех данных по заданию");
            alert.setContentText("Вы уверены?");
            alert.initOwner(Main.getStage());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                executeDeleteTask(tasksTable.getSelectionModel().getSelectedItem());
            } else {
                alert.close();
            }
        }
    }

    @FXML
    private void handleAddTask() {
        if (isValidToken) {
            boolean okClicked = showTaskDialog(null);
            if (okClicked) {
                updateTable(tasksTable.getSelectionModel().getSelectedIndex());
            }
        }
    }

    @FXML
    private void handleEditTask() {
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            boolean okClicked = showTaskDialog(selectedTask);
            if (okClicked) {
                updateTable(tasksTable.getSelectionModel().getSelectedIndex());
            }
        }
    }

    @FXML
    private void handlePDFupload() {
        if (tasksTable.getSelectionModel().getSelectedIndex() >= 0) {
            Task task = tasksTable.getSelectionModel().getSelectedItem();
            FileChooser chooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
            chooser.getExtensionFilters().add(extFilter);
            chooser.setTitle("Выбрать файл");
            File file = chooser.showOpenDialog(new Stage());
            executeUploadFile(file, task);
        }
    }

    @FXML
    private void handleFolderShow() {
        if (tasksTable.getSelectionModel().getSelectedIndex() >= 0) {
            executeShowFolder(tasksTable.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    private void handlePDFshow() {
        if (tasksTable.getSelectionModel().getSelectedIndex() >= 0) {
            Task task = tasksTable.getSelectionModel().getSelectedItem();
            if (task.isHasPDF())
                executeShowFile(task);
        }
    }

    @FXML
    private void handlePDFremove() {
        if (tasksTable.getSelectionModel().getSelectedIndex() >= 0) {
            Task task = tasksTable.getSelectionModel().getSelectedItem();
            if (task.isHasPDF())
                executeRemoveFile(task);
        }
    }

    @FXML
    private void handleQrShow() {
        if (tasksTable.getSelectionModel().getSelectedIndex() >= 0)
            showQrCodeDialog(tasksTable.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void handleChangeToken() {
        boolean okClicked = showDbxTokenDialog();
        if (okClicked) {
            updateDb();
        }
    }

    private void executeDeleteTask(Task task){
        javafx.concurrent.Task<Boolean> task1 = new javafx.concurrent.Task<Boolean>() {
            @Override public Boolean call() {
                DbxHelper.Files.removeTaskPDF(task);
                DbxHelper.Folders.removeFolder(task);
                SqliteHelper.removeTask(task);
                return null;
            }
        };

        task1.setOnSucceeded((e) -> {
            tasksTable.getItems().remove(tasksTable.getSelectionModel().getSelectedIndex());
            updateTable(tasksTable.getSelectionModel().getSelectedIndex());
        });

        ProgressDialog progDiag = new ProgressDialog(task1);
        progDiag.setTitle("Загрузка");
        progDiag.initOwner(Main.getStage());
        progDiag.setHeaderText("Удаление всех данных...");
        progDiag.initModality(Modality.WINDOW_MODAL);

        new Thread(task1).start();
    }

    private void executeUploadFile(File file, Task task){
        javafx.concurrent.Task<Boolean> task1 = new javafx.concurrent.Task<Boolean>() {
            @Override public Boolean call() {
                DbxHelper.Files.uploadTaskPDF(file, task);
                return null;
            }
        };

        task1.setOnSucceeded((e) -> {
            updateTable(tasksTable.getSelectionModel().getSelectedIndex());
        });

        ProgressDialog progDiag = new ProgressDialog(task1);
        progDiag.setTitle("Загрузка");
        progDiag.initOwner(Main.getStage());
        progDiag.setHeaderText("Загрузка файла на сервер...");
        progDiag.initModality(Modality.WINDOW_MODAL);

        new Thread(task1).start();
    }

    private void executeShowFile(Task task){
        javafx.concurrent.Task<Boolean> task1 = new javafx.concurrent.Task<Boolean>() {
            @Override public Boolean call() {
                DbxHelper.Files.showTaskPDF(task);
                return null;
            }
        };

        ProgressDialog progDiag = new ProgressDialog(task1);
        progDiag.setTitle("Загрузка");
        progDiag.initOwner(Main.getStage());
        progDiag.setHeaderText("Получение ссылки на файл...");
        progDiag.initModality(Modality.WINDOW_MODAL);

        new Thread(task1).start();
    }

    private void executeShowFolder(Task task){
        javafx.concurrent.Task<Boolean> task1 = new javafx.concurrent.Task<Boolean>() {
            @Override public Boolean call() {
                DbxHelper.Folders.showFolder(task);
                return null;
            }
        };

        ProgressDialog progDiag = new ProgressDialog(task1);
        progDiag.setTitle("Загрузка");
        progDiag.initOwner(Main.getStage());
        progDiag.setHeaderText("Получение ссылки на каталог...");
        progDiag.initModality(Modality.WINDOW_MODAL);

        new Thread(task1).start();
    }

    private void executeRemoveFile(Task task){
        javafx.concurrent.Task<Boolean> task1 = new javafx.concurrent.Task<Boolean>() {
            @Override public Boolean call() {
                DbxHelper.Files.removeTaskPDF(task);
                return null;
            }
        };
        task1.setOnSucceeded((e) -> {
            updateTable(tasksTable.getSelectionModel().getSelectedIndex());
        });

        ProgressDialog progDiag = new ProgressDialog(task1);
        progDiag.setTitle("Загрузка");
        progDiag.initOwner(Main.getStage());
        progDiag.setHeaderText("Удаление файла...");
        progDiag.initModality(Modality.WINDOW_MODAL);

        new Thread(task1).start();
    }

    private boolean showTaskDialog(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("views/EditTaskLayout.fxml"));
            AnchorPane page = loader.load();

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

    private void showQrCodeDialog(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("views/QRcodeLayout.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("QR code");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(Main.getStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.getIcons().add(new Image("file:resources/images/icon.png"));

            QrCodeFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setData(task);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean showDbxTokenDialog() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("views/DbxTokenLayout.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(Main.getStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.getIcons().add(new Image("file:resources/images/icon.png"));

            DbxTokenController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
