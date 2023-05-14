package com.ebsee.emu;

import org.mini.glfm.Glfm;
import org.mini.glfw.Glfw;
import org.mini.gui.*;
import org.mini.gui.event.GActionListener;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class EmuForm extends GForm {
    J2meEmu app;
    final static float LCD_W = 240f, LCD_H = 320f;
    String[] NUM_TAG = {
            "1", "abc 2", "def 3",//
            "ghi 4", "jkl 5", "mno 6",//
            "pqrs 7", "tuv 8", "wxyz 9",//
            "* .", "0", "#-+",//
    };
    int[] NUM_KEYCODE = {
            KeyEvent.VK_NUMPAD7, KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD9,//
            KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6,//
            KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD3,//
            KeyEvent.VK_ASTERISK, KeyEvent.VK_NUMPAD0, KeyEvent.VK_NUMBER_SIGN,//
    };

    char[] NUM_CHAR = {
            '7', '8', '9',//
            '4', '5', '6',//
            '1', '2', '3',//
            '*', '0', '#',//
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

        GButton exitbut = new GButton(getForm(), GLanguage.getString("Exit"), 20, 0, 60f, 25f);
        form.add(exitbut);
        exitbut.setActionListener(gObject -> {
            app.closeApp();
        });
        GButton esc = new GButton(getForm(), "", 90f, 0, 60f, 25f);
        form.add(esc);
        esc.setPreIcon("\uE005");
        esc.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
        });

        //navi
        GButton up = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(up);
        up.setPreIcon("\uE4AF");
        up.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
        });
        dx = dw * 0.2f;
        dy += butH;
        GButton left = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(left);
        left.setPreIcon("\uE4AD");
        left.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
        });
        dx = dw * 0.6f;
        GButton right = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(right);
        right.setPreIcon("\uE4AE");
        right.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
        });
        dx = dw * 0.4f;
        dy += butH;
        GButton down = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(down);
        down.setPreIcon("\uE4B0");
        down.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
        });
        //menu back
        butH = 30f;
        dx = dw - butW;
        dy = getH() - butH;
        GButton menu = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(menu);
        menu.setPreIcon("●");//
        menu.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_Q, 'q', KeyEvent.KEY_LOCATION_UNKNOWN);
        });
        dx = dw + LCD_W;
        dy = getH() - butH;
        GButton back = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(back);
        back.setPreIcon("\uE712");
        back.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_W, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
        });

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
                num.setFontSize(14f);
                num.setAttachment(Integer.valueOf(row * 3 + col));
                form.add(num);
                num.setStateChangeListener(gObject -> {
                    Integer ai = gObject.getAttachment();
                    dispathKeyEvent(((GButton) gObject).isPressed(), NUM_KEYCODE[ai.intValue()], NUM_CHAR[ai.intValue()], KeyEvent.KEY_LOCATION_NUMPAD);
                });
            }
        }

        //OK
        dx = dw + LCD_W + (dw - butW) * .5f;
        dy = getH() * 0.9f - butH;
        GButton ok = new GButton(getForm(), "OK", dx, dy, butW, butH);
        form.add(ok);
        //ok.setPreIcon("OK");
        ok.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
        });
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
        GPanel panel = GToolkit.getComponent(getForm(), "FreeJ2ME");
        if (panel != null) {

        }
        switch (key) {
            case Glfw.GLFW_KEY_A:
            case Glfw.GLFW_KEY_LEFT: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
                }
                break;
            }
            case Glfw.GLFW_KEY_D:
            case Glfw.GLFW_KEY_RIGHT: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
                }
                break;
            }
            case Glfw.GLFW_KEY_W:
            case Glfw.GLFW_KEY_UP: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
                }
                break;
            }
            case Glfw.GLFW_KEY_S:
            case Glfw.GLFW_KEY_DOWN: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
                }
                break;
            }
            case Glfw.GLFW_KEY_KP_ENTER:
            case Glfw.GLFW_KEY_ENTER: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
                }
                break;
            }
            case Glfw.GLFW_KEY_KP_0: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_NUMPAD0, '0', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_KP_1: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_NUMPAD7, '7', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_KP_2: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_NUMPAD8, '8', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_KP_3: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_NUMPAD9, '9', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_KP_4: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_NUMPAD4, '4', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_KP_5: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_NUMPAD5, '5', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_KP_6: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_NUMPAD6, '6', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_KP_7: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_NUMPAD1, '1', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_KP_8: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_NUMPAD2, '2', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_KP_9: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_NUMPAD3, '3', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_KP_MULTIPLY: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_ASTERISK, '*', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_KP_DIVIDE: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_NUMBER_SIGN, '#', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_F1: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_Q, 'q', KeyEvent.KEY_LOCATION_UNKNOWN);
                }
                break;
            }
            case Glfw.GLFW_KEY_F2: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_W, 'w', KeyEvent.KEY_LOCATION_UNKNOWN);
                }
                break;
            }
        }

    }

    Frame findJ2meFrame() {
        GPanel panel = GToolkit.getComponent(getForm(), "FreeJ2ME");
        return panel.getAttachment();
    }


    void dispathKeyEvent(boolean pressed, int keyCode, char ch, int location) {
        AWTManager.iterAwtComponentAndProcess(f -> f.getKeyListeners().forEach(keyListener -> {
            if (pressed) {
                KeyEvent keyEvent = new KeyEvent(f, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, ch, location);
                keyListener.keyPressed(keyEvent);
            } else {
                KeyEvent keyEvent1 = new KeyEvent(f, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, keyCode, ch, location);
                keyListener.keyReleased(keyEvent1);
            }
        }));
    }

    @Override
    public void mouseButtonEvent(int button, boolean pressed, int x, int y) {
        super.mouseButtonEvent(button, pressed, x, y);
        AWTManager.iterAwtComponentAndProcess(f -> f.getMouseListeners().forEach(mouseListener -> {
            if (pressed) {
                if (f.getPeer().isInArea(x, y)) {
                    MouseEvent mouseEvent = new MouseEvent(f, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0,
                            (int) (x - f.getPeer().getX()), (int) (y - f.getPeer().getY()), x, y,
                            1, false, MouseEvent.BUTTON1 + (button - Glfw.GLFW_MOUSE_BUTTON_1));
                    mouseListener.mousePressed(mouseEvent);
                }
            } else {
                if (f.getPeer().isInArea(x, y)) {
                    MouseEvent mouseEvent = new MouseEvent(f, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0,
                            (int) (x - f.getPeer().getX()), (int) (y - f.getPeer().getY()), x, y,
                            1, false, MouseEvent.BUTTON1 + (button - Glfw.GLFW_MOUSE_BUTTON_1));
                    mouseListener.mouseReleased(mouseEvent);
                }
            }
        }));
    }

    @Override
    public void touchEvent(int touchid, int phase, int x, int y) {
        super.touchEvent(touchid, phase, x, y);
        AWTManager.iterAwtComponentAndProcess(f -> f.getMouseListeners().forEach(mouseListener -> {
            if (phase == Glfm.GLFMTouchPhaseBegan) {
                if (f.getPeer().isInArea(x, y)) {
                    MouseEvent mouseEvent = new MouseEvent(f, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0,
                            (int) (x - f.getPeer().getX()), (int) (y - f.getPeer().getY()), x, y,
                            1, false, MouseEvent.BUTTON1);
                    mouseListener.mousePressed(mouseEvent);
                }
            } else if (phase == Glfm.GLFMTouchPhaseEnded) {
                if (f.getPeer().isInArea(x, y)) {
                    MouseEvent mouseEvent = new MouseEvent(f, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0,
                            (int) (x - f.getPeer().getX()), (int) (y - f.getPeer().getY()), x, y,
                            1, false, MouseEvent.BUTTON1);
                    mouseListener.mouseReleased(mouseEvent);
                }
            }
        }));
    }
}
