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
public class RectanglePicker extends CustomPicker{
    public RectanglePicker(Image icon, ArrayList<String> imgPaths, GraphicsContext gc) {
        super(icon, imgPaths, gc);
    }
    public RectanglePicker(Image icon, ArrayList<String> imgPaths, CaptureDisplayController controller) {
        super(icon, imgPaths, controller);
    }
    public RectanglePicker(){
        super();
    }

    @Override
    protected void onClicked(GraphicsContext gc,int index) {
        if(controller==null){
            return;
        }
        switch (index){
            case 0->{
                //rect
                controller.setType(2);
            }
            case 1->{
                //filled rect
                controller.setType(3);
            }
            case 2->{
                //rect round
                controller.setType(13);
            }
            case 3->{
                //filled rect round
                controller.setType(14);
            }
            case 4->{
                //round dashed
                controller.setType(23);
            }
            case 5->{
                //round dashed filled
                controller.setType(24);
            }
            case 6->{
                //rect dashed
                controller.setType(25);
            }
            case 7->{
                //filled rect round
                controller.setType(26);
            }

        }
        controller.setCursorShape(Cursor.CROSSHAIR);
    }

}
