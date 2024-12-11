module net.jackchuan.screencapturetool {
    requires com.github.kwhat.jnativehook;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.bytedeco.opencv;
    requires javafx.swing;
    requires java.compiler;

    opens net.jackchuan.screencapturetool to javafx.fxml;
    opens net.jackchuan.screencapturetool.controller to javafx.fxml;
    exports net.jackchuan.screencapturetool;
    exports net.jackchuan.screencapturetool.test;

}
