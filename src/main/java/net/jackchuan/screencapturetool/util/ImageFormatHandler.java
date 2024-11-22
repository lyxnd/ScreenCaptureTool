package net.jackchuan.screencapturetool.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import java.awt.image.BufferedImage;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/21 21:05
 */
public class ImageFormatHandler {
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
