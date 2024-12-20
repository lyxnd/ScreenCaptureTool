package net.jackchuan.screencapturetool.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.util.Pair;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.*;
import java.util.List;

public class ScreenCaptureUtil {
    private static final int SRCCOPY = 0x00CC0020;
    public static int PHYSICAL_SIZE=0;
    public static int DEFAULT_SIZE=1;

    public static BufferedImage captureWithPython(int x, int y, int width, int height) throws FileNotFoundException {
        BufferedImage image=null;
        try {
            // 获取缩放比例（适用于 HiDPI 显示器）
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            double scaleX = gc.getDefaultTransform().getScaleX();
            double scaleY = gc.getDefaultTransform().getScaleY();
            // 调整后的宽度和高度
            width = (int) (width * scaleX);
            height = (int) (height * scaleY);
            x= (int) (x*scaleX);
            y= (int) (y*scaleY);
            // Python 脚本路径
            File script = getExecutableScript();
            // 构建命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", script.getAbsolutePath(),
                    String.valueOf(x), String.valueOf(y),
                    String.valueOf(width), String.valueOf(height)
            );

            processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出
            Process process = processBuilder.start();

            // 读取脚本输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String outputPath = reader.readLine(); // 读取第一行，即保存路径
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                image = ImageIO.read(new File(outputPath));
                System.out.println("Screenshot saved to: " + outputPath);
            } else {
                System.out.println("Error occurred. Exit code: " + exitCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    public static BufferedImage captureWithJNA(int x, int y, int width, int height) throws AWTException {
        Mat frame = new Mat();
        VideoCapture capture = new VideoCapture(0); // 使用 0 表示主摄像头

        if (capture.isOpened()) {
            capture.read(frame);
            Imgcodecs.imwrite("screenshot.jpg", frame);
            System.out.println("Screenshot saved!");
        }
        capture.release();
        return captureWithFX(x, y, width, height);
    }

//    public static BufferedImage captureWithJNA(int x, int y, int width, int height) {
//        User32 user32 = User32.INSTANCE;
//        GDI32 gdi32 = GDI32.INSTANCE;
//
//        WinDef.HWND desktopWindow = user32.GetDesktopWindow();
//        WinDef.HDC hdcWindow = user32.GetDC(desktopWindow);
//
//        // 获取 DPI 缩放比例
//        int dpiX = gdi32.GetDeviceCaps(hdcWindow, net.jackchuan.screencapturetool.util.impl.GDI32.LOGPIXELSX);
//        int dpiY = gdi32.GetDeviceCaps(hdcWindow, net.jackchuan.screencapturetool.util.impl.GDI32.LOGPIXELSY);
//        double scaleX = dpiX / 96.0;
//        double scaleY = dpiY / 96.0;
//
//        // 调整坐标和尺寸
//        int adjustedX = (int) (x * scaleX);
//        int adjustedY = (int) (y * scaleY);
//        int adjustedWidth = (int) (width * scaleX);
//        int adjustedHeight = (int) (height * scaleY);
//
//        // 创建内存 DC 和位图
//        WinDef.HDC hdcMemDC = gdi32.CreateCompatibleDC(hdcWindow);
//        WinDef.HBITMAP hBitmap = gdi32.CreateCompatibleBitmap(hdcWindow, adjustedWidth, adjustedHeight);
//        if (hBitmap == null) {
//            throw new RuntimeException("Failed to create compatible bitmap.");
//        }
//        gdi32.SelectObject(hdcMemDC, hBitmap);
//
//        // 将屏幕内容复制到内存 DC
//        boolean bitBltResult = gdi32.BitBlt(hdcMemDC, 0, 0, adjustedWidth, adjustedHeight, hdcWindow, adjustedX, adjustedY, SRCCOPY);
//        if (!bitBltResult) {
//            throw new RuntimeException("BitBlt failed.");
//        }
//
//        // 获取位图数据
//        WinGDI.BITMAPINFO bmpInfo = new WinGDI.BITMAPINFO();
//        bmpInfo.bmiHeader.biSize = new WinDef.DWORD(bmpInfo.bmiHeader.size()).intValue();
//        bmpInfo.bmiHeader.biWidth = adjustedWidth;
//        bmpInfo.bmiHeader.biHeight = -adjustedHeight; // 顶部朝上
//        bmpInfo.bmiHeader.biPlanes = 1;
//        bmpInfo.bmiHeader.biBitCount = 32; // 每像素 32 位 (ARGB)
//        bmpInfo.bmiHeader.biCompression = WinGDI.BI_RGB;
//
//        int bufferSize = adjustedWidth * adjustedHeight * 4; // 每像素 4 字节
//        Memory buffer = new Memory(bufferSize);
//
//        int result = gdi32.GetDIBits(hdcWindow, hBitmap, 0, adjustedHeight, buffer, bmpInfo, WinGDI.DIB_RGB_COLORS);
//        if (result == 0) {
//            throw new RuntimeException("GetDIBits failed.");
//        }
//
//        // 转换为 BufferedImage
//        BufferedImage image = new BufferedImage(adjustedWidth, adjustedHeight, BufferedImage.TYPE_INT_ARGB);
//        int[] rgbArray = new int[adjustedWidth * adjustedHeight];
//        for (int i = 0; i < rgbArray.length; i++) {
//            int b = buffer.getByte(i * 4) & 0xFF;
//            int g = buffer.getByte(i * 4 + 1) & 0xFF;
//            int r = buffer.getByte(i * 4 + 2) & 0xFF;
//            int a = buffer.getByte(i * 4 + 3) & 0xFF;
//            rgbArray[i] = (a << 24) | (r << 16) | (g << 8) | b;
//        }
//        image.setRGB(0, 0, adjustedWidth, adjustedHeight, rgbArray, 0, adjustedWidth);
//
//        // 释放资源
//        user32.ReleaseDC(desktopWindow, hdcWindow);
//        gdi32.DeleteObject(hBitmap);
//        gdi32.DeleteDC(hdcMemDC);
//
//        return image;
//    }


    public static BufferedImage captureWithAWT(int x,int y,int width,int height) throws AWTException {
        Robot awtRobot = new Robot();
        Rectangle screenRect = new Rectangle(x, y, width, height); // 定义截图区域
        MultiResolutionImage capture = awtRobot.createMultiResolutionScreenCapture(screenRect);
        List<Image> resolutionVariants = capture.getResolutionVariants();
        if (resolutionVariants.size() > 1) {
            return ImageFormatHandler.convertToBufferedImage(resolutionVariants.get(1));
        } else {
            return ImageFormatHandler.convertToBufferedImage(resolutionVariants.get(0));
        }
    }

    public static BufferedImage captureWithFX(int x,int y,int width,int height) throws AWTException {
        Rectangle2D screenRegion = new Rectangle2D(x, y, width, height);
        // 使用 Robot 截取屏幕
        javafx.scene.robot.Robot robot = new javafx.scene.robot.Robot();
        javafx.scene.image.Image screenshot = robot.getScreenCapture(null, screenRegion);
        return SwingFXUtils.fromFXImage(screenshot,null);
    }
    public static double getScreenScale() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();

        // 获取屏幕的 DPI
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();

        return dpi / 96.0;
    }

    public static Pair<Integer,Integer> getScreenSize(int type){
        if(type==PHYSICAL_SIZE){
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            DisplayMode dm = gd.getDisplayMode();
            int physicalWidth = dm.getWidth();
            int physicalHeight = dm.getHeight();
            return new Pair<>(physicalWidth, physicalHeight);
        }else {
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            return new Pair<>(size.width, size.height);
        }
    }

    private static File getExecutableScript() throws IOException {
        InputStream scriptStream = ScreenCaptureToolApp.class.getResourceAsStream("scripts/capture.py");
        if (scriptStream == null) {
            throw new FileNotFoundException("Resource not found: scripts/capture.py");
        }
        // 创建临时文件
        File tempScriptFile = File.createTempFile("capture", ".py");
        try (OutputStream outputStream = new FileOutputStream(tempScriptFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = scriptStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        // 设置临时文件可执行权限（部分系统可能需要）
        tempScriptFile.setExecutable(true);
        return tempScriptFile;
    }
}
