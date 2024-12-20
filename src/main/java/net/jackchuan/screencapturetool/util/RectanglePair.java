package net.jackchuan.screencapturetool.util;

import net.jackchuan.screencapturetool.external.ExternalImageHandler;
import net.jackchuan.screencapturetool.util.impl.CornerType;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/29 22:33
 */
public class RectanglePair {
    public double x;
    public double y;
    public double w;
    public double h;

    public static RectanglePair calculateResizedImage(ExternalImageHandler.DrawableImage image, double mouseX, double mouseY, CornerType type) {
        RectanglePair rect=new RectanglePair();
        rect.x=image.getX();
        rect.y=image.getY();
        rect.w=image.getWidth();
        rect.h=image.getHeight();
        switch (type) {
            case SOUTH ->{
                rect.h=mouseY-image.getY();
            }
            case NORTH ->{
                rect.y=mouseY;
                rect.h=image.getY()-mouseY+image.getHeight();
            }
            case WEST ->{
                rect.w=image.getX()-mouseX+image.getWidth();
                rect.x=mouseX;
            }
            case EAST ->{
                rect.w=mouseX-image.getX();
            }
            case NORTHEAST ->{//右上
                rect.y=mouseY;
                rect.w=mouseX-image.getX();
                rect.h=image.getY()-mouseY+image.getHeight();
            }
            case NORTHWEST ->{//左上
                rect.w=image.getX()-mouseX+image.getWidth();
                rect.h=image.getY()-mouseY+image.getHeight();
                rect.x=mouseX;
                rect.y=mouseY;
            }
            case SOUTHEAST ->{//右下
                rect.w=mouseX-image.getX();
                rect.h=mouseY-image.getY();
            }
            case SOUTHWEST ->{//左下
                rect.x=mouseX;
                rect.w=image.getX()-mouseX+image.getWidth();
                rect.h=mouseY-image.getY();
            }
        }
        String str="OriginalImage{" +
                "x=" + image.getX() +
                ", y=" + image.getY() +
                ", w=" + image.getWidth() +
                ", h=" + image.getHeight()+
                '}';
        System.out.println(toString(rect.getX(), rect.getY(), rect.getW(), rect.getH()));
        System.out.println(str);
        return rect;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setH(double h) {
        this.h = h;
    }

    public void setW(double w) {
        this.w = w;
    }

    public double getH() {
        return h;
    }

    public double getW() {
        return w;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public static String toString(double x,double y,double w,double h) {
        return "RectanglePair{" +
                "x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                '}';
    }
}
