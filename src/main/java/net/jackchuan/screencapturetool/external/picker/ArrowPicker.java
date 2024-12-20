package net.jackchuan.screencapturetool.external.picker;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;
import net.jackchuan.screencapturetool.util.ResourceLoader;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/18 14:14
 */
public class ArrowPicker extends CustomPicker{
    public ArrowPicker(Image icon, ArrayList<String> imgPaths, GraphicsContext gc) {
        super(icon, imgPaths, gc);
    }
    public ArrowPicker(Image icon, ArrayList<String> imgPaths, CaptureDisplayController controller) {
        super(icon, imgPaths,controller);
    }
    public ArrowPicker(){
        super();
    }

    @Override
    protected void onClicked(GraphicsContext gc,int index) {
        if(controller==null){
            return;
        }
        switch (index){
            case 0->{
                controller.setType(4);
            }
            case 1->{
                //arrow dashed
                controller.setType(15);
            }
            case 2->{
                //arrow filled
                controller.setType(16);
            }
            case 3->{
                //arrow empty
                controller.setType(17);
            }
            case 4 ->{
                //arrow two dir
                controller.setType(18);
            }
            case 5->{
                //arrow wide
                controller.setType(19);
            }
            case 6->{
                //arrow wide dashed
                controller.setType(21);
            }
            case 7->{
                //arrow wide two dir
                controller.setType(22);
            }
        }
        controller.setCursorShape(Cursor.CROSSHAIR);
    }

}
