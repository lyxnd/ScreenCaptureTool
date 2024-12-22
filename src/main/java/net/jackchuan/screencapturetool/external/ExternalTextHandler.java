package net.jackchuan.screencapturetool.external;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import net.jackchuan.screencapturetool.util.impl.CornerType;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/15 14:17
 */
public class ExternalTextHandler {

    private final List<DrawableText> texts = new ArrayList<>();
    private Canvas editArea;
    private Canvas outerCanvas;
    public ExternalTextHandler(Canvas editArea,Canvas outerCanvas){
        this.editArea =editArea;
        this.outerCanvas=outerCanvas;
    }
    public void drawAllTexts(GraphicsContext gc) {
        for (DrawableText text : texts) {
            gc.setFont(text.getFont());
            gc.setStroke(text.getColor());
            if(text.shouldRenderBorder()){
                drawBorder(outerCanvas.getGraphicsContext2D(), text);
            }
            if(text.shouldRender()){
                gc.strokeText(text.getValue(),text.getX(),text.getY());
            }else {
                outerCanvas.getGraphicsContext2D().setFont(text.getFont());
                outerCanvas.getGraphicsContext2D().setStroke(text.getColor());
                outerCanvas.getGraphicsContext2D().strokeText(text.getValue(),text.getX(),text.getY());
            }
        }
    }

    public void updateText(GraphicsContext gc, DrawableText text){
//        if(text.shouldRenderBorder()){
//            drawBorder(outerCanvas.getGraphicsContext2D(), text);
//        }
        gc.setFont(text.getFont());
        gc.setStroke(text.getColor());
        outerCanvas.getGraphicsContext2D().strokeText(text.getValue(),text.getX(),text.getY());
    }
    public void init(DrawableText text) {
        GraphicsContext gc = editArea.getGraphicsContext2D();
        gc.setStroke(text.getColor());
        gc.setFont(text.getFont());
        gc.strokeText(text.getValue(),text.getX(),text.getY());
    }

    private void drawBorder(GraphicsContext gc, DrawableText text) {
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(3);
        gc.strokeRect(text.x-10, text.y-20, text.width, text.height);
    }

    public CornerType isNearBorder(DrawableText text, double mouseX, double mouseY) {
        double borderThreshold = 5;  // 阈值，决定多近算在边界上
        double cornerThreshold = 5;  // 角的距离阈值（用于角判断）
        // 检查四个角
        // 左上角
        double distanceToTopLeft = Math.sqrt(Math.pow(mouseX - text.x, 2) + Math.pow(mouseY - text.y, 2));
        if (distanceToTopLeft <= cornerThreshold) {
            return CornerType.NORTHWEST;
        }
        // 右上角
        double distanceToTopRight = Math.sqrt(Math.pow(mouseX - (text.x + text.width), 2) + Math.pow(mouseY - text.y, 2));
        if (distanceToTopRight <= cornerThreshold) {
            return CornerType.NORTHEAST;
        }
        // 左下角
        double distanceToBottomLeft = Math.sqrt(Math.pow(mouseX - text.x, 2) + Math.pow(mouseY - (text.y + text.height), 2));
        if (distanceToBottomLeft <= cornerThreshold) {
            return CornerType.SOUTHWEST;
        }
        // 右下角
        double distanceToBottomRight = Math.sqrt(Math.pow(mouseX - (text.x + text.width), 2) + Math.pow(mouseY - (text.y + text.height), 2));
        if (distanceToBottomRight <= cornerThreshold) {
            return CornerType.SOUTHEAST;
        }
        // 检查左边
        if (mouseX >= text.x - borderThreshold && mouseX <= text.x + borderThreshold) {
            if (mouseY >= text.y - borderThreshold && mouseY <= text.y + text.height + borderThreshold) {
                return CornerType.WEST;
            }
        }
        // 检查右边
        if (mouseX >= text.x + text.width - borderThreshold && mouseX <= text.x + text.width + borderThreshold) {
            if (mouseY >= text.y - borderThreshold && mouseY <= text.y + text.height + borderThreshold) {
                return CornerType.EAST;
            }
        }
        // 检查上边
        if (mouseY >= text.y - borderThreshold && mouseY <= text.y + borderThreshold) {
            if (mouseX >= text.x - borderThreshold && mouseX <= text.x + text.width + borderThreshold) {
                return CornerType.NORTH;
            }
        }
        // 检查下边
        if (mouseY >= text.y + text.height - borderThreshold && mouseY <= text.y + text.height + borderThreshold) {
            if (mouseX >= text.x - borderThreshold && mouseX <= text.x + text.width + borderThreshold) {
                return CornerType.SOUTH;
            }
        }
        // 如果不在任何边界上，返回 EMPTY
        return CornerType.EMPTY;
    }


    public List<DrawableText> getTexts() {
        return texts;
    }

    public void addExternalText(DrawableText text) {
        texts.add(text);
    }

    public void removeExternalText(DrawableText text) {
        texts.remove(text);
    }

    public void clearAll() {
        texts.clear();
        drawAllTexts(editArea.getGraphicsContext2D());
    }




    public static class DrawableText{
        private String value;
        double x, y, width, height;
        double oriX, oriY;
        private Color color;
        private Font font;
        private boolean shouldRender,renderBorder;
        private boolean isUndo;
        public DrawableText(String value, double x, double y,Font font,Color color) {
            this.value=value;
            this.x = x;
            this.y = y;
            this.font=font;
            this.color=color;
            this.width = font.getSize()*value.length()+20;
            this.height = font.getSize()+12;
        }

        public boolean isInside(double mouseX, double mouseY) {
            return mouseX >= (x-10) && mouseX <= (x + width-10) && mouseY >= (y-20) && mouseY <= (y + height-20);
        }

        @Override
        public String toString() {
            return "DrawableText{" +
                    "value='" + value + '\'' +
                    ", x=" + x +
                    ", y=" + y +
                    ", width=" + width +
                    ", height=" + height +
                    ", shouldRender=" + shouldRender +
                    ", font=" + font.getFamily() +
                    '}';
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

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
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

        public void setUndo(boolean undo) {
            isUndo = undo;
        }

        public boolean isUndo() {
            return isUndo;
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

        public boolean canKeep() {
            return Math.abs(this.x-this.oriX)<5||Math.abs(this.y-this.oriY)<5;
        }

        public Color getColor() {
            return color;
        }

        public Font getFont() {
            return font;
        }
    }
}
