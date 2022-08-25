package wfc;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class WFCFailController implements Initializable {
    @FXML
    Button failButton;

    @FXML
    Label failLabel;

    boolean booleanProperty;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        failButton.setOnAction(event -> {
            booleanProperty = !booleanProperty;
            ((Stage) failButton.getScene().getWindow()).close();
        });
    }

    public void setLabel(String text) {
        failLabel.setText(text);
    }

    public void setInitialBoolean(boolean bool) {
        booleanProperty = bool;
    }

    public void setButtonText(String text) {
        failButton.setText(text);
    }

    public boolean getBooleanProperty() {
        return booleanProperty;
    }
}
