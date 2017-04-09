package ahgpoug;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("qrReader");

        initRootLayout();
    }

    private void initRootLayout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("views/MainLayout.fxml"));
            primaryStage.setTitle("qrReader");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getStage(){
        return primaryStage;
    }
}
