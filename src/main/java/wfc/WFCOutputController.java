package wfc;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class WFCOutputController implements Initializable {
    @FXML
    Canvas outputCanvas;

    @FXML
    FlowPane canvasHolder;

    @FXML
    Label zoomLabel;

    @FXML
    Slider zoomSlider;

    @FXML
    MenuItem saveImage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        zoomLabel.setText("Zoom - " + WFCMainController.defaultZoom + "%");

        zoomSlider.setValue(WFCMainController.defaultZoom);

        zoomSlider.valueProperty().addListener((event, oldValue, newValue) -> {
            int intValue = newValue.intValue();
            zoomLabel.setText("Zoom - " + intValue + "%");
            int canvasPixelSize = (WFCMainController.defaultCanvasPixelSize * intValue) / 100;
            BufferedImage image = WFCMainController.wfcModel.postProcessImage(canvasPixelSize);
            WritableImage writableImage = SwingFXUtils.toFXImage(image, null);
            drawOutput(writableImage);
        });

        saveImage.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();

            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files (*.png)", "*.png"));

            File saveFile = fileChooser.showSaveDialog(canvasHolder.getScene().getWindow());

            if (saveFile != null) {
                BufferedImage postProcessSave = WFCMainController.wfcModel.postProcessForSaving();
                try {
                    ImageIO.write(postProcessSave, "PNG", saveFile);
                } catch (IOException ignored) {}
            }
        });
    }

    public void drawOutput(WritableImage image) {
        outputCanvas.setWidth(image.getWidth());
        outputCanvas.setHeight(image.getHeight());

        GraphicsContext graphicsContext = outputCanvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, outputCanvas.getWidth(), outputCanvas.getHeight());
        graphicsContext.drawImage(image, 0, 0);
    }
}
