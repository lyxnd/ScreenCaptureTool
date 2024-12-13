package net.jackchuan.screencapturetool.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import net.jackchuan.screencapturetool.external.ExternalImageHandler.DrawableImage;
import net.jackchuan.screencapturetool.util.impl.DrawType;


/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/12 13:26
 */
public class DrawRecords {
    private String drawType;
    private double startX;
    private double startY;
    private double endX;
    private double endY;
    private Color color;
    private WritableImage image;
    private DrawableImage externalImage;

    public DrawRecords(){
    }

    public DrawRecords(String drawType, double startX, double startY, double endX,double endY,WritableImage img,Color color) {
        this.drawType = drawType;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.image = img;
        this.color = color;
    }
    public DrawRecords(String drawType,DrawableImage externalImage) {
        this.drawType = drawType;
        this.externalImage = externalImage;
    }

    public void draw(GraphicsContext gc){
        gc.setStroke(color);
        gc.setFill(color);
        switch (drawType){
            case "rect" ->{
                DrawType.RECTANGLE.draw(gc, startX, startY,endX,endY);
            }
            case "fillRect" ->{
                DrawType.FILLED_RECTANGLE.draw(gc, startX, startY,endX,endY,color);
            }
            case "arrow" ->{
                DrawType.ARROW.draw(gc, startX, startY, endX,endY, color);
            }
            case "line" ->{
                DrawType.LINE.draw(gc, startX, startY, endX, endY);
            }
            case "wave" ->{
                DrawType.WAVE.draw(gc, startX, startY, endX, endY);
            }
            case "circle" ->{
                DrawType.CIRCLE.draw(gc, startX, startY, endX, endY);
            }
            case "filledOval" ->{
                DrawType.FILLED_CIRCLE.draw(gc, startX, startY, endX, endY,color);
            }
            case "externalImg" ->{
                if(externalImage!=null&&externalImage.shouldRender()){
                    gc.drawImage(externalImage.getImage(), externalImage.getOriX(), externalImage.getOriY(),
                            externalImage.getWidth(), externalImage.getHeight());
                    if(externalImage.shouldRenderBorder()){
                        gc.setStroke(Color.CYAN);
                        gc.setLineWidth(3);
                        gc.strokeRect(externalImage.getX(), externalImage.getY(),
                                externalImage.getWidth(), externalImage.getHeight());
                    }
                }
            }
        }
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setDrawType(String drawType) {
        this.drawType = drawType;
    }

    public void setEndX(double endX) {
        this.endX = endX;
    }

    public void setEndY(double endY) {
        this.endY = endY;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public Color getColor() {
        return color;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public String getDrawType() {
        return drawType;
    }

    public WritableImage getImage() {
        return image;
    }
    public void setImage(WritableImage image) {
        this.image = image;
    }
}
