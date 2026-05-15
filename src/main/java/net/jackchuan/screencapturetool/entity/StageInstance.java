package net.jackchuan.screencapturetool.entity;

import lombok.Data;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;
import net.jackchuan.screencapturetool.controller.EditRecordController;
import net.jackchuan.screencapturetool.controller.SettingController;
import net.jackchuan.screencapturetool.external.stage.ProgressStage;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/23 16:53
 */
@Data
public class StageInstance {
    private static StageInstance instance;
    private volatile ProgressStage progressStage;
    private StageInstance() {}

    public static synchronized StageInstance getInstance() {
        if (instance == null) {
            instance = new StageInstance();
        }
        return instance;
    }

}
