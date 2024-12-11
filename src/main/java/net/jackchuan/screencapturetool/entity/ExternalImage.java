package net.jackchuan.screencapturetool.entity;

import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/4 22:33
 */
public class ExternalImage {
    private ImageView imageView;
    private String imageName;

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public String getImageName() {
        return imageName;
    }
}
