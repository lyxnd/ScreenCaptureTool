package net.jackchuan.screencapturetool.external.picker;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/18 14:14
 */
public class EmojyPicker extends CustomPicker{
    public EmojyPicker(){

    }
    public EmojyPicker(CaptureDisplayController controller) throws URISyntaxException {
        super("assets/emojy",controller);
        ImageView imageView = new ImageView(new Image(ScreenCaptureToolApp.class.getResource("assets/icon/emojy.png").toExternalForm()));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        this.setGraphic(imageView);
    }
    @Override
    protected void onClicked(GraphicsContext gc, int index) {
        if (controller == null) return;
        String resourcePath = getResourcePath(index);
        if (resourcePath != null) {
            InputStream stream = ScreenCaptureToolApp.class.getResourceAsStream(resourcePath);
            if (stream != null) {
                String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
                controller.addEmojy(new Image(stream), fileName);
            }
        }
        controller.setCursorShape(Cursor.HAND);
    }

}
