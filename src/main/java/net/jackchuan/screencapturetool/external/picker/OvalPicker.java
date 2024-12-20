package net.jackchuan.screencapturetool.external.picker;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;

import java.util.ArrayList;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/18 14:14
 */
public class OvalPicker extends CustomPicker{
    public OvalPicker(Image icon, ArrayList<String> imgPaths, GraphicsContext gc) {
        super(icon, imgPaths, gc);
    }
    public OvalPicker(Image icon, ArrayList<String> imgPaths, CaptureDisplayController controller) {
        super(icon, imgPaths, controller);
    }
    public OvalPicker(){
        super();
    }

    @Override
    protected void onClicked(GraphicsContext gc,int index) {
        if(controller==null){
            return;
        }
        switch (index){
            case 0->{
                //oval
                controller.setType(7);
            }
            case 1->{
                //filled oval
                controller.setType(10);
            }
            case 2->{
                //dashed oval
                controller.setType(20);
            }
        }
        controller.setCursorShape(Cursor.CROSSHAIR);
    }

}
