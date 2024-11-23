package net.jackchuan.screencapturetool.util;

import net.jackchuan.screencapturetool.controller.CaptureDisplayController;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/23 13:30
 */
public class ControllerInstance {
    private static ControllerInstance instance;
    private CaptureDisplayController controller;
    private ControllerInstance() {}

    public static synchronized ControllerInstance getInstance() {
        if (instance == null) {
            instance = new ControllerInstance();
        }
        return instance;
    }

    public void setController(CaptureDisplayController controller) {
        this.controller = controller;
    }

    public CaptureDisplayController getController() {
        return controller;
    }
}
