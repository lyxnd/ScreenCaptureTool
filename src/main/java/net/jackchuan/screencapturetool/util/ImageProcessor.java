package net.jackchuan.screencapturetool.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @Author : jackchuan
 * @Date 2025/6/21-16:00
 * @Function : process image with opencv
 **/
public class ImageProcessor {

    public static WritableImage toGray(Image image){
        Mat src = ImageFormatHandler.toMat(image);
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(gray), null);
    }

    public static WritableImage laplacian(Image image){
        // 1. JavaFX Image -> OpenCV Mat
        Mat mat = ImageFormatHandler.toMat(image);
        // 2. 转灰度图
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);

        // 3. 拉普拉斯处理
        Mat laplacian = new Mat();
        Imgproc.Laplacian(gray, laplacian, CvType.CV_16S, 3, 1, 0);
        Core.convertScaleAbs(laplacian, laplacian);

        // 4. 叠加锐化（转换回彩色后再加）
        Mat grayColor = new Mat();
        Imgproc.cvtColor(gray, grayColor, Imgproc.COLOR_GRAY2BGR);
        Mat laplacianColor = new Mat();
        Imgproc.cvtColor(laplacian, laplacianColor, Imgproc.COLOR_GRAY2BGR);

        Mat sharpened = new Mat();
        Core.addWeighted(grayColor, 1.0, laplacianColor, 1.0, 0, sharpened);
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(sharpened), null);
    }
    public static WritableImage edgeDetect(Image image){
        Mat mat = ImageFormatHandler.toMat(image);
        // 2. 转灰度
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        // 3. 高斯模糊（减少噪声）
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(gray, blurred, new Size(3, 3), 0);

        // 4. Canny 边缘检测
        Mat edges = new Mat();
        double threshold1=50;
        double threshold2=150;
        Imgproc.Canny(blurred, edges, threshold1, threshold2); // eg: 50, 150
        Mat colorEdges = new Mat();
        Imgproc.cvtColor(edges, colorEdges, Imgproc.COLOR_GRAY2BGR);

        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(colorEdges), null);
    }

    public static WritableImage vhDetect(Image image){
        Mat mat = ImageFormatHandler.toMat(image);
        // 2. 灰度图
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);

        // 3. Sobel X（水平边缘）
        Mat gradX = new Mat();
        Imgproc.Sobel(gray, gradX, CvType.CV_16S, 1, 0, 3, 1, 0);
        Core.convertScaleAbs(gradX, gradX);  // 转换为 8-bit

        // 4. Sobel Y（垂直边缘）
        Mat gradY = new Mat();
        Imgproc.Sobel(gray, gradY, CvType.CV_16S, 0, 1, 3, 1, 0);
        Core.convertScaleAbs(gradY, gradY);  // 转换为 8-bit

        // 5. 合并梯度（近似）
        Mat edge = new Mat();
        Core.addWeighted(gradX, 0.5, gradY, 0.5, 0, edge);

        // 6. 可选：转为彩色显示
        Mat edgeColor = new Mat();
        Imgproc.cvtColor(edge, edgeColor, Imgproc.COLOR_GRAY2BGR);

        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(edgeColor), null);
    }

    public static WritableImage averageSmooth(Image image) {
        Mat mat = ImageFormatHandler.toMat(image);
        // 2. 均值平滑
        Mat blurred = new Mat();
        double kernelSize=5;
        Size ksize = new Size(kernelSize, kernelSize);  // 如 (3, 3)
        Imgproc.blur(mat, blurred, ksize);
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(blurred), null);
    }

    public static WritableImage gaussianSmooth(Image image) {
        Mat mat = ImageFormatHandler.toMat(image);
        Mat blurred = new Mat();
        double kernelSize=5;
        Size ksize = new Size(kernelSize, kernelSize);  // e.g., (5,5)
        double sigmaX=1.5;
        Imgproc.GaussianBlur(mat, blurred, ksize, sigmaX);
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(blurred), null);
    }

    public static WritableImage medianSmooth(Image image) {
        Mat mat = ImageFormatHandler.toMat(image);
        Mat blurred = new Mat();
        int kernelSize=5;
        Imgproc.medianBlur(mat, blurred, kernelSize);
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(blurred), null);
    }
    public static WritableImage invertColor(Image image) {
        Mat mat = ImageFormatHandler.toMat(image);
        Mat inverted = new Mat();
        Core.bitwise_not(mat, inverted);
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(inverted), null);
    }

    public static WritableImage enhanceContrast(Image image) {
        Mat mat = ImageFormatHandler.toMat(image);
        Mat contrasted = new Mat();
        double alpha=1.5;
        double beta=10;
        mat.convertTo(contrasted, -1, alpha, beta); // -1: 保持原通道类型
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(contrasted), null);
    }

    public static WritableImage grayContrast(Image image) {
        Mat mat = ImageFormatHandler.toMat(image);
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        Mat resultMat = new Mat();
        Imgproc.equalizeHist(gray, resultMat);
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(resultMat), null);
    }

    public static WritableImage grabCut(Image image) {
        Mat mat = ImageFormatHandler.toMat(image);
        Mat mask = new Mat(mat.size(), CvType.CV_8UC1, new Scalar(Imgproc.GC_PR_BGD));
        Rect rect = new Rect(10, 10, 300, 300); // 初始矩形框
        Mat bgd = new Mat(), fgd = new Mat();
        Imgproc.grabCut(mat, mask, rect, bgd, fgd, 5, Imgproc.GC_INIT_WITH_RECT);
        Mat result = new Mat();
        Core.compare(mask, new Scalar(Imgproc.GC_PR_FGD), result, Core.CMP_EQ); // result 是 0/255 的二值图像

        // 或者合并前景和可能前景
        Mat foregroundMask = new Mat();
        Core.inRange(mask, new Scalar(Imgproc.GC_PR_FGD), new Scalar(Imgproc.GC_FGD), foregroundMask);

        // 应用掩码到原图像
        Mat foreground = new Mat();
        mat.copyTo(foreground, foregroundMask); // 只保留前景部分

        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(result), null);
    }





    //区域增长算法
    public static WritableImage regionGrowing(Image image) {
        Mat src = ImageFormatHandler.toMat(image);
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Point seedPoint = new Point(100, 100);
        int threshold = 10;
        Mat visited = Mat.zeros(gray.size(), CvType.CV_8U); // 标记是否访问
        Mat output = Mat.zeros(gray.size(), CvType.CV_8U);  // 输出图像

        Queue<Point> queue = new LinkedList<>();
        queue.add(seedPoint);
        visited.put((int) seedPoint.y, (int) seedPoint.x, 1);

        double seedValue = gray.get((int) seedPoint.y, (int) seedPoint.x)[0];

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            int x = (int) p.x, y = (int) p.y;

            output.put(y, x, 255); // 标记为前景

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = x + dx, ny = y + dy;
                    if (nx >= 0 && ny >= 0 && nx < gray.cols() && ny < gray.rows()) {
                        if (visited.get(ny, nx)[0] == 0) {
                            double neighborValue = gray.get(ny, nx)[0];
                            if (Math.abs(neighborValue - seedValue) < threshold) {
                                queue.add(new Point(nx, ny));
                                visited.put(ny, nx, 1);
                            }
                        }
                    }
                }
            }
        }

        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(output), null);
    }


    //同态绿滤波
    public static WritableImage applyHomomorphicFilter(Image image) {
        Mat mat = ImageFormatHandler.toMat(image);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        mat.convertTo(mat, CvType.CV_32F);  // 使用 float 类型以进行 log

        // Step 2: 取对数
        Core.add(mat, Scalar.all(1), mat); // 防止 log(0)
        Core.log(mat, mat);

        // Step 3: DFT
        Mat padded = new Mat();
        int m = Core.getOptimalDFTSize(mat.rows());
        int n = Core.getOptimalDFTSize(mat.cols());
        Core.copyMakeBorder(mat, padded, 0, m - mat.rows(), 0, n - mat.cols(), Core.BORDER_CONSTANT, Scalar.all(0));
        List<Mat> planes = new ArrayList<>();
        planes.add(padded);
        planes.add(Mat.zeros(padded.size(), CvType.CV_32F));
        Mat complexImage = new Mat();
        Core.merge(planes, complexImage);
        Core.dft(complexImage, complexImage);

        // Step 4: 构造高通滤波器 (H(u,v))
        Mat filter = createHomomorphicFilter(padded.size(), 30.0, 0.5, 2.0); // gammaL=0.5, gammaH=2.0
        List<Mat> dftPlanes = new ArrayList<>();
        Core.split(complexImage, dftPlanes);
        Core.multiply(dftPlanes.get(0), filter, dftPlanes.get(0));
        Core.multiply(dftPlanes.get(1), filter, dftPlanes.get(1));
        Core.merge(dftPlanes, complexImage);

        // Step 5: IDFT
        Core.idft(complexImage, complexImage);
        Core.split(complexImage, planes);
        Mat restored = planes.get(0);
        Core.exp(restored, restored); // 反对数
        restored = new Mat(restored, new Rect(0, 0, mat.cols(), mat.rows())); // 裁剪

        // Step 6: 转回 8 位图
        restored.convertTo(restored, CvType.CV_8U);

        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(restored), null);
    }

    public static WritableImage sketch(Image image) {
        Mat src = ImageFormatHandler.toMat(image);
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Mat inverted = new Mat();
        Core.bitwise_not(gray, inverted);
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(inverted, blurred, new Size(21, 21), 0);
        // 颜色加深混合：sketch = gray / (255 - blurred) * 255，夹紧到 [0, 255]
        Mat grayF = new Mat(), denomF = new Mat();
        gray.convertTo(grayF, CvType.CV_32F);
        blurred.convertTo(denomF, CvType.CV_32F, -1.0, 255.0);  // 255 - blurred
        Core.max(denomF, new Scalar(1.0), denomF);
        Mat sketchF = new Mat();
        Core.divide(grayF, denomF, sketchF, 255.0);
        Core.min(sketchF, new Scalar(255.0), sketchF);
        Mat result = new Mat();
        sketchF.convertTo(result, CvType.CV_8U);
        Mat colorResult = new Mat();
        Imgproc.cvtColor(result, colorResult, Imgproc.COLOR_GRAY2BGR);
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(colorResult), null);
    }

    public static WritableImage emboss(Image image) {
        Mat src = ImageFormatHandler.toMat(image);
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        kernel.put(0, 0, new float[]{-2, -1, 0, -1, 1, 1, 0, 1, 2});
        Mat result = new Mat();
        Imgproc.filter2D(src, result, -1, kernel);
        Core.add(result, new Scalar(128, 128, 128), result);
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(result), null);
    }

    public static WritableImage bilateralFilter(Image image) {
        Mat src = ImageFormatHandler.toMat(image);
        Mat result = new Mat();
        Imgproc.bilateralFilter(src, result, 9, 75, 75);
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(result), null);
    }

    public static WritableImage clahe(Image image) {
        Mat src = ImageFormatHandler.toMat(image);
        Mat lab = new Mat();
        Imgproc.cvtColor(src, lab, Imgproc.COLOR_BGR2Lab);
        List<Mat> channels = new ArrayList<>();
        Core.split(lab, channels);
        org.opencv.imgproc.CLAHE claheObj = Imgproc.createCLAHE(2.0, new Size(8, 8));
        claheObj.apply(channels.get(0), channels.get(0));
        Core.merge(channels, lab);
        Mat result = new Mat();
        Imgproc.cvtColor(lab, result, Imgproc.COLOR_Lab2BGR);
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(result), null);
    }

    public static WritableImage dilate(Image image) {
        Mat src = ImageFormatHandler.toMat(image);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Mat result = new Mat();
        Imgproc.dilate(src, result, kernel);
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(result), null);
    }

    public static WritableImage erode(Image image) {
        Mat src = ImageFormatHandler.toMat(image);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Mat result = new Mat();
        Imgproc.erode(src, result, kernel);
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(result), null);
    }

    public static WritableImage otsuThreshold(Image image) {
        Mat src = ImageFormatHandler.toMat(image);
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Mat result = new Mat();
        Imgproc.threshold(gray, result, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        Mat colorResult = new Mat();
        Imgproc.cvtColor(result, colorResult, Imgproc.COLOR_GRAY2BGR);
        return SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(colorResult), null);
    }

    // 创建高通滤波器
    private static Mat createHomomorphicFilter(Size size, double cutoff, double gammaL, double gammaH) {
        int rows = (int) size.height;
        int cols = (int) size.width;
        Mat filter = Mat.zeros(rows, cols, CvType.CV_32F);
        Point center = new Point(cols / 2.0, rows / 2.0);
        double c2 = cutoff * cutoff;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double D2 = Math.pow(i - center.y, 2) + Math.pow(j - center.x, 2);
                double H = (gammaH - gammaL) * (1 - Math.exp(-D2 / (2 * c2))) + gammaL;
                filter.put(i, j, H);
            }
        }
        return filter;
    }
}
