package com.ebsee.emu;

import org.mini.apploader.GApplication;
import org.mini.glfm.Glfm;
import org.mini.gui.*;
import org.recompile.freej2me.FreeJ2ME;

import java.io.File;
import java.io.FileFilter;

public class J2meEmu extends GApplication {


    static J2meEmu mainApp;
    EmuForm gform;
    Thread thread;

    public static J2meEmu getInstance() {
        return mainApp;
    }

    public J2meEmu() {
        mainApp = this;
    }

    @Override
    public GForm getForm() {
        if (gform == null) {
            Glfm.glfmSetSupportedInterfaceOrientation(GCallBack.getInstance().getDisplay(), Glfm.GLFMInterfaceOrientationLandscapeLeft);
            Glfm.glfmSetDisplayChrome(GCallBack.getInstance().getDisplay(), Glfm.GLFMUserInterfaceChromeFullscreen);
            gform = new EmuForm(null, this);
            gform.addButtons();

            GFrame chooser = GToolkit.getFileChooser(gform, "Select a j2me midlet jar", null, new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(".jar") || file.isDirectory();
                }
            }, gform.getDeviceWidth(), gform.getDeviceHeight(), (gobj) -> {
                String[] args = new String[1];
                File f = gobj.getAttachment();
                args[0] = "file:" + f.getAbsolutePath();
                main(args);
            }, null);
            gform.add(chooser);
            GButton exitbut = new GButton(gform, GLanguage.getString("Exit"), 20, 0, 60f, 25f);
            gform.add(exitbut);
            exitbut.setActionListener(gObject -> {
                closeApp();
            });
        }
        return gform;
    }

    @Override
    public void onClose() {
    }

    public static void main(String args[]) {
        FreeJ2ME app = new FreeJ2ME(args);
    }

}


