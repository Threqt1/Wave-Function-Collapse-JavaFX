package wfc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class WFCApplication extends Application {
    static final String version = "1.6.1";
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/wfc/WFCMain.fxml")));
        primaryStage.setTitle("Wave Function Collapse Demo - v" + version);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
