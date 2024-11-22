package net.jackchuan.screencapturetool.util;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;

import java.awt.image.BufferedImage;

public class ScreenCaptureUtil {
    private static final int SRCCOPY = 0x00CC0020;

    public static BufferedImage captureScreenRegion(int x, int y, int width, int height) {
        User32 user32 = User32.INSTANCE;
        GDI32 gdi32 = GDI32.INSTANCE;

        // 获取屏幕设备上下文 (DC)
        WinDef.HWND desktopWindow = user32.GetDesktopWindow();
        WinDef.HDC hdcWindow = user32.GetDC(desktopWindow);

        // 创建兼容的内存 DC 和位图
        WinDef.HDC hdcMemDC = gdi32.CreateCompatibleDC(hdcWindow);
        WinDef.HBITMAP hBitmap = gdi32.CreateCompatibleBitmap(hdcWindow, width, height);
        if (hBitmap == null) {
            throw new RuntimeException("Failed to create compatible bitmap.");
        }
        gdi32.SelectObject(hdcMemDC, hBitmap);

        // 将屏幕内容复制到内存 DC
        boolean bitBltResult = gdi32.BitBlt(hdcMemDC, 0, 0, width, height, hdcWindow, x, y, SRCCOPY);
        if (!bitBltResult) {
            throw new RuntimeException("BitBlt failed.");
        }

        // 设置位图信息
        WinGDI.BITMAPINFO bmpInfo = new WinGDI.BITMAPINFO();
        bmpInfo.bmiHeader.biSize = new WinDef.DWORD(bmpInfo.bmiHeader.size()).intValue();
        bmpInfo.bmiHeader.biWidth = width;
        bmpInfo.bmiHeader.biHeight = -height; // 使用负值以确保图像顶部朝上
        bmpInfo.bmiHeader.biPlanes = 1;
        bmpInfo.bmiHeader.biBitCount = 32; // 每像素 32 位 (ARGB)
        bmpInfo.bmiHeader.biCompression = WinGDI.BI_RGB;

        // 创建内存缓冲区来存储像素数据
        int bufferSize = width * height * 4; // 每像素 4 字节
        Memory buffer = new Memory(bufferSize);

        // 获取位图像素数据
        int result = gdi32.GetDIBits(hdcWindow, hBitmap, 0, height, buffer, bmpInfo, WinGDI.DIB_RGB_COLORS);
        if (result == 0) {
            throw new RuntimeException("GetDIBits failed.");
        }

        // 将像素数据转换为 BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] rgbArray = new int[width * height];
        for (int i = 0; i < rgbArray.length; i++) {
            int b = buffer.getByte(i * 4) & 0xFF;
            int g = buffer.getByte(i * 4 + 1) & 0xFF;
            int r = buffer.getByte(i * 4 + 2) & 0xFF;
            int a = buffer.getByte(i * 4 + 3) & 0xFF;
            rgbArray[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        image.setRGB(0, 0, width, height, rgbArray, 0, width);

        // 释放 GDI 资源
        user32.ReleaseDC(desktopWindow, hdcWindow);
        gdi32.DeleteObject(hBitmap);
        gdi32.DeleteDC(hdcMemDC);

        return image;
    }

}
