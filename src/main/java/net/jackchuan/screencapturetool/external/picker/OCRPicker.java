package net.jackchuan.screencapturetool.external.picker;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import net.jackchuan.screencapturetool.CaptureProperties;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;
import net.jackchuan.screencapturetool.external.stage.AlertHelper;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @Author : jackchuan
 * @Date 2025/6/15-21:26
 * @Function :
 **/
public class OCRPicker extends CustomPicker{
    public OCRPicker(){
        super();
    }
    public OCRPicker(Image icon, ArrayList<String> imgPaths, GraphicsContext gc) {
        super(icon, imgPaths, gc);
    }
    public OCRPicker(Image icon, ArrayList<String> imgPaths, CaptureDisplayController controller) {
        super(icon, imgPaths,controller);
    }
    @Override
    protected void onClicked(GraphicsContext gc, int index) {
        if(controller==null){
            return;
        }
        try {
            if(CaptureProperties.checkOCR()){
                controller.initialExecutor();
                switch (index){
                    case 0->{
                        controller.setType(27);
                        controller.setCursorShape(Cursor.CROSSHAIR);
                    }
                    case 1->{
                        //arrow dashed
                        controller.setType(28);
                        controller.doOCR(controller.getOriginalImage());
                    }
                }
                controller.setCursorShape(Cursor.CROSSHAIR);
            }else{
                AlertHelper.showErrorDialog("Library error",
                        "Unable to find tessData","please download data first");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
