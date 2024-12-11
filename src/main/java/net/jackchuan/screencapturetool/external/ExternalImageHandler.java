package net.jackchuan.screencapturetool.external;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/4 22:02
 */
public class ExternalImageHandler {
    private final List<DrawableImage> images = new ArrayList<>();
    private Canvas canvas;
    public ExternalImageHandler(Canvas canvas){
        this.canvas=canvas;
    }
    public void drawAllImages(GraphicsContext gc, DrawableImage selectedImage) {
        for (DrawableImage image : images) {
            gc.drawImage(image.image, image.x, image.y, image.width, image.height);
            if(image.equals(selectedImage)){
                drawBorder(gc, image);
            }
        }
    }

    private void drawBorder(GraphicsContext gc, DrawableImage image) {
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(3);
        gc.strokeRect(image.x, image.y, image.width, image.height);
    }

    public boolean isNearBorder(DrawableImage image, double mouseX, double mouseY) {
        double borderThreshold = 10;
        return (mouseX >= image.x + image.width - borderThreshold && mouseX <= image.x + image.width + borderThreshold) ||
                (mouseY >= image.y + image.height - borderThreshold && mouseY <= image.y + image.height + borderThreshold);
    }


    public List<DrawableImage> getImages() {
        return images;
    }

    public void addExternalImage(DrawableImage image) {
        images.add(image);
    }

    public void removeExternalImage(DrawableImage image) {
        System.out.println("before remove external image size :"+images.size());
        images.remove(image);
        System.out.println("after remove external image size :"+images.size());
    }

    public void clearAll() {
        images.clear();
        drawAllImages(canvas.getGraphicsContext2D(),null);
    }

    public static class DrawableImage {
        private Image image;
        double x, y, width, height;
        private String name;
        public DrawableImage(Image image, double x, double y, double width, double height,String name) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.name=name;
        }

        public boolean isInside(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        public void setY(double y) {
            this.y = y;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public void setX(double x) {
            this.x = x;
        }
        public double getHeight() {
            return height;
        }

        public double getWidth() {
            return width;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof DrawableImage drawableImage){
                return this.name.equals(drawableImage.getName());
            }else {
                return false;
            }

        }
    }

}
