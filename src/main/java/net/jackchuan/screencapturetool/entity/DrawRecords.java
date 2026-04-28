package net.jackchuan.screencapturetool.entity;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import lombok.Data;
import net.jackchuan.screencapturetool.external.ExternalImageHandler.DrawableImage;
import net.jackchuan.screencapturetool.external.ExternalTextHandler.DrawableText;
import net.jackchuan.screencapturetool.util.impl.DrawType;

import java.util.List;


/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/12 13:26
 */
@Data
public class DrawRecords {
    private double startX;
    private double startY;
    private double endX;
    private double endY;
    private double scaleX;
    private double scaleY;
    private double width, height;
    private Color color;
    private Image image;
    private boolean shouldRender = true;
    private String detailInfo;
    private DrawableImage drawableImage;
    private DrawableText drawableText;
    private long editTick;
    private boolean shouldRepaint;
    private String text;
    private DrawTypes type;
    private CropImage cropImage;
    private List<Point2D> handPoints;
    private int strokeWidth;
    public DrawRecords() {
    }

    public record CropImage(Image beforeCrop, Image afterCrop){}

    public DrawRecords(DrawTypes drawType, DrawableText text, String detail, long tick) {
        this.type = drawType;
        this.drawableText = text;
        setStartX(text.getX());
        setStartY(text.getY());
        setWidth(text.getWidth());
        setHeight(text.getHeight());
        setText(text.getValue());
        setDetailInfo(detail);
        setEditTick(tick);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DrawRecords record) {
            boolean type = this.getType().equals(record.getType());
            if (type) {
                if (getType()==DrawTypes.EXTERNAL_IMAGE) {
                    return image.equals(record.getImage());
                } else {
                    return getEditTick() == record.getEditTick();
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public DrawRecords(DrawTypes drawType, double startX, double startY,
                       double endX, double endY, WritableImage img, Color color, long tick,int strokeWidth) {
        this.type = drawType;
        this.strokeWidth=strokeWidth;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.image = img;
        this.color = color;
        this.editTick = tick;
    }
    public DrawRecords(DrawTypes drawType, List<Point2D> handPoints, Color color, long tick,int strokeWidth) {
        this.type = drawType;
        this.strokeWidth=strokeWidth;
        this.color = color;
        this.editTick = tick;
        this.handPoints = handPoints;
    }
    public DrawRecords(DrawTypes drawType, double startX, double startY, double endX, double endY,
                       WritableImage beforeCrop,Image afterCrop, Color color, long tick) {
        this.type = drawType;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.cropImage=new CropImage(beforeCrop,afterCrop);
        this.color = color;
        this.editTick = tick;
    }

    public DrawRecords(DrawTypes drawType, DrawableImage externalImage, String detail, long tick) {
        this.type = drawType;
        this.drawableImage = externalImage;
        setStartX(externalImage.getX());
        setStartY(externalImage.getY());
        setWidth(externalImage.getWidth());
        setHeight(externalImage.getHeight());
        setImage(externalImage.getImage());
        setDetailInfo(detail);
        setEditTick(tick);
    }

    public void draw(GraphicsContext gc, GraphicsContext animatorGc) {
        gc.setStroke(color);
        gc.setFill(color);
        gc.setLineWidth(strokeWidth);
        switch (type) {
            case DrawTypes.COMMON->{
                for(int i=1;i<=handPoints.size()-1;i++) {
                    Point2D lastPoint = handPoints.get(i-1);
                    Point2D nowPoint = handPoints.get(i);
                    gc.strokeLine(lastPoint.getX(), lastPoint.getY(), nowPoint.getX(), nowPoint.getY());
                }
            }
            case DrawTypes.ERASER->{
                for(int i=1;i<=handPoints.size()-1;i++) {
                    Point2D lastPoint = handPoints.get(i-1);
                    Point2D nowPoint = handPoints.get(i);
                    gc.clearRect(lastPoint.getX(), lastPoint.getY(),strokeWidth*10,strokeWidth*10);
                }
            }
            case DrawTypes.RECT -> {
                DrawType.RECTANGLE.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.FILL_RECT -> {
                DrawType.FILLED_RECTANGLE.draw(gc, startX, startY, endX, endY, color);
            }
            case DrawTypes.ARROW -> {
                DrawType.ARROW.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.LINE -> {
                DrawType.LINE.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.WAVE -> {
                DrawType.WAVE.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.CIRCLE -> {
                DrawType.CIRCLE.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.FILLED_OVAL -> {
                DrawType.FILLED_CIRCLE.draw(gc, startX, startY, endX, endY, color);
            }
            case DrawTypes.LINE_DASHED->{
                DrawType.LINE_DASHED.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.LINE_DOUBLE->{
                DrawType.LINE_DOUBLE.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.RECT_ROUND->{
                DrawType.RECTANGLE_ROUND.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.RECT_ROUND_FILLED->{
                DrawType.RECTANGLE_ROUND_FILLED.draw(gc, startX, startY, endX, endY,color);
            }
            case DrawTypes.ARROW_DASHED->{
                DrawType.ARROW_DASHED.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.ARROW_FILLED->{
                DrawType.ARROW_FILLED.draw(gc, startX, startY, endX, endY,color);
            }
            case DrawTypes.ARROW_EMPTY->{
                DrawType.ARROW_EMPTY.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.ARROW_TWO_DIR->{
                DrawType.ARROW_TWO_DIR.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.ARROW_DOUBLE->{
                DrawType.ARROW_DOUBLE.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.OVAL_DASHED->{
                DrawType.OVAL_DASHED.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.ARROW_DOUBLE_DASHED->{
                DrawType.ARROW_DOUBLE_DASHED.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.ARROW_DOUBLE_TWO_DIR->{
                DrawType.ARROW_DOUBLE_TWO_DIR.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.RECT_ROUND_DASHED->{
                DrawType.RECTANGLE_ROUND_DASHED.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.RECT_ROUND_DASHED_FILLED->{
                DrawType.RECTANGLE_ROUND_DASHED_FILLED.draw(gc, startX, startY, endX, endY,color);
            }
            case DrawTypes.RECT_DASHED->{
                DrawType.RECTANGLE_DASHED.draw(gc, startX, startY, endX, endY);
            }
            case DrawTypes.RECT_DASHED_FILLED->{
                DrawType.RECTANGLE_DASHED_FILLED.draw(gc, startX, startY, endX, endY,color);
            }
            case DrawTypes.EXTERNAL_IMAGE -> {
                updateExternalImage(gc, animatorGc);
            }
            case DrawTypes.EXTERNAL_TEXT -> {
                updateExternalText(gc, animatorGc);
            }
        }
    }

    private void updateExternalText(GraphicsContext gc, GraphicsContext animatorGc) {
        if (!isShouldRepaint()) {
            return;
        }
        if(drawableText.shouldRenderBorder()){
            gc.setStroke(Color.CYAN);
            gc.setLineWidth(3);
            gc.strokeRect(getStartX()-10, getStartY()-20, getWidth(), getHeight());
        }
        if(isShouldRender()){
            gc.setFont(drawableText.getFont());
            gc.setStroke(drawableText.getColor());
            gc.strokeText(getText(), getStartX(), getStartY());
        }else {
            animatorGc.setFont(drawableText.getFont());
            animatorGc.setStroke(drawableText.getColor());
            animatorGc.strokeText(getText(), getStartX(), getStartY());
        }
    }

    public void updateExternalImage(GraphicsContext gc, GraphicsContext animatorGc) {
        if (!isShouldRepaint()) {
            return;
        }
        if(drawableImage.shouldRenderBorder()){
            gc.setStroke(Color.CYAN);
            gc.setLineWidth(3);
            gc.strokeRect(getStartX(), getStartY(), getWidth(), getHeight());
        }
        if(isShouldRender()){
            gc.drawImage(getImage(), getStartX(), getStartY(), getWidth(), getHeight());
        }else {
            animatorGc.drawImage(getImage(), getStartX(), getStartY(), getWidth(), getHeight());
        }
    }

    public void print() {

    }

    @Override
    public String toString() {
        String pos = drawableImage==null? "empty" : drawableImage.getX()+"\t"+drawableImage.getY();
        String pos1 = drawableText==null? "empty" : drawableText.getX()+"\t"+drawableText.getY();
        return "DrawRecords{" +
                "drawType='" + type + '\'' +
                ", startX=" + startX +
                ", startY=" + startY +
                ", width=" + width +
                ", height=" + height +
                ", image info=" + pos +
                ", shouldRender=" + shouldRender +
                ", detailInfo='" + detailInfo + '\'' +
                ", editTick=" + editTick +
                ", shouldRepaint=" + shouldRepaint +
                ", text=" + text +
                ", textPos=" + pos1 +
                '}';
    }
}
