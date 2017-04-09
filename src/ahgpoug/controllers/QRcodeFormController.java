package ahgpoug.controllers;

import ahgpoug.objects.Task;
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
import javafx.stage.Stage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Hashtable;

public class QRcodeFormController {
    @FXML
    private ImageView qrCode;

    private Task task;
    private Stage dialogStage;

    @FXML
    private void initialize() {
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;


    }

    public void setData(Task task) {
        this.task = task;

        try {
            qrCode.setImage(SwingFXUtils.toFXImage(createQRImage(task.getId().getValue(), 100), null));

            ImageViewPane viewPane = new ImageViewPane(qrCode);
            VBox vbox=new VBox();
            StackPane root=new StackPane();
            root.getChildren().addAll(viewPane);
            vbox.getChildren().addAll(root);
            VBox.setVgrow(root, Priority.ALWAYS);

            Scene scene= new Scene(vbox,200,200);
            dialogStage.setScene(scene);

            dialogStage.setMinHeight(200);
            dialogStage.setMinWidth(400);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private BufferedImage createQRImage(String qrCodeText, int size) throws WriterException {
        Hashtable hintMap = new Hashtable();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);
        int matrixWidth = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // Paint and save the image using the ByteMatrix
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }

        return image;
    }
}
