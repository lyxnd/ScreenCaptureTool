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
public class LinePicker extends CustomPicker{
    public LinePicker(Image icon, ArrayList<String> imgPaths, GraphicsContext gc) {
        super(icon, imgPaths, gc);
    }
    public LinePicker(Image icon, ArrayList<String> imgPaths, CaptureDisplayController controller) {
        super(icon, imgPaths, controller);
    }
    public LinePicker(){
        super();
    }

    @Override
    protected void onClicked(GraphicsContext gc,int index) {
        if(controller==null){
            return;
        }
        switch (index){
            case 0->{
                controller.setType(5);
                controller.setCursorShape(Cursor.CROSSHAIR);
            }
            case 1->{
                //cos
                controller.setType(6);
            }
            case 2->{
                //line dashed
                controller.setType(11);
            }
            case 3->{
                //double line
                controller.setType(12);
            }
        }
        controller.setCursorShape(Cursor.CROSSHAIR);
    }

}
