package net.jackchuan.screencapturetool.external.picker;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    protected void onClicked(GraphicsContext gc,int index) {
        if(controller==null){
            return;
        }
        URL resource = ScreenCaptureToolApp.class.getResource("assets/emojy");
        if (resource != null) {
            File dir = null;
            try {
                dir = new File(resource.toURI());
                File[] files = dir.listFiles();
                if(files!=null && files.length>index){
                    controller.addEmojy(files[index]);
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        controller.setCursorShape(Cursor.HAND);
    }

}
