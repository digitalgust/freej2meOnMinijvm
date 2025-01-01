package com.ebsee.emu;

import org.mini.apploader.AppLoader;
import org.mini.glfm.Glfm;
import org.mini.glfw.Glfw;
import org.mini.gui.*;
import org.mini.gui.event.GChildrenListener;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Properties;
import java.util.function.Predicate;

public class EmuForm extends GForm implements GChildrenListener {
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

    public void removeAllButtons() {
        GForm form = getForm();
        if (form != null) {
            form.getElements().removeIf(new Predicate<GObject>() {
                @Override
                public boolean test(GObject gObject) {
                    return gObject instanceof GButton;
                }
            });
        }
    }

    public void addButtons() {
        GForm form = getForm();

        GButton exitbut = new GButton(getForm(), "", 0, 30, 40f, 25f);
        form.add(exitbut);
        exitbut.setPreIcon("\uE741");
        exitbut.setActionListener(gObject -> {
            app.closeApp();
        });
        GButton open = new GButton(getForm(), "", 0, 60, 40f, 25f);
        form.add(open);
        open.setPreIcon("\uD83D\uDCC1");
        open.setActionListener(gObject -> {
            app.openFileChooser();
        });
        GButton orientation = new GButton(getForm(), "", getW() - 40, 30, 40f, 25f);
        form.add(orientation);
        orientation.setPreIcon("\uE717");
        orientation.setActionListener(gObject -> {
            if (Glfm.glfmGetSupportedInterfaceOrientation(GCallBack.getInstance().getDisplay()) == Glfm.GLFMInterfaceOrientationPortrait) {
                Glfm.glfmSetSupportedInterfaceOrientation(GCallBack.getInstance().getDisplay(), Glfm.GLFMInterfaceOrientationLandscapeLeft);
            } else {
                Glfm.glfmSetSupportedInterfaceOrientation(GCallBack.getInstance().getDisplay(), Glfm.GLFMInterfaceOrientationPortrait);
            }
            Glfm.glfmSetDisplayChrome(GCallBack.getInstance().getDisplay(), Glfm.GLFMUserInterfaceChromeFullscreen);
        });
        GButton esc = new GButton(getForm(), "", getW() - 40, 60, 40f, 25f);
        form.add(esc);
        esc.setPreIcon("\uE005");
        esc.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
        });

        //navi
        final float dw = 240f;
        float leftPos = getW() > getH() ? 10f : 2f;
        float butW = 48f;
        float butH = butW;
        float dx = leftPos + butW;
        float dy = getH() - dw;


        //menu back
        butH = 35f;
        dx = leftPos;
        GButton menu = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(menu);
        menu.setName("SOFT1");
        menu.setPreIcon("⚏");//
        menu.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_Q, 'q', KeyEvent.KEY_LOCATION_UNKNOWN);
        });
        dx = leftPos + butW * 2f;
        GButton back = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(back);
        menu.setName("SOFT2");
        back.setPreIcon("⇆");
        back.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_W, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
        });

        butH = butW;
        dx = leftPos + butW;
        dy += butH;
        GButton up = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(up);
        up.setPreIcon("\uE4AF");
        up.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
        });
        dx = leftPos;
        dy += butH;
        GButton left = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(left);
        left.setPreIcon("\uE4AD");
        left.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
        });
        //OK
        dx = leftPos + butW;
        GButton ok2 = new GButton(getForm(), "OK", dx + 2, dy + 2, butW - 4, butH - 4);
        form.add(ok2);
        ok2.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
        });
        dx = leftPos + butW * 2f;
        GButton right = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(right);
        right.setPreIcon("\uE4AE");
        right.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
        });
        dx = leftPos + butW;
        dy += butH;
        GButton down = new GButton(getForm(), "", dx, dy, butW, butH);
        form.add(down);
        down.setPreIcon("\uE4B0");
        down.setStateChangeListener(gObject -> {
            dispathKeyEvent(((GButton) gObject).isPressed(), KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
        });


        //num pad
        butW = dw * 0.24f;
        butH = 35f;
        float spacingX = getW() > getH() ? 6f : 2;
        float spacingY = 6f;
        dx = getW() - (butW + spacingX) * 3f;
        dy = getH() - dw;
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
        dx = getW() - (butW + spacingX) * 2f;
        dy = dy + 4 * (spacingY + butH);
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
    }

    @Override
    public void keyEventGlfw(int key, int scanCode, int action, int mods) {
        super.keyEventGlfw(key, scanCode, action, mods);
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

    GFrame findJ2meFrame(int x, int y) {
        GForm gForm = app.getForm();
        GObject go = gForm.getCurrent();
        if (go != null && go.getFrame() != null) {
            if ("J2ME_INPUT_FRAME".equals(go.getFrame().getName())) {
                return null;
            }
        }
        for (int i = gForm.getElements().size() - 1; i >= 0; i--) {
            GObject gobj = gForm.getElements().get(i);
            if (gobj instanceof GFrame) {
                GFrame curFrame = (GFrame) gobj;
                if ("J2ME_INPUT_FRAME".equals(curFrame.getName())) {
                    return null;
                }
                return curFrame;
            }
        }
//        GObject gobj = app.getForm().findByXY(x, y);
//        if (gobj != null) {
//            GFrame frame = gobj.getFrame();
//            if (frame != null && frame.getAttachment() instanceof Frame) {
//                curFrame = frame;
//                return frame;
//            }
//        }
//        if (curFrame == null) {
//            for (GObject go : gForm.getElements()) {
//                if (go instanceof GFrame) {
//                    if (go.getAttachment() instanceof Frame) {
//                        curFrame = (GFrame) go;
//                        return curFrame;
//                    }
//                }
//            }
//        }
        return null;
    }


    void dispathKeyEvent(boolean pressed, int keyCode, char ch, int location) {
        GFrame curFrame = findJ2meFrame(0, 0);
        AWTManager.iterAwtComponentAndProcess(curFrame, f -> f.getKeyListeners().forEach(keyListener -> {

            if (pressed) {
                KeyEvent keyEvent = new KeyEvent(f, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, ch, location);
                keyListener.keyPressed(keyEvent);
                //System.out.println("keyPressed " + keyCode);
            } else {
                KeyEvent keyEvent1 = new KeyEvent(f, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, keyCode, ch, location);
                keyListener.keyReleased(keyEvent1);
                //System.out.println("keyReleased " + keyCode);
            }
        }));
    }

    @Override
    public void mouseButtonEvent(int button, boolean pressed, int x, int y) {
        GFrame curFrame = findJ2meFrame(x, y);
        AWTManager.iterAwtComponentAndProcess(curFrame, f -> f.getMouseListeners().forEach(mouseListener -> {
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
        super.mouseButtonEvent(button, pressed, x, y);
    }

    @Override
    public void touchEvent(int touchid, int phase, int x, int y) {
        GFrame curFrame = findJ2meFrame(x, y);
        AWTManager.iterAwtComponentAndProcess(curFrame, f -> f.getMouseListeners().forEach(mouseListener -> {
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
        super.touchEvent(touchid, phase, x, y);
    }

    @Override
    public void onChildAdd(GObject child) {
        if (child instanceof GFrame) {
            GFrame frame = (GFrame) child;
            frame.setLocationChangeListener((oldLeft, oldTop, newLeft, newTop) -> {
//                if (getW() > getH()) {//横向
//                    child.setLocation(getW() / 2 - child.getW() / 2, 0);
//                } else {
//                    child.setLocation(getW() / 2 - child.getW() / 2, 0);
//                }
                if (child.getAttachment() instanceof Frame) {
                    app.setProperty("frame.location.x", String.format("%f", child.getLocationLeft()));
                    app.setProperty("frame.location.y", String.format("%f", child.getLocationTop()));
                }
            });

            try {
                if (child.getAttachment() instanceof Frame) {
                    frame.setLocation(Float.parseFloat(app.getProperty("frame.location.x", "0")), Float.parseFloat(app.getProperty("frame.location.y", "0")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onChildRemove(GObject child) {

    }

    @Override
    public boolean dragEvent(int button, float dx, float dy, float x, float y) {
        GFrame curFrame = findJ2meFrame((int) x, (int) y);
        if (curFrame != null && curFrame.getAttachment() instanceof Frame) {
            AWTManager.iterAwtComponentAndProcess(curFrame, f -> f.getMouseMotionListeners().forEach(mouseListener -> {
                if (f.getPeer().isInArea(x, y)) {
                    MouseEvent mouseEvent = new MouseEvent(f, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 0,
                            (int) x, (int) y, (int) x, (int) y,
                            1, false, MouseEvent.BUTTON1);
                    mouseListener.mouseDragged(mouseEvent);
                }
            }));
        }
        return super.dragEvent(button, dx, dy, x, y);
    }

    public GFrame getCurFrame() {
        return findJ2meFrame(0, 0);
    }


}
