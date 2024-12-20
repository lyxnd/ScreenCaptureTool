package net.jackchuan.screencapturetool.external;

import javafx.scene.image.Image;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/15 14:17
 */
public class ExternalTextHandler {


    public static class DrawableText{
        private String value;
        double x, y, width, height;
        double oriX, oriY;
        private boolean shouldRender,renderBorder;
        public DrawableText(String value, double x, double y, double width, double height) {
            this.value=value;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
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
    }
}
