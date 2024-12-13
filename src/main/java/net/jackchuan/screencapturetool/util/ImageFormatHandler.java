package net.jackchuan.screencapturetool.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/21 21:05
 */
public class ImageFormatHandler {

    //合并两个canvas内容
    public static WritableImage combineCanvases(Canvas imageCanvas, Canvas drawingCanvas) {
        // 获取 canvas 的宽度和高度
        double width = imageCanvas.getWidth();
        double height = imageCanvas.getHeight();

        // 创建一个 Canvas 用于合并两个 Canvas 的内容
        Canvas combinedCanvas = new Canvas(width, height);
        GraphicsContext gc = combinedCanvas.getGraphicsContext2D();

        // 绘制第一个 Canvas（显示图片的 Canvas）到合并 Canvas
        gc.drawImage(imageCanvas.snapshot(null, null), 0, 0);

        // 绘制第二个 Canvas（绘制内容的 Canvas）到合并 Canvas
        gc.drawImage(drawingCanvas.snapshot(null, null), 0, 0);

        // 创建一个 WritableImage，用于从合并的 Canvas 获取最终的图像
        return combinedCanvas.snapshot(null, null);
    }

    public static WritableImage getTransparentImage(Canvas editArea){
        Image image = new Image(ScreenCaptureToolApp.class.getResource("assets/transparent.png").toExternalForm());
        // 将 Image 转换为 WritableImage
        WritableImage writableImage = new WritableImage(image.getPixelReader(),(int) image.getWidth(), (int) image.getHeight());
        return writableImage;
    }

    public static BufferedImage fxImageToBufferedImage(Image writableImage){
        return SwingFXUtils.fromFXImage(writableImage,null);
    }

    public static Image bufferedToFXImage(BufferedImage screenshot) throws IOException {
        return SwingFXUtils.toFXImage(screenshot,null);
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

    public static IntegerPair getScaledSize(Image image, double width, double height){
        double w = image.getWidth();
        double h = image.getHeight();
        IntegerPair pair = new IntegerPair();
        double scale=1;
        if(width<w||height<h){
            scale=height/h*0.25;
        }
        pair.setW(w*scale);
        pair.setH(h*scale);
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
