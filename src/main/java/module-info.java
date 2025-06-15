module net.jackchuan.screencapturetool {
    requires javafx.graphics;
    requires javafx.swing;
    requires javafx.fxml;
    requires javafx.controls;
    requires org.bytedeco.opencv;
    requires java.sql;
    requires org.slf4j;
    requires com.github.kwhat.jnativehook;
    requires java.compiler;
    requires net.sourceforge.tess4j;


    opens net.jackchuan.screencapturetool to javafx.fxml;
    opens net.jackchuan.screencapturetool.external.picker to javafx.fxml;
    opens net.jackchuan.screencapturetool.external.stage to javafx.fxml;
    opens net.jackchuan.screencapturetool.controller to javafx.fxml;
    exports net.jackchuan.screencapturetool;
    exports net.jackchuan.screencapturetool.test;

}
