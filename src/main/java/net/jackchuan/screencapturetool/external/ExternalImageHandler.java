package net.jackchuan.screencapturetool.external;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import net.jackchuan.screencapturetool.util.impl.CornerType;

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
    private Canvas outerCanvas;
    public ExternalImageHandler(Canvas canvas,Canvas outerCanvas){
        this.canvas=canvas;
        this.outerCanvas=outerCanvas;
    }
    public void drawAllImages(GraphicsContext gc) {
        for (DrawableImage image : images) {
            if(image.shouldRenderBorder()){
                drawBorder(outerCanvas.getGraphicsContext2D(), image);
            }
            if(image.shouldRender()){
                gc.drawImage(image.image,image.x, image.y,  image.width, image.height);
            }else {
                outerCanvas.getGraphicsContext2D().drawImage(image.image, image.x, image.y, image.width, image.height);
            }
        }
    }

    public void updateImage(GraphicsContext gc,DrawableImage image){
        if(image.shouldRenderBorder()){
            drawBorder(outerCanvas.getGraphicsContext2D(), image);
        }
        outerCanvas.getGraphicsContext2D().drawImage(image.image, image.x, image.y, image.width, image.height);
    }

    private void drawBorder(GraphicsContext gc, DrawableImage image) {
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(3);
        gc.strokeRect(image.x, image.y, image.width, image.height);
    }

    public CornerType isNearBorder(DrawableImage image, double mouseX, double mouseY) {
        double borderThreshold = 10;  // 阈值，决定多近算在边界上
        double cornerThreshold = 10;  // 角的距离阈值（用于角判断）
        // 检查四个角
        // 左上角
        double distanceToTopLeft = Math.sqrt(Math.pow(mouseX - image.x, 2) + Math.pow(mouseY - image.y, 2));
        if (distanceToTopLeft <= cornerThreshold) {
            return CornerType.NORTHWEST;
        }
        // 右上角
        double distanceToTopRight = Math.sqrt(Math.pow(mouseX - (image.x + image.width), 2) + Math.pow(mouseY - image.y, 2));
        if (distanceToTopRight <= cornerThreshold) {
            return CornerType.NORTHEAST;
        }
        // 左下角
        double distanceToBottomLeft = Math.sqrt(Math.pow(mouseX - image.x, 2) + Math.pow(mouseY - (image.y + image.height), 2));
        if (distanceToBottomLeft <= cornerThreshold) {
            return CornerType.SOUTHWEST;
        }
        // 右下角
        double distanceToBottomRight = Math.sqrt(Math.pow(mouseX - (image.x + image.width), 2) + Math.pow(mouseY - (image.y + image.height), 2));
        if (distanceToBottomRight <= cornerThreshold) {
            return CornerType.SOUTHEAST;
        }
        // 检查左边
        if (mouseX >= image.x - borderThreshold && mouseX <= image.x + borderThreshold) {
            if (mouseY >= image.y - borderThreshold && mouseY <= image.y + image.height + borderThreshold) {
                return CornerType.WEST;
            }
        }
        // 检查右边
        if (mouseX >= image.x + image.width - borderThreshold && mouseX <= image.x + image.width + borderThreshold) {
            if (mouseY >= image.y - borderThreshold && mouseY <= image.y + image.height + borderThreshold) {
                return CornerType.EAST;
            }
        }
        // 检查上边
        if (mouseY >= image.y - borderThreshold && mouseY <= image.y + borderThreshold) {
            if (mouseX >= image.x - borderThreshold && mouseX <= image.x + image.width + borderThreshold) {
                return CornerType.NORTH;
            }
        }
        // 检查下边
        if (mouseY >= image.y + image.height - borderThreshold && mouseY <= image.y + image.height + borderThreshold) {
            if (mouseX >= image.x - borderThreshold && mouseX <= image.x + image.width + borderThreshold) {
                return CornerType.SOUTH;
            }
        }
        // 如果不在任何边界上，返回 EMPTY
        return CornerType.EMPTY;
    }


    public List<DrawableImage> getImages() {
        return images;
    }

    public void addExternalImage(DrawableImage image) {
        images.add(image);
    }

    public void removeExternalImage(DrawableImage image) {
        images.remove(image);
    }

    public void clearAll() {
        images.clear();
        drawAllImages(canvas.getGraphicsContext2D());
    }

    public static class DrawableImage implements Cloneable{
        private Image image;
        double x, y, width, height;
        double oriX, oriY;
        private String name;
        private boolean shouldRender,renderBorder;
        private boolean isUndo;
        public DrawableImage(Image image, double x, double y, double width, double height,String name) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.name=name;
        }

        @Override
        public DrawableImage clone() throws CloneNotSupportedException {
            DrawableImage cloned = (DrawableImage) super.clone();
            cloned.setX(getX());
            cloned.setY(getY());
            cloned.setWidth(getWidth());
            cloned.setHeight(getHeight());
            cloned.setShouldRender(shouldRender());
            cloned.setRenderBorder(shouldRenderBorder());
            return cloned;
        }

        public boolean isInside(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        public double getOriY() {
            return oriY;
        }

        public double getOriX() {
            return oriX;
        }

        public void setOriY(double oriY) {
            this.oriY = oriY;
        }

        public void setOriX(double oriX) {
            this.oriX = oriX;
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

        public void setUndo(boolean undo) {
            isUndo = undo;
        }

        public boolean isUndo() {
            return isUndo;
        }

        public String getName() {
            return name;
        }

        public Image getImage() {
            return image;
        }

        public void setName(String name) {
            this.name = name;
        }
        public boolean shouldRender() {
            return shouldRender;
        }
        public void setShouldRender(boolean shouldRender) {
            this.shouldRender = shouldRender;
        }

        public void setRenderBorder(boolean renderBorder) {
            this.renderBorder = renderBorder;
        }

        public boolean shouldRenderBorder() {
            return renderBorder;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof DrawableImage drawableImage){
                return this.name.equals(drawableImage.getName());
            }else {
                return false;
            }
        }

        public boolean canKeep() {
            return Math.abs(this.x-this.oriX)<5||Math.abs(this.y-this.oriY)<5;
        }
    }

}
