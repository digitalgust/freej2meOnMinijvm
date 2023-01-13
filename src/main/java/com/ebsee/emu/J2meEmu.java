package com.ebsee.emu;

import org.mini.apploader.GApplication;
import org.mini.glfm.Glfm;
import org.mini.gui.GCallBack;
import org.mini.gui.GForm;
import org.recompile.freej2me.FreeJ2ME;

public class J2meEmu extends GApplication {


    static J2meEmu mainApp;
    GForm gform;

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
            gform=new GForm(null);
            main(null);
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


