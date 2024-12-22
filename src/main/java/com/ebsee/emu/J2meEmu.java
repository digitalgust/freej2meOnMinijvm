package com.ebsee.emu;

import org.mini.apploader.GApplication;
import org.mini.glfm.Glfm;
import org.mini.gui.*;
import org.mini.gui.event.GSizeChangeListener;
import org.recompile.freej2me.FreeJ2ME;
import org.recompile.mobile.Mobile;
import org.recompile.mobile.MobilePlatform;

import java.io.File;
import java.io.FileFilter;

public class J2meEmu extends GApplication {


    static J2meEmu mainApp;
    EmuForm gform;

    public static J2meEmu getInstance() {
        return mainApp;
    }

    public J2meEmu() {
        mainApp = this;
    }

    @Override
    public GForm getForm() {
        if (gform == null) {
            Glfm.glfmSetSupportedInterfaceOrientation(GCallBack.getInstance().getDisplay(), Glfm.GLFMInterfaceOrientationPortrait);
            Glfm.glfmSetDisplayChrome(GCallBack.getInstance().getDisplay(), Glfm.GLFMUserInterfaceChromeFullscreen);
            gform = new EmuForm(null, this);
            gform.addButtons();
            checkMidletHome();//检测midlet home目录在不在，不在就创建一个
            openFileChooser();
            gform.setSizeChangeListener(new GSizeChangeListener() {
                @Override
                public void onSizeChange(int i, int i1) {
                    gform.removeAllButtons();
                    gform.addButtons();
                    GObject frame = gform.getCurFrame();
                    if (frame != null) {
                        frame.setLocation((gform.getW() - frame.getW()) * .5f, 30);
                    }
                }
            });
            gform.addChildrenListener(gform);

        }
        return gform;
    }

    public String getRmsRoot() {
        return getSaveRoot() + "/rms";
    }

    public String getMidlletHome() {
        return getSaveRoot() + "/midletapp";
    }

    private void checkMidletHome() {
        String midletHome = getMidlletHome();
        File dir = new File(midletHome);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void openFileChooser() {

        GFrame chooser = GToolkit.getFileChooser(gform, "J2ME JAR SELECT", getMidlletHome(),
                new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith(".jar") || file.isDirectory();
                    }
                },
                gform.getDeviceWidth() * .8f, gform.getDeviceHeight() * .8f,
                (gobj) -> {
                    String[] args = new String[2];
                    File f = gobj.getAttachment();
                    args[0] = "file:" + f.getAbsolutePath();
                    args[1] = getRmsRoot();
                    main(args);
                },
                gobj -> {
                    //closeApp();
                });
        GToolkit.showFrame(chooser);
    }

    @Override
    public void onClose() {
    }

    public static void main(String args[]) {
        FreeJ2ME app = new FreeJ2ME(args);
    }

}


