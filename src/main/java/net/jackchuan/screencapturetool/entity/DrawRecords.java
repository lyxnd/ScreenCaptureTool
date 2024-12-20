package net.jackchuan.screencapturetool.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
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
    private double scaleX;
    private double scaleY;
    private double width, height;
    private Color color;
    private Image image;
    private boolean shouldRender = true;
    private String detailInfo;
    private DrawableImage drawableImage;
    private long editTick;
    private boolean shouldRepaint;

    public DrawRecords() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DrawRecords record) {
            boolean type = this.getDrawType().equals(record.getDrawType());
            if (type) {
                if ("externalImg".equals(getDrawType())) {
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

    public DrawRecords(String drawType, double startX, double startY, double endX, double endY, WritableImage img, Color color, long tick) {
        this.drawType = drawType;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.image = img;
        this.color = color;
        this.editTick = tick;
    }

    public DrawRecords(String drawType, DrawableImage externalImage, String detail, long tick) {
        this.drawType = drawType;
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
        switch (drawType) {
            case "common"->{

            }
            case "rect" -> {
                DrawType.RECTANGLE.draw(gc, startX, startY, endX, endY);
            }
            case "fillRect" -> {
                DrawType.FILLED_RECTANGLE.draw(gc, startX, startY, endX, endY, color);
            }
            case "arrow" -> {
                DrawType.ARROW.draw(gc, startX, startY, endX, endY);
            }
            case "line" -> {
                DrawType.LINE.draw(gc, startX, startY, endX, endY);
            }
            case "wave" -> {
                DrawType.WAVE.draw(gc, startX, startY, endX, endY);
            }
            case "circle" -> {
                DrawType.CIRCLE.draw(gc, startX, startY, endX, endY);
            }
            case "filledOval" -> {
                DrawType.FILLED_CIRCLE.draw(gc, startX, startY, endX, endY, color);
            }
            case "lineDashed"->{
                DrawType.LINE_DASHED.draw(gc, startX, startY, endX, endY);
            }
            case "lineDouble"->{
                DrawType.LINE_DOUBLE.draw(gc, startX, startY, endX, endY);
            }
            case "rectRound"->{
                DrawType.RECTANGLE_ROUND.draw(gc, startX, startY, endX, endY);
            }
            case "rectRoundFilled"->{
                DrawType.RECTANGLE_ROUND_FILLED.draw(gc, startX, startY, endX, endY,color);
            }
            case "arrowDashed"->{
                DrawType.ARROW_DASHED.draw(gc, startX, startY, endX, endY);
            }
            case "arrowFilled"->{
                DrawType.ARROW_FILLED.draw(gc, startX, startY, endX, endY,color);
            }
            case "arrowEmpty"->{
                DrawType.ARROW_EMPTY.draw(gc, startX, startY, endX, endY);
            }
            case "arrowTwoDir"->{
                DrawType.ARROW_TWO_DIR.draw(gc, startX, startY, endX, endY);
            }
            case "arrowDouble"->{
                DrawType.ARROW_DOUBLE.draw(gc, startX, startY, endX, endY);
            }
            case "ovalDashed"->{
                DrawType.OVAL_DASHED.draw(gc, startX, startY, endX, endY);
            }
            case "arrowDoubleDashed"->{
                DrawType.ARROW_DOUBLE_DASHED.draw(gc, startX, startY, endX, endY);
            }
            case "arrowDoubleTwo"->{
                DrawType.ARROW_DOUBLE_TWO_DIR.draw(gc, startX, startY, endX, endY);
            }
            case "rectRoundDashed"->{
                DrawType.RECTANGLE_ROUND_DASHED.draw(gc, startX, startY, endX, endY);
            }
            case "rectRoundDashedFilled"->{
                DrawType.RECTANGLE_ROUND_DASHED_FILLED.draw(gc, startX, startY, endX, endY,color);
            }
            case "rectDashed"->{
                DrawType.RECTANGLE_DASHED.draw(gc, startX, startY, endX, endY);
            }
            case "rectDashedFilled"->{
                DrawType.RECTANGLE_DASHED_FILLED.draw(gc, startX, startY, endX, endY,color);
            }
            case "externalImg" -> {
                updateExternalImage(gc, animatorGc);
            }
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

    public DrawableImage getDrawableImage() {
        return drawableImage;
    }

    public void setDrawableImage(DrawableImage drawableImage) {
        this.drawableImage = drawableImage;
    }

    public void setShouldRepaint(boolean shouldRepaint) {
        this.shouldRepaint = shouldRepaint;
    }

    public boolean isShouldRepaint() {
        return shouldRepaint;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }


    public long getEditTick() {
        return editTick;
    }

    public void setEditTick(long editTick) {
        this.editTick = editTick;
    }
    //    public boolean isShouldExchange() {
//        return shouldExchange;
//    }
//
//    public void setShouldExchange(boolean shouldExchange) {
//        this.shouldExchange = shouldExchange;
//    }

    public boolean isShouldRender() {
        return shouldRender;
    }

    public void setShouldRender(boolean shouldRender) {
        this.shouldRender = shouldRender;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }

    public double getScaleX() {
        return scaleX;
    }

    public double getScaleY() {
        return scaleY;
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

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

//    public boolean isChangeRender() {
//        return changeRender;
//    }
//
//    public void setChangeRender(boolean changeRender) {
//        this.changeRender = changeRender;
//    }

    public String getDetailInfo() {
        return detailInfo;
    }

    public void setDetailInfo(String detailInfo) {
        this.detailInfo = detailInfo;
    }

    @Override
    public String toString() {
        String pos = drawableImage==null? "empty" : drawableImage.getX()+"\t"+drawableImage.getY();
        return "DrawRecords{" +
                "drawType='" + drawType + '\'' +
                ", startX=" + startX +
                ", startY=" + startY +
                ", width=" + width +
                ", height=" + height +
                ", image info=" + pos +
                ", shouldRender=" + shouldRender +
                ", detailInfo='" + detailInfo + '\'' +
                ", editTick=" + editTick +
                ", shouldRepaint=" + shouldRepaint +
                '}';
    }
}
