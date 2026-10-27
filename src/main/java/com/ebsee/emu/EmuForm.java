package com.ebsee.emu;

import org.mini.glfm.Glfm;
import org.mini.glfw.Glfw;
import org.mini.gui.*;
import org.mini.gui.event.GChildrenListener;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

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
            KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD3,//
            KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6,//
            KeyEvent.VK_NUMPAD7, KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD9,//
            KeyEvent.VK_ASTERISK, KeyEvent.VK_NUMPAD0, KeyEvent.VK_NUMBER_SIGN,//
    };

    char[] NUM_CHAR = {
            '1', '2', '3',//
            '4', '5', '6',//
            '7', '8', '9',//
            '*', '0', '#',//
    };

    public EmuForm(J2meEmu app) {
        super(app);
        this.app = app;

    }

    private EmuControls controls;
    private final Set<Integer> controlPointers = new HashSet<>();

    public void removeAllButtons() {
        if (controls != null) {
            controls.resetInput();
            remove(controls);
            controls = null;
        }
    }

    public void addButtons() {
        removeAllButtons();
        controls = new EmuControls(this);
        add(0, controls);
    }

    public void resetVirtualInput() {
        if (controls != null) controls.resetInput();
    }

    private boolean routeControlPointer(int id, int phase, int x, int y) {
        if (phase == Glfm.GLFMTouchPhaseBegan) {
            controlPointers.remove(id);
            if (controls == null || findSonByXY(x, y) != controls) return false;
            // Register before callbacks: a key press may open a dialog and reset controls.
            controlPointers.add(id);
            if (controls.pointerEvent(id, phase, x, y)) return true;
            controlPointers.remove(id);
            return false;
        }
        if (!controlPointers.contains(id)) return false;
        if (phase == Glfm.GLFMTouchPhaseEnded || phase == Glfm.GLFMTouchPhaseCancelled) {
            controlPointers.remove(id);
        }
        if (controls != null) controls.pointerEvent(id, phase, x, y);
        // After resize/reset, swallow this gesture's remaining events until release.
        return true;
    }

    @Override
    public void onAppFocus(boolean focus) {
        if (!focus) resetVirtualInput();
        super.onAppFocus(focus);
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
            case Glfw.GLFW_KEY_0:
            case Glfw.GLFW_KEY_KP_0: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_0, '0', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_1:
            case Glfw.GLFW_KEY_KP_1: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_1, '1', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_2:
            case Glfw.GLFW_KEY_KP_2: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_2, '2', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_3:
            case Glfw.GLFW_KEY_KP_3: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_3, '3', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_4:
            case Glfw.GLFW_KEY_KP_4: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_4, '4', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_5:
            case Glfw.GLFW_KEY_KP_5: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_5, '5', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_6:
            case Glfw.GLFW_KEY_KP_6: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_6, '6', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_7:
            case Glfw.GLFW_KEY_KP_7: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_7, '7', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_8:
            case Glfw.GLFW_KEY_KP_8: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_8, '8', KeyEvent.KEY_LOCATION_NUMPAD);
                }
                break;
            }
            case Glfw.GLFW_KEY_9:
            case Glfw.GLFW_KEY_KP_9: {
                if (action == Glfw.GLFW_PRESS || action == Glfw.GLFW_RELEASE) {
                    dispathKeyEvent(action == Glfw.GLFW_PRESS, KeyEvent.VK_9, '9', KeyEvent.KEY_LOCATION_NUMPAD);
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
        return null;
    }


    void dispathKeyEvent(boolean pressed, int keyCode, char ch, int location) {
        GFrame curFrame = findJ2meFrame(0, 0);
        dispatchKeyEvent(curFrame, pressed, keyCode, ch, location);
    }

    void dispatchKeyEvent(GFrame curFrame, boolean pressed, int keyCode, char ch, int location) {
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
        int pointer = -button - 2;
        if (routeControlPointer(pointer, pressed ? Glfm.GLFMTouchPhaseBegan : Glfm.GLFMTouchPhaseEnded, x, y)) return;
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
        if (routeControlPointer(touchid, phase, x, y)) return;
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
            resetVirtualInput();
            GFrame frame = (GFrame) child;
            frame.setLocationChangeListener((oldLeft, oldTop, newLeft, newTop) -> {
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
        child.flushNow();
    }

    @Override
    public void onChildRemove(GObject child) {
        if (child instanceof GFrame) resetVirtualInput();
    }

    @Override
    public boolean dragEvent(int button, float dx, float dy, float x, float y) {
        // GLFM uses touch IDs here; GLFW uses mouse button IDs.
        int pointer = controlPointers.contains(button) ? button : -button - 2;
        if (routeControlPointer(pointer, Glfm.GLFMTouchPhaseMoved, (int) x, (int) y)) {
            return true;
        }
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
