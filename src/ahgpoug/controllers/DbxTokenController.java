package ahgpoug.controllers;

import ahgpoug.Main;
import ahgpoug.objects.Task;
import ahgpoug.util.Globals;
import ahgpoug.util.ImageViewPane;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.codec.digest.DigestUtils;
import org.controlsfx.dialog.ProgressDialog;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

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
            encryptToken(dbxTokenField.getText());
    }

    private void encryptToken(String token){
        writeToFile(DigestUtils.sha1Hex("WhenTheLocustNests"));
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
            Globals.dbxToken = line;
            okClicked = true;
            dialogStage.close();
        });

        ProgressDialog progDiag = new ProgressDialog(task1);
        progDiag.setTitle("Загрузка");
        progDiag.initOwner(Main.getStage());
        progDiag.setHeaderText("Загрузка...");
        progDiag.initModality(Modality.WINDOW_MODAL);

        new Thread(task1).start();
    }
}
