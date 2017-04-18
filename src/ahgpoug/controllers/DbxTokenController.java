package ahgpoug.controllers;

import ahgpoug.util.Crypto;
import ahgpoug.util.Globals;
import com.sun.jna.platform.win32.Advapi32Util;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static com.sun.jna.platform.win32.WinReg.HKEY_CURRENT_USER;


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
            Globals.dbxToken = token;
            writeToRegistry(Crypto.encrypt(token));
        } catch (Exception e){
            e.printStackTrace();
            dialogStage.close();
        }
    }

    private void writeToRegistry(String line){
        if (!Advapi32Util.registryKeyExists(HKEY_CURRENT_USER, Globals.registryPath))
            Advapi32Util.registryCreateKey(HKEY_CURRENT_USER, Globals.registryPath);

        Advapi32Util.registrySetStringValue(HKEY_CURRENT_USER, Globals.registryPath, "token", line);

        File file = new File("sqlite.db");
        if (file.exists())
            file.delete();

        okClicked = true;
        dialogStage.close();
    }
}
