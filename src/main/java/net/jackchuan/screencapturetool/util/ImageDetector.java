package net.jackchuan.screencapturetool.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.bytedeco.opencv.opencv_core.Rect;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/11 15:22
 */
public class ImageDetector {

    /**
     * 返回图像中检测到的所有矩形区域坐标（原始尺寸坐标）。
     * 每个元素为 int[]{x, y, width, height}。
     */
    public static List<int[]> detectRects(Image image) {
        Mat originalImage = ImageFormatHandler.toMat(image);
        double scaleFactor = 0.4;
        Mat resized = new Mat();
        Imgproc.resize(originalImage, resized,
                new Size(originalImage.cols() * scaleFactor, originalImage.rows() * scaleFactor));
        Mat gray = new Mat();
        Imgproc.cvtColor(resized, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0);
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 30, 120);
        // 膨胀使断开的边缘连接，提高轮廓闭合率
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(edges, edges, kernel);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        // RETR_LIST 同时找外层和内层轮廓，适合检测嵌套 UI 区域
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        List<int[]> rects = new ArrayList<>();
        double invScale = 1.0 / scaleFactor;
        double minAreaScaled = 50 * scaleFactor * 50 * scaleFactor;
        for (MatOfPoint contour : contours) {
            if (Imgproc.contourArea(contour) < minAreaScaled) continue;
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double perimeter = Imgproc.arcLength(contour2f, true);
            MatOfPoint2f approx2f = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f, approx2f, 0.02 * perimeter, true);
            int vertices = (int) approx2f.total();
            // 放宽：4~8 个顶点均视为矩形类区域（圆角、轻微形变都能通过）
            if (vertices < 4 || vertices > 8) continue;
            org.opencv.core.Rect br = Imgproc.boundingRect(new MatOfPoint(approx2f.toArray()));
            double aspect = (double) br.width / br.height;
            if (aspect < 0.1 || aspect > 10) continue;
            int x = (int) (br.x * invScale);
            int y = (int) (br.y * invScale);
            int w = (int) (br.width * invScale);
            int h = (int) (br.height * invScale);
            if (w > 50 && h > 50) {
                rects.add(new int[]{x, y, w, h});
            }
        }
        return rects;
    }

    public static Image detectRect(Image image){
        Mat originalImage = ImageFormatHandler.toMat(image);
        Mat resizedImage = new Mat();
        double scaleFactor=0.4;
        Imgproc.resize(originalImage, resizedImage, new Size(originalImage.cols() * scaleFactor, originalImage.rows() * scaleFactor));
//        displayProcess(resizedImage,"缩小处理图像");
        Mat grayImage = new Mat();
        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_BGR2GRAY);
//        displayProcess(grayImage,"灰化处理图像");
        Mat edges = new Mat();
        Imgproc.Canny(grayImage, edges, 50, 150);
//        displayProcess(edges,"边缘提取图片");

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // 5. 筛选矩形轮廓
        for (MatOfPoint contour : contours) {
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double perimeter = Imgproc.arcLength(contour2f, true);

            // 使用多边形逼近轮廓
            MatOfPoint2f approx2f = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f, approx2f, 0.02 * perimeter, true);

            // 判断是否为矩形（4 个顶点）
            if (approx2f.total() == 4) {
                MatOfPoint approx = new MatOfPoint(approx2f.toArray());

                // 检查是否为凸形
                if (Imgproc.isContourConvex(approx)) {
                    // 在原图上绘制轮廓
                    Imgproc.drawContours(resizedImage, List.of(approx), -1, new Scalar(0, 255, 0), 3);
                }
            }
        }
//        displayProcess(resizedImage,"提取后的图片");
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(resizedImage),null);
    }



    public static void displayProcess(Mat mat,String title){
        Stage stage=new Stage();
        stage.setTitle(title);
        BufferedImage bufferedImage = ImageFormatHandler.toBufferedImage(mat);
        ImageView imageView=new ImageView(SwingFXUtils.toFXImage(bufferedImage,null));
        StackPane stackPane=new StackPane(imageView);
        Scene scene=new Scene(stackPane,bufferedImage.getWidth(),bufferedImage.getHeight());
        stage.setScene(scene);
        stage.show();
    }

}
