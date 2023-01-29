package com.ebsee.emu;

import org.mini.gui.GButton;
import org.mini.gui.GForm;
import org.mini.gui.GPanel;
import org.mini.gui.GToolkit;

public class EmuForm extends GForm {
    J2meEmu app;
    final static float LCD_W = 240f, LCD_H = 320f;
    String[] NUM_TAG = {
            "1", "2 abc", "3 def",//
            "4 ghi", "5 jkl", "6 mno",//
            "7 pqrs", "8 tuv", "9 wxyz",//
            "* .", "0", "#-+",//
    };

    public EmuForm(GForm form, J2meEmu app) {
        super(form);
        this.app = app;

    }

    public void addButtons() {
        GForm form = getForm();
        float dw = (getW() - LCD_W) * .5f;
        float dh = dw * .6f;
        float dx = dw * 0.4f;
        float dy = (getH() - dh) * 0.5f;
        float butW = dw * 0.2f;
        float butH = butW;

        //navi
        GButton up = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(up);
        up.setPreIcon("\uE4AF");
        dx = dw * 0.2f;
        dy += butH;
        GButton left = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(left);
        left.setPreIcon("\uE4AD");
        dx = dw * 0.6f;
        GButton right = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(right);
        right.setPreIcon("\uE4AE");
        dx = dw * 0.4f;
        dy += butH;
        GButton down = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(down);
        down.setPreIcon("\uE4B0");

        //menu back
        butH = 30f;
        dx = dw - butW;
        dy = getH() - butH;
        GButton menu = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(menu);
        menu.setPreIcon("\uE005");
        dx = dw + LCD_W;
        dy = getH() - butH;
        GButton back = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(back);
        back.setPreIcon("\uE712");

        //num pad
        butW = dw * 0.24f;
        float spacingX = dw * 0.04f;
        float spacingY = getH() * 0.02f;
        dx = dw + LCD_W + dw * 0.1f;
        dy = getH() * 0.9f - (butH + spacingY) * 5;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 3; col++) {
                float tmpx = dx + col * (spacingX + butW);
                float tmpy = dy + row * (spacingY + butH);
                GButton num = new GButton(getForm(), NUM_TAG[row * 3 + col], tmpx, tmpy, butW, butH);
                form.add(num);

            }
        }

        //OK
        dx = dw + LCD_W + (dw - butW) * .5f;
        dy = getH() * 0.9f - butH;
        GButton ok = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(ok);
        ok.setPreIcon("●");
    }

    @Override
    public void keyEventGlfm(int key, int action, int mods) {
        super.keyEventGlfm(key, action, mods);
        GPanel panel = GToolkit.getComponent(getForm(), "LCD_FRAME");
        if (panel != null) {

        }
    }

    @Override
    public void keyEventGlfw(int key, int scanCode, int action, int mods) {
        super.keyEventGlfw(key, scanCode, action, mods);

    }

}
