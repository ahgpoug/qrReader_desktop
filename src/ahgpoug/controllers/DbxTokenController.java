package ahgpoug.controllers;

import ahgpoug.util.Crypto;
import ahgpoug.util.Globals;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


public class DbxTokenController {
    @FXML
    private TextField dbxTokenField;

    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    private void initialize() {
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        dialogStage.setTitle("Добавление Access Token'а");
    }

    @FXML
    private void handleOk() {
        if (dbxTokenField.getText() != null && dbxTokenField.getText().length() > 0)
            encryptToken(dbxTokenField.getText().trim());
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    private void encryptToken(String token){
        try {
            writeToFile(Crypto.encrypt(token));
        } catch (Exception e){
            e.printStackTrace();
            dialogStage.close();
        }
    }

    private void writeToFile(String line){
        javafx.concurrent.Task<Boolean> task1 = new javafx.concurrent.Task<Boolean>() {
            @Override public Boolean call() {
                try{
                    PrintWriter writer = new PrintWriter("dbx", "UTF-8");
                    writer.println(line);
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        task1.setOnSucceeded((e) -> {
            Globals.dbxToken = dbxTokenField.getText().trim();
            File file = new File("sqlite.db");
            if (file.exists())
                file.delete();
            okClicked = true;
            dialogStage.close();
        });

        new Thread(task1).start();
    }
}
