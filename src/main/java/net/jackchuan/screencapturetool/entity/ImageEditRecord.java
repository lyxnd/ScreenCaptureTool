package net.jackchuan.screencapturetool.entity;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/14 19:39
 */
public class ImageEditRecord {
    private String type;
    private double x,y;
    private double w,h;
    public ImageEditRecord(String type,double x,double y,double w,double h){
        this.type=type;
        this.x=x;
        this.y=y;
        this.w=w;
        this.h=h;
    }

    public void setW(double w) {
        this.w = w;
    }

    public void setH(double h) {
        this.h = h;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public double getH() {
        return h;
    }

    public double getW() {
        return w;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ImageEditRecord{" +
                "type='" + type + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                '}';
    }
}
