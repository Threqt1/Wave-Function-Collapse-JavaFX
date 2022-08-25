module wfc {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.desktop;
    requires com.google.common;
    requires org.json;

    opens wfc to javafx.fxml;

    exports wfc;
    exports wfc.algorithm;
    exports wfc.algorithm.overlapping;
}
