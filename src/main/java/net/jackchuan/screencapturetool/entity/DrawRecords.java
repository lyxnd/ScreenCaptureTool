package net.jackchuan.screencapturetool.entity;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import net.jackchuan.screencapturetool.external.ExternalImageHandler;
import net.jackchuan.screencapturetool.external.ExternalImageHandler.DrawableImage;
import net.jackchuan.screencapturetool.util.impl.DrawType;

import java.util.Stack;


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
    private Color color;
    private WritableImage image;
    private DrawableImage externalImage;
    private boolean shouldRender=true;
//    private boolean changeRender=false;
//    private boolean shouldExchange=false;
    private String detailInfo;
    private ExternalImageRecord externalRecord;
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
    public DrawRecords(String drawType,DrawableImage externalImage,String editType) {
        this.drawType = drawType;
//        try {
            this.externalImage = externalImage;
            ImageEditRecord rec=new ImageEditRecord(editType,externalImage.getX(),externalImage.getY(),
                    externalImage.getWidth(),externalImage.getHeight());
            externalRecord= new ExternalImageRecord();
            externalRecord.addRecord(rec);
//        } catch (CloneNotSupportedException e) {
//            throw new RuntimeException(e);
//        }
    }

    public void draw(GraphicsContext gc,GraphicsContext animatorGc,int undoOrRedo){
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
//                if(externalImage!=null&&isShouldRender()&&externalImage.shouldRender()){
//                    gc.drawImage(externalImage.getImage(), externalImage.getX(), externalImage.getY(),
//                            externalImage.getWidth(), externalImage.getHeight());
//                    if(externalImage.shouldRenderBorder()){
//                        gc.setStroke(Color.CYAN);
//                        gc.setLineWidth(3);
//                        gc.strokeRect(externalImage.getX(), externalImage.getY(),
//                                externalImage.getWidth(), externalImage.getHeight());
//                    }
//                }
                updateExternalImage(gc,animatorGc,undoOrRedo);
            }
        }
    }

    public void updateExternalImage(GraphicsContext gc,GraphicsContext animatorGc,int undoOrRedo){
        gc.clearRect(externalImage.getX(), externalImage.getY(), externalImage.getWidth(), externalImage.getHeight());
        if(undoOrRedo==1){
            ImageEditRecord record = externalRecord.undo(gc,animatorGc, externalImage.getImage());
            if(record!=null){
                externalRecord.updateExternalPos(externalImage,record);
            }
        }else if(undoOrRedo==-1){
            ImageEditRecord record = externalRecord.redo(gc,animatorGc, externalImage.getImage());
            if(record!=null){
                externalRecord.updateExternalPos(externalImage,record);
            }
        }else{
            gc.drawImage(externalImage.getImage(), externalImage.getX(), externalImage.getY(),
                    externalImage.getWidth(), externalImage.getHeight());
        }
    }

    public ExternalImageRecord getExternalRecord() {
        return externalRecord;
    }

    public void setExternalRecord(ExternalImageRecord externalRecord) {
        this.externalRecord = externalRecord;
    }

    public DrawableImage getExternalImage() {
        return externalImage;
    }

    public void setExternalImage(DrawableImage externalImage) {
        this.externalImage = externalImage;
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

    public WritableImage getImage() {
        return image;
    }
    public void setImage(WritableImage image) {
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
        String lo=externalImage.getX()+"\t"+externalImage.getY()+" | "+
                externalImage.getOriX()+"\t"+externalImage.getOriY();
        return "DrawRecords{" +
                "\tdrawType=" + drawType +
                ",\t image=" + image +
                ",\t externalImage info=" + lo +
                ",\t shouldRender=" + shouldRender +
//                ",\t shouldExchange=" + shouldExchange +
                ",\t detail=" + detailInfo +
                '}';
    }

    public static class ExternalImageRecord{
        Stack<ImageEditRecord> exImgStack;
        Stack<ImageEditRecord> undoStack;
        public ExternalImageRecord(){
            exImgStack = new Stack<>();
            undoStack = new Stack<>();
        }
        public boolean shouldPop(){
            return exImgStack.isEmpty();
        }

        public ImageEditRecord undo(GraphicsContext gc,GraphicsContext animatorGc, Image image){
            undoStack.push(exImgStack.pop());
            if(!exImgStack.isEmpty()){
                ImageEditRecord record = exImgStack.getLast();
                gc.drawImage(image,record.getX(), record.getY(),record.getW(),record.getH());
                animatorGc.setStroke(Color.CYAN);
                animatorGc.setLineWidth(3);
                animatorGc.strokeRect(record.getX(), record.getY(), record.getW(), record.getH());
                return record;
            }else {
                return null;
            }
        }
        public ImageEditRecord redo(GraphicsContext gc,GraphicsContext animatorGc, Image image){
            if(!undoStack.isEmpty()){
                ImageEditRecord record = undoStack.pop();
                exImgStack.push(record);
                gc.drawImage(image,record.getX(), record.getY(),record.getW(),record.getH());
                animatorGc.setStroke(Color.CYAN);
                animatorGc.setLineWidth(3);
                animatorGc.strokeRect(record.getX(), record.getY(), record.getW(), record.getH());
                return record;
            }else {
                return null;
            }

        }
        public void addRecord(ImageEditRecord record){
            exImgStack.push(record);
        }
        public void print(){
            System.out.println("---------- Log 图片编辑记录 ----------------");
            System.out.println("\t ======  全部记录 =======  \t");
            for(ImageEditRecord r:exImgStack){
                System.out.println(r.toString());
            }
            System.out.println("\t ====== undo 记录  =======  \t");
            for(ImageEditRecord r:undoStack){
                System.out.println(r.toString());
            }

            System.out.println("---------- Log finished ----------------");
        }

        public void updateExternalPos(DrawableImage externalImage,ImageEditRecord record) {
            externalImage.setX(record.getX());
            externalImage.setY(record.getY());
            externalImage.setWidth(record.getW());
            externalImage.setHeight(record.getH());
        }

        public boolean canRedo() {
            return !this.undoStack.isEmpty();
        }
    }
}
