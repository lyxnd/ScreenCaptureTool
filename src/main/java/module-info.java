module net.jackchuan.screencapturetool {
    requires javafx.controls;
    requires javafx.fxml;

    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.swing;
    requires com.github.kwhat.jnativehook;
    requires opencv;
    requires com.sun.jna.platform;
    requires com.sun.jna;


    opens net.jackchuan.screencapturetool to javafx.fxml;
    opens net.jackchuan.screencapturetool.controller to javafx.fxml;
    exports net.jackchuan.screencapturetool;
    exports net.jackchuan.screencapturetool.test;

}
