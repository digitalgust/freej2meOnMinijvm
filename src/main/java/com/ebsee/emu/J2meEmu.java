package com.ebsee.emu;

import org.mini.apploader.GApplication;
import org.mini.glfm.Glfm;
import org.mini.gui.*;
import org.mini.gui.callback.GCallBack;
import org.mini.gui.event.GSizeChangeListener;
import org.mini.layout.loader.XuiAppHolder;
import org.recompile.freej2me.FreeJ2ME;
import org.recompile.mobile.Mobile;
import org.recompile.mobile.MobilePlatform;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;

public class J2meEmu extends GApplication implements XuiAppHolder {


    static J2meEmu mainApp;
    EmuForm gform;
    static String[] midlets = {"pipes-game.jar", "rayman-game.jar"};

    public static J2meEmu getInstance() {
        return mainApp;
    }

    public J2meEmu() {
        mainApp = this;
    }

    @Override
    public void onInit() {

        Glfm.glfmSetSupportedInterfaceOrientation(GCallBack.getInstance().getDisplay(), Glfm.GLFMInterfaceOrientationPortrait);
        Glfm.glfmSetDisplayChrome(GCallBack.getInstance().getDisplay(), Glfm.GLFMUserInterfaceChromeFullscreen);
        gform = new EmuForm(this);
        gform.addButtons();
        checkMidletHome();//检测midlet home目录在不在，不在就创建一个
        copyFiles();
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

    void copyFiles() {
        //复制res中的jars到midlethome中
        for (int i = 0; i < midlets.length; i++) {
            File f = new File(getMidlletHome() + "/" + midlets[i]);
            if (!f.exists()) {
                try {
                    byte[] data = GToolkit.readFileFromJar("/" + midlets[i]);
                    if (data != null) {
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(data);
                        fos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
                GCallBack.getInstance().getDeviceWidth() * .8f, GCallBack.getInstance().getDeviceHeight() * .8f,
                (gobj) -> {
                    String[] args = new String[2];
                    File f = gobj.getAttachment();
                    args[0] = "file:" + f.getAbsolutePath();
                    args[1] = getRmsRoot();
                    main(args);
                    gobj.flushNow();
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

    @Override
    public GApplication getApp() {
        return this;
    }

    @Override
    public GContainer getWebView() {
        return null;
    }
}


