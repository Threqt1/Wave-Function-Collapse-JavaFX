package wfc;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONArray;
import org.json.JSONObject;
import wfc.algorithm.Overlapping;
import wfc.algorithm.overlapping.Pattern;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class WFCMainController implements Initializable {
    @FXML
    BorderPane mainPane;

    @FXML
    MenuItem importPattern;

    @FXML
    MenuButton templateSelect;

    @FXML
    Canvas canvas;

    @FXML
    ColorPicker colorPicker;

    @FXML
    Button fillButton;

    @FXML
    Label widthLabel;

    @FXML
    Slider widthSlider;

    @FXML
    Label heightLabel;

    @FXML
    Slider heightSlider;

    @FXML
    Label tileLabel;

    @FXML
    Slider tileSlider;

    @FXML
    Label variationLabel;

    @FXML
    Slider variationSlider;

    @FXML
    Label retriesLabel;

    @FXML
    Slider retriesSlider;

    @FXML
    TextField groundInput;

    @FXML
    Label outWidthLabel;

    @FXML
    Slider outWidthSlider;

    @FXML
    Label outHeightLabel;

    @FXML
    Slider outHeightSlider;

    @FXML
    Button generateButton;

    @FXML
    Button resetButton;

    @FXML
    Label zoomLabel;

    @FXML
    Slider zoomSlider;

    boolean currentlyGenerating;

    WFCImage bufferImage;
    int canvasWidth;
    int canvasHeight;
    Color canvasColor;
    int tileSize;
    int variation;
    int retries;
    int ground;
    int outWidth;
    int outHeight;
    int canvasPixelSize;
    int contextMenuPixelX;
    int contextMenuPixelY;

    int[] groundXY;

    public static int defaultCanvasWidth = 20;
    public static int defaultCanvasHeight = 20;
    public static Color defaultCanvasColor = Color.BLACK;

    public static int defaultTileSize = 3;
    public static int defaultVariation = 8;
    public static int defaultRetries = 5;
    public static int defaultGround = 0;

    public static int defaultOutWidth = 48;
    public static int defaultOutHeight = 48;

    public static int defaultZoom = 100;
    public static int defaultCanvasPixelSize = 12;

    public static Overlapping wfcModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /* Default Values */
        currentlyGenerating = false;
        canvasPixelSize = defaultCanvasPixelSize;
        canvasWidth = defaultCanvasWidth * canvasPixelSize;
        canvasHeight = defaultCanvasHeight * canvasPixelSize;

        canvas.setWidth(canvasWidth);
        canvas.setHeight(canvasHeight);

        canvasColor = defaultCanvasColor;
        colorPicker.setValue(canvasColor);

        widthLabel.setText("Canvas Width - " + defaultCanvasWidth);
        heightLabel.setText("Canvas Height - " + defaultCanvasHeight);
        tileLabel.setText("Tile Size - " + defaultTileSize);
        variationLabel.setText("Variation - " + defaultVariation);
        retriesLabel.setText("Retries - " + defaultRetries);
        outWidthLabel.setText("Output Width - " + defaultOutWidth);
        outHeightLabel.setText("Output Height - " + defaultOutHeight);
        zoomLabel.setText("Zoom - " + defaultZoom + "%");

        widthSlider.setValue(defaultCanvasWidth);
        heightSlider.setValue(defaultCanvasHeight);
        tileSlider.setValue(defaultTileSize);
        variationSlider.setValue(defaultVariation);
        retriesSlider.setValue(defaultRetries);
        groundInput.setText("" + defaultGround);
        outWidthSlider.setValue(defaultOutWidth);
        outHeightSlider.setValue(defaultOutHeight);
        zoomSlider.setValue(defaultZoom);

        tileSize = defaultTileSize;
        variation = defaultVariation;
        retries = defaultRetries;
        outWidth = defaultOutWidth;
        outHeight = defaultOutHeight;
        ground = defaultGround;
        groundXY = new int[]{0, 0};

        /* Setup Algorithm */
        bufferImage = new WFCImage(defaultCanvasWidth, defaultCanvasHeight, 50, 50);
        wfcModel = new Overlapping(bufferImage);

        /* Listeners for sliders and colorPicker */
        widthSlider.valueProperty().addListener((event, oldValue, newValue) -> {
            int newInt = newValue.intValue();
            widthLabel.setText("Canvas Width - " + newInt);
            bufferImage.setWidth(newInt);
            canvasWidth = newInt * canvasPixelSize;
            canvas.setWidth(canvasWidth);
            handleGroundChange(groundXY[0], groundXY[1]);
        });
        heightSlider.valueProperty().addListener((event, oldValue, newValue) -> {
            int newInt = newValue.intValue();
            heightLabel.setText("Canvas Height - " + newInt);
            bufferImage.setHeight(newInt);
            canvasHeight = newInt * canvasPixelSize;
            canvas.setHeight(canvasHeight);
            handleGroundChange(groundXY[0], groundXY[1]);
        });
        tileSlider.valueProperty().addListener((event, oldValue, newValue) -> {
            tileSize = newValue.intValue();
            tileLabel.setText("Tile Size - " + tileSize);
            wfcModel.loadNew(tileSize, canvasPixelSize, variation, ground, outWidth, outHeight);
            handleGroundChange(groundXY[0], groundXY[1]);
        });
        variationSlider.valueProperty().addListener((event, oldValue, newValue) -> {
            variation = newValue.intValue();
            variationLabel.setText("Variation - " + variation);
            wfcModel.loadNew(tileSize, canvasPixelSize, variation, ground, outWidth, outHeight);
            handleGroundChange(groundXY[0], groundXY[1]);
        });
        retriesSlider.valueProperty().addListener((event, oldValue, newValue) -> {
            retries = newValue.intValue();
            retriesLabel.setText("Retries - " + retries);
        });
        outWidthSlider.valueProperty().addListener((event, oldValue, newValue) -> {
            outWidth = newValue.intValue();
            outWidthLabel.setText("Output Width - " + outWidth);
        });
        outHeightSlider.valueProperty().addListener((event, oldValue, newValue) -> {
            outHeight = newValue.intValue();
            outHeightLabel.setText("Output Height - " + outHeight);
        });

        /* Drawing on canvas */
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        canvas.setOnMouseDragged(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown() || mouseEvent.getX() > canvasWidth || mouseEvent.getY() > canvasHeight || mouseEvent.getX() < 0 || mouseEvent.getY() < 0)
                return;
            int x = (int) mouseEvent.getX() / canvasPixelSize;
            int y = (int) mouseEvent.getY() / canvasPixelSize;
            graphicsContext.setFill(canvasColor);
            graphicsContext.fillRect(x * canvasPixelSize, y * canvasPixelSize, canvasPixelSize, canvasPixelSize);
            bufferImage.setPixel(x, y, canvasColor);
            handleGroundChange(groundXY[0], groundXY[1]);
        });
        canvas.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.PRIMARY || mouseEvent.getX() > canvasWidth || mouseEvent.getY() > canvasHeight || mouseEvent.getX() < 0 || mouseEvent.getY() < 0)
                return;
            int x = (int) mouseEvent.getX() / canvasPixelSize;
            int y = (int) mouseEvent.getY() / canvasPixelSize;
            graphicsContext.setFill(canvasColor);
            graphicsContext.fillRect(x * canvasPixelSize, y * canvasPixelSize, canvasPixelSize, canvasPixelSize);
            bufferImage.setPixel(x, y, canvasColor);
            handleGroundChange(groundXY[0], groundXY[1]);
        });
        colorPicker.setOnAction(event -> canvasColor = colorPicker.getValue());
        fillButton.setOnAction(event -> {
            graphicsContext.setFill(canvasColor);
            graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);
            for (int y = 0; y < bufferImage.getHeight(); y++) {
                for (int x = 0; x < bufferImage.getWidth(); x++) {
                    bufferImage.setPixel(x, y, canvasColor);
                }
            }
            handleGroundChange(groundXY[0], groundXY[1]);
        });
        resetButton.setOnAction(event -> {
            graphicsContext.clearRect(0, 0, canvasWidth, canvasHeight);
            for (int y = 0; y < heightSlider.valueProperty().getValue().intValue(); y++) {
                for (int x = 0; x < widthSlider.valueProperty().getValue().intValue(); x++) {
                    bufferImage.setPixel(x, y, Color.WHITE);
                }
            }
            widthSlider.valueProperty().setValue(defaultCanvasWidth);
            heightSlider.valueProperty().setValue(defaultCanvasHeight);
            tileSlider.valueProperty().setValue(defaultTileSize);
            variationSlider.valueProperty().setValue(defaultVariation);
            retriesSlider.valueProperty().setValue(defaultRetries);
            groundInput.setText("");
            ground = 0;
            groundXY[0] = 0;
            groundXY[1] = 0;
            outWidthSlider.valueProperty().setValue(defaultOutWidth);
            outHeightSlider.valueProperty().setValue(defaultOutHeight);
        });
        generateButton.setOnAction(event -> {
            if (currentlyGenerating) return;
            currentlyGenerating = true;
            wfcModel.loadNew(tileSize, canvasPixelSize, variation, ground, outWidth, outHeight);
            generateOutput();
        });

        zoomSlider.valueProperty().addListener((event, oldValue, newValue) -> {
            int intValue = newValue.intValue();
            zoomLabel.setText("Zoom - " + intValue + "%");
            canvasPixelSize = (defaultCanvasPixelSize * intValue) / 100;
            canvasWidth = bufferImage.getWidth() * canvasPixelSize;
            canvas.setWidth(canvasWidth);
            canvasHeight = bufferImage.getHeight() * canvasPixelSize;
            canvas.setHeight(canvasHeight);
            graphicsContext.clearRect(0, 0, canvasWidth, canvasHeight);
            int[] rawPixels = bufferImage.getPixelData();
            for (int y = 0; y < bufferImage.getHeight(); y++) {
                for (int x = 0; x < bufferImage.getWidth(); x++) {
                    int pixelIndex = (y * bufferImage.getArrWidth() + x) * 4;
                    Color color = Color.rgb(rawPixels[pixelIndex], rawPixels[pixelIndex + 1], rawPixels[pixelIndex + 2], rawPixels[pixelIndex + 3] / 255.0d);
                    graphicsContext.setFill(color);
                    graphicsContext.fillRect(x * canvasPixelSize, y * canvasPixelSize, canvasPixelSize, canvasPixelSize);
                }
            }
        });

        /* Menu Items */
        importPattern.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Select A Pattern (*.png)", "*.png");
            fileChooser.getExtensionFilters().add(extensionFilter);
            File pattern = fileChooser.showOpenDialog(new Stage());
            if (pattern != null) {
                Image chosenPatternImage = new Image(pattern.toURI().toString());
                if (chosenPatternImage.getHeight() > 50.0d || chosenPatternImage.getWidth() > 50.0d) {
                    try {
                        FXMLLoader generatedModalFXML = new FXMLLoader(getClass().getResource("/wfc/WFCFailModal.fxml"));
                        Parent generatedModal = generatedModalFXML.load();
                        WFCFailController modalController = generatedModalFXML.getController();

                        Scene modalScene = new Scene(generatedModal);
                        Stage modalStage = new Stage(StageStyle.DECORATED);
                        modalStage.setTitle("Invalid");
                        modalStage.initOwner(mainPane.getScene().getWindow());
                        modalStage.initModality(Modality.APPLICATION_MODAL);

                        modalController.setLabel("Image Width/Height exceeds 50");
                        modalController.setButtonText("Close");

                        modalStage.setScene(modalScene);
                        modalStage.sizeToScene();
                        modalStage.show();
                    } catch (IOException ignored) {
                    }
                    return;
                }
                handleImage(chosenPatternImage, 0, 0);
            }
        });
        URL jsonFile = getClass().getResource("/wfc/data/templates.json");
        String jsonText;
        try {
            assert jsonFile != null;
            jsonText = Resources.toString(jsonFile, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JSONObject json = new JSONObject(jsonText);
        JSONArray templates = json.getJSONArray("templates");
        for (int i = 0; i < templates.length(); i++) {
            JSONObject template = templates.getJSONObject(i);
            final MenuItem templateItem = new MenuItem(template.getString("name"));
            templateItem.setOnAction(event -> {
                try {
                    Image templateImage = new Image(Objects.requireNonNull(getClass().getResource("/wfc/" + template.getString("path"))).toURI().toString());
                    handleImage(templateImage, template.getInt("ground-x"), template.getInt("ground-y"));
                    tileSlider.valueProperty().setValue(template.getInt("tile-size"));
                    variationSlider.valueProperty().setValue(template.getInt("variation"));
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
            templateSelect.getItems().add(templateItem);
        }

        contextMenuPixelX = 0;
        contextMenuPixelY = 0;
        ContextMenu canvasContextMenu = new ContextMenu();
        MenuItem setGround = new MenuItem("Set As Ground");
        setGround.setOnAction(event -> {
            int realX = contextMenuPixelX / canvasPixelSize;
            int realY = contextMenuPixelY / canvasPixelSize;
            handleGroundChange(realX, realY);
        });
        MenuItem resetGround = new MenuItem("Reset Ground");
        resetGround.setOnAction(event -> {
            ground = 0;
            groundInput.setText("");
            groundXY[0] = 0;
            groundXY[1] = 0;
        });
        SeparatorMenuItem separator = new SeparatorMenuItem();
        MenuItem close = new MenuItem("Close");
        canvasContextMenu.getItems().addAll(setGround, resetGround, separator, close);
        canvas.setOnContextMenuRequested(event -> {
            if (event.getX() < 0 || event.getX() > canvasWidth || event.getY() < 0 || event.getY() > canvasHeight)
                return;
            contextMenuPixelX = (int) event.getX();
            contextMenuPixelY = (int) event.getY();
            canvasContextMenu.show(canvas, event.getScreenX(), event.getScreenY());
        });
    }

    void handleGroundChange(int realX, int realY) {
        if (realX > bufferImage.getWidth() || realY > bufferImage.getHeight() || realX < 0 || realY < 0 || (realX == 0 && realY == 0)) {
            ground = 0;
            groundInput.setText("");
            groundXY[0] = 0;
            groundXY[1] = 0;
        } else {
            wfcModel.loadNew(tileSize, canvasPixelSize, variation, ground, outWidth, outHeight);
            wfcModel.preProcessImage();
            int[] groundPixelData = new int[tileSize * tileSize];
            int rawArrayWidth = bufferImage.getArrWidth();
            int[] rawPixelData = bufferImage.getPixelData();
            Map<String, Integer> colorMap = wfcModel.getColorMap();
            for (int y = realY, y1 = 0; y < realY + tileSize; y++, y1++) {
                for (int x = realX, x1 = 0; x < realX + tileSize; x++, x1++) {
                    int rawPixelIndex = ((y % bufferImage.getHeight()) * rawArrayWidth + (x % bufferImage.getWidth())) * 4;
                    String colorIndex = "" + rawPixelData[rawPixelIndex] + "-" + rawPixelData[rawPixelIndex + 1] + "-" + rawPixelData[rawPixelIndex + 2] + "-" + rawPixelData[rawPixelIndex + 3];
                    groundPixelData[y1 * tileSize + x1] = colorMap.get(colorIndex);
                }
            }
            Pattern groundPattern = new Pattern(groundPixelData, tileSize);
            int patternIndex = wfcModel.getPatternIndex(groundPattern);
            if (patternIndex == -1) {
                ground = 0;
                groundInput.setText("");
                groundXY[0] = 0;
                groundXY[1] = 0;
            } else {
                groundInput.setText("" + patternIndex);
                ground = patternIndex;
                groundXY[0] = realX;
                groundXY[1] = realY;
                wfcModel.loadNew(tileSize, canvasPixelSize, variation, ground, outWidth, outHeight);
            }
        }
    }

    void handleImage(Image chosenPatternImage, int groundX, int groundY) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(chosenPatternImage, null);
        bufferImage.setWidth(bufferedImage.getWidth());
        bufferImage.setHeight(bufferedImage.getHeight());
        for (int y = 0; y < bufferImage.getHeight(); y++) {
            for (int x = 0; x < bufferImage.getWidth(); x++) {
                int rgba = bufferedImage.getRGB(x, y);
                int R = (rgba & 0xff0000) >> 16;
                int G = (rgba & 0xff00) >> 8;
                int B = rgba & 0xff;
                int A = (rgba & 0xff000000) >>> 24;
                Color color = Color.rgb(R, G, B, A / 255.0d);
                bufferImage.setPixel(x, y, color);
            }
        }
        BufferedImage processedImage = Overlapping.processImportedPattern(bufferedImage, canvasPixelSize);
        WritableImage writableImage = SwingFXUtils.toFXImage(processedImage, null);
        widthSlider.valueProperty().setValue(chosenPatternImage.getWidth());
        heightSlider.valueProperty().setValue(chosenPatternImage.getHeight());
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, canvasWidth, canvasHeight);
        graphicsContext.drawImage(writableImage, 0, 0);
        handleGroundChange(groundX, groundY);
    }

    void generateOutput() {
        long start = System.currentTimeMillis();
        boolean generation = wfcModel.generate(0);
        if (generation) {
            BufferedImage result = wfcModel.postProcessImage(-1);
            try {
                presentOutput(result, 0, System.currentTimeMillis() - start);
            } catch (IOException ignored) {
            }
        } else {
            if (0 < retries) {
                generateOutput(1, start);
            } else {
                try {
                    presentFail(System.currentTimeMillis() - start);
                } catch (IOException ignored) {
                }
            }
        }
    }

    void generateOutput(int currRetries, long timestamp) {
        boolean generation = wfcModel.generate(0);
        if (generation) {
            BufferedImage result = wfcModel.postProcessImage(-1);
            try {
                presentOutput(result, currRetries, System.currentTimeMillis() - timestamp);
            } catch (IOException ignored) {
            }
        } else {
            if (currRetries < retries) {
                generateOutput(currRetries + 1, timestamp);
            } else {
                try {
                    presentFail(System.currentTimeMillis() - timestamp);
                } catch (IOException ignored) {
                }
            }
        }
    }

    void presentOutput(BufferedImage image, int triesTaken, long timeTaken) throws IOException {
        FXMLLoader generatedModalFXML = new FXMLLoader(getClass().getResource("/wfc/WFCOutputModal.fxml"));
        Parent generatedModal = generatedModalFXML.load();
        WFCOutputController modalController = generatedModalFXML.getController();

        Scene modalScene = new Scene(generatedModal);
        Stage modalStage = new Stage(StageStyle.DECORATED);
        modalStage.setTitle("Generated Output " + outWidth + "x" + outWidth + " - " + (triesTaken + 1) + " attempt(s) - " + timeTaken + "ms");
        modalStage.initOwner(mainPane.getScene().getWindow());
        modalStage.initModality(Modality.APPLICATION_MODAL);

        WritableImage outputImage = SwingFXUtils.toFXImage(image, null);
        modalController.drawOutput(outputImage);

        modalStage.setOnHidden(event -> currentlyGenerating = false);

        modalStage.setScene(modalScene);
        modalStage.sizeToScene();
        modalStage.show();
    }

    void presentFail(long timeTaken) throws IOException {
        FXMLLoader generatedModalFXML = new FXMLLoader(getClass().getResource("/wfc/WFCFailModal.fxml"));
        Parent generatedModal = generatedModalFXML.load();
        WFCFailController modalController = generatedModalFXML.getController();

        Scene modalScene = new Scene(generatedModal);
        Stage modalStage = new Stage(StageStyle.DECORATED);
        modalStage.setTitle("Failed - " + timeTaken + "ms");
        modalStage.initOwner(mainPane.getScene().getWindow());
        modalStage.initModality(Modality.APPLICATION_MODAL);

        modalController.setLabel("Generation Failed");
        modalController.setButtonText("Retry");
        modalController.setInitialBoolean(false);

        modalStage.setOnHidden(event -> {
            if (modalController.getBooleanProperty()) {
                generateOutput();
            } else {
                currentlyGenerating = false;
            }
        });

        modalStage.setScene(modalScene);
        modalStage.sizeToScene();
        modalStage.show();
    }
}
