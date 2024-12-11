package net.jackchuan.screencapturetool.util;
import javafx.embed.swing.SwingFXUtils;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/21 22:35
 */
public class TransferableImage implements Transferable {
    private java.awt.Image image;

    public TransferableImage(javafx.scene.image.Image image) {
        this.image = SwingFXUtils.fromFXImage(image,null);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return DataFlavor.imageFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!DataFlavor.imageFlavor.equals(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return image;
    }
}
