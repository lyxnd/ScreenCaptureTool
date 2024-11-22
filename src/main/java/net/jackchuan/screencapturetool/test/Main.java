package net.jackchuan.screencapturetool.test;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/22 13:01
 */

import com.sun.jna.platform.win32.GDI32Util;
import com.sun.jna.platform.win32.WinDef;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Main {

    public static void main(String[] args) {
        try {
            //Get JNA User32 Instace
            com.sun.jna.platform.win32.User32 user32 = com.sun.jna.platform.win32.User32.INSTANCE;
            //Get desktop windows handler
            WinDef.HWND hwnd = user32.GetDesktopWindow();
            //Create a BufferedImage
            BufferedImage bi;
            //Function that take screenshot and set to BufferedImage bi
            bi = GDI32Util.getScreenshot(hwnd);
            //Save screenshot to a file
            ImageIO.write(getHighResolutionImage(), "png", new java.io.File("screenshot1.png"));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage getHighResolutionImage() throws AWTException {
        Robot robot = new Robot();
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        MultiResolutionImage multiResolutionImage = robot.createMultiResolutionScreenCapture(new Rectangle(0, 0,
                (int) size.getWidth(), (int) size.getHeight()));
        Image image = multiResolutionImage.getResolutionVariant((int) size.getWidth(), (int) size.getHeight());
        BufferedImage screenCapture = (BufferedImage) image;
        return screenCapture;
    }

}
