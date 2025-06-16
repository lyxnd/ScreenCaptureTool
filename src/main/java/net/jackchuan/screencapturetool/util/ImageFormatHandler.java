package net.jackchuan.screencapturetool.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/21 21:05
 */
public class ImageFormatHandler {

    public static WritableImage toFXImage(BufferedImage bImage) {
        int width = bImage.getWidth();
        int height = bImage.getHeight();
        WritableImage wr = new WritableImage(width, height);
        PixelWriter pw = wr.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = bImage.getRGB(x, y); // 包含 alpha
                pw.setArgb(x, y, argb);
            }
        }
        return wr;
    }

    public static BufferedImage toBufferedImage(Image fxImage) {
        int width = (int) fxImage.getWidth();
        int height = (int) fxImage.getHeight();

        // 创建一个 ARGB 类型的 BufferedImage
        BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // 获取 JavaFX 图像的像素读取器
        PixelReader pixelReader = fxImage.getPixelReader();
        if (pixelReader == null) {
            throw new IllegalArgumentException("Image has no PixelReader");
        }

        // 准备缓冲区
        int[] buffer = new int[width];
        WritablePixelFormat<IntBuffer> format = WritablePixelFormat.getIntArgbInstance();

        // 每行逐行复制
        for (int y = 0; y < height; y++) {
            pixelReader.getPixels(0, y, width, 1, format, buffer, 0, width);
            bimg.getRaster().setDataElements(0, y, width, 1, buffer);
        }

        return bimg;
    }

    public static BufferedImage cropImage(Canvas canvas,Image image,double startX,double startY,double currentX,double currentY){
        //TODO 裁剪不准确
        double imageStartX = (startX - canvas.getTranslateX()) / canvas.getScaleX();
        double imageStartY = (startY - canvas.getTranslateY()) / canvas.getScaleY();
        double imageEndX = (currentX - canvas.getTranslateX()) / canvas.getScaleX();
        double imageEndY = (currentY - canvas.getTranslateY()) / canvas.getScaleY();
        // 计算矩形区域的宽度和高度
        int width = (int) Math.abs(imageEndX - imageStartX);
        int height = (int) Math.abs(imageEndY - imageStartY);
        // 确保起始点为左上角的坐标
        int x = (int) Math.min(imageStartX, imageEndX);
        int y = (int) Math.min(imageStartY, imageEndY);
        BufferedImage bf;
        if(image!=null){
            bf = SwingFXUtils.fromFXImage(image, null);
        }else {
            bf = SwingFXUtils.fromFXImage(canvas.snapshot(null,null),null);
        }
        return bf.getSubimage(x, y, width, height);
    }

    public static WritableImage getTransparentImage(Canvas editArea) {
        Image image = new Image(ScreenCaptureToolApp.class.getResource("assets/transparent.png").toExternalForm());
        // 将 Image 转换为 WritableImage
        WritableImage writableImage = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
        return writableImage;
    }


    //awt Image to BufferedImage
    public static BufferedImage convertToBufferedImage(java.awt.Image image) {
        // 创建一个与原始 Image 尺寸相同的 BufferedImage
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // 获取 Graphics2D 对象来绘制图像
        Graphics2D g2d = bufferedImage.createGraphics();
        // 绘制图像到 BufferedImage
        g2d.drawImage(image, 0, 0, null);

        // 释放资源
        g2d.dispose();

        return bufferedImage;
    }

    public static RectanglePair getScaledSize(Image image, double width, double height) {
        double w = image.getWidth();
        double h = image.getHeight();
        RectanglePair pair = new RectanglePair();
        double scale = 1;
        if (width < w || height < h) {
            scale = height / h * 0.25;
        }
        pair.setW(w * scale);
        pair.setH(h * scale);
        return pair;
    }


    public static Mat toMat(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        // 创建一个Mat对象
        Mat mat = new Mat(height, width, CvType.CV_8UC3);  // 使用CV_8UC3表示3通道的图像
        PixelReader pixelReader = image.getPixelReader();
        // 逐个像素提取并填充到Mat对象中
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                byte blue = (byte) (color.getBlue() * 255);
                byte green = (byte) (color.getGreen() * 255);
                byte red = (byte) (color.getRed() * 255);

                // 将像素值存入Mat（BGR格式）
                mat.put(y, x, new byte[]{blue, green, red});
            }
        }
        return mat;
    }

    public static BufferedImage toBufferedImage(Mat mat) {
        if (mat.empty()) {
            throw new IllegalArgumentException("Input Mat is empty");
        }

        int width = mat.width();
        int height = mat.height();

        // 单通道灰度图像
        if (mat.channels() == 1) {
            // 确保 Mat 类型为 CV_8U（8 位无符号整型）
            if (mat.type() != CvType.CV_8U) {
                Mat converted = new Mat();
                mat.convertTo(converted, CvType.CV_8U);
                mat = converted;
            }

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            byte[] data = new byte[width * height];
            mat.get(0, 0, data);
            bufferedImage.getRaster().setDataElements(0, 0, width, height, data);
            return bufferedImage;

            // 三通道彩色图像（BGR格式）
        } else if (mat.channels() == 3) {
            // 确保 Mat 类型为 CV_8UC3（8 位无符号整型，3 通道）
            if (mat.type() != CvType.CV_8UC3) {
                Mat converted = new Mat();
                mat.convertTo(converted, CvType.CV_8UC3);
                mat = converted;
            }

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            byte[] data = new byte[width * height * 3];
            mat.get(0, 0, data);

            // OpenCV 默认是 BGR 格式，而 BufferedImage 是 RGB 格式，需要交换通道
            for (int i = 0; i < data.length; i += 3) {
                byte blue = data[i];
                data[i] = data[i + 2];  // 将蓝色和红色交换
                data[i + 2] = blue;
            }

            bufferedImage.getRaster().setDataElements(0, 0, width, height, data);
            return bufferedImage;

        } else {
            throw new IllegalArgumentException("Unsupported Mat format: channels = " + mat.channels() + ", type = " + mat.type());
        }
    }


}
