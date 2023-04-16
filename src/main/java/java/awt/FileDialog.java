package java.awt;

import org.mini.gui.*;
import org.mini.gui.event.GActionListener;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

public class FileDialog extends Window {

    public static final int LOAD = 0;

    public static final int SAVE = 1;

    FilenameFilter filter;
    String parentPath;
    String fileName;
    String title;


    public FileDialog(Frame parent, String title, int mode) {
        this.title = title;
    }


    public synchronized void setFilenameFilter(FilenameFilter filter) {
        this.filter = filter;
        if (getPeer() != null) {
            //peer.setFilenameFilter(filter);
        }
    }

    public void setVisible(boolean b) {
        GForm gform = GCallBack.getInstance().getApplication().getForm();
        GObject peer = GToolkit.getFileChooser(gform, "Select J2me Jar", null, new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (filter == null) return true;
                if (file.isDirectory()) return true;
                return filter.accept(file.getParentFile(), file.getName());
            }
        }, gform.getDeviceWidth(), gform.getDeviceHeight(), new GActionListener() {
            @Override
            public void action(GObject gObject) {
                File f = gObject.getAttachment();
                fileName = f.getName();
                parentPath = f.getParent();
            }
        }, null);
        GFrame gf = (GFrame) peer;
        gf.setTitle(title);

        peer.setVisible(b);
        GToolkit.showFrame(peer);
        while (true) {
            if (peer.getForm().contains(peer)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            } else {
                break;
            }
        }
        setPeer(peer);
    }


    public String getDirectory() {
        return parentPath;
    }

    public String getFile() {
        return fileName;
    }
}
