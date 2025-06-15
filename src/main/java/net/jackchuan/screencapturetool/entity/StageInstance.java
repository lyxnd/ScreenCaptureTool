package net.jackchuan.screencapturetool.entity;

import net.jackchuan.screencapturetool.controller.CaptureDisplayController;
import net.jackchuan.screencapturetool.controller.EditRecordController;
import net.jackchuan.screencapturetool.controller.SettingController;
import net.jackchuan.screencapturetool.external.stage.ProgressStage;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/23 16:53
 */
public class StageInstance {
    private static StageInstance instance;
    private ProgressStage progressStage;
    private StageInstance() {}

    public static synchronized StageInstance getInstance() {
        if (instance == null) {
            instance = new StageInstance();
        }
        return instance;
    }

    public ProgressStage getProgressStage() {
        return progressStage;
    }

    public void setProgressStage(ProgressStage progressStage) {
        this.progressStage = progressStage;
    }
}
