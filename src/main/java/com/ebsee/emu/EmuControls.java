package com.ebsee.emu;

import org.mini.glfm.Glfm;
import org.mini.gui.*;
import org.mini.gui.callback.GCallBack;
import org.mini.hmi.widget.Joystick;
import org.mini.hmi.widget.JoystickListener;
import org.mini.hmi.widget.WButton;
import org.mini.hmi.widget.Widget;
import org.mini.hmi.widget.WidgetContainer;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** Touch ownership is independent of xgui's single current/focused component. */
class EmuControls extends GPanel {
    private final EmuForm emuForm;
    private final WidgetContainer widgets;
    private final Map<Integer, Widget> captures = new HashMap<>();
    private final Map<Integer, HeldKey> heldKeys = new HashMap<>();
    private final DirectionStick joystick;
    private int directionMask;
    private static final int RIGHT = 1, UP = 2, LEFT = 4, DOWN = 8;
    private static final int[] DIRECTION_BITS = {RIGHT, UP, LEFT, DOWN};
    private static final int[] DIRECTION_KEYS = {
            KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN
    };
    private static final int[] SECTORS = {RIGHT, RIGHT | UP, UP, UP | LEFT,
            LEFT, LEFT | DOWN, DOWN, DOWN | RIGHT};

    private static class HeldKey {
        GFrame frame;
        char character;
        int location;
        int count = 1;
    }

    EmuControls(EmuForm form) {
        super(form, 0, 0, form.getW(), form.getH());
        emuForm = form;
        setName("J2ME_CONTROLS");
        widgets = new WidgetContainer(null, 0, 0, getW(), getH()) {
            @Override
            public float getX() {
                return EmuControls.this.getX();
            }

            @Override
            public float getY() {
                return EmuControls.this.getY();
            }
        };

        double[] inset = new double[4];
        Glfm.glfmGetDisplayChromeInsets(GCallBack.getInstance().getDisplay(), inset);
        float ratio = GCallBack.getInstance().getDeviceRatio();
        float leftSpace = (float) inset[3] / ratio;
        float rightSpace = (float) inset[1] / ratio;
        addAction("", "\uE741", leftSpace, 30, 40, 25, () -> emuForm.app.closeApp());
        addAction("", "\uD83D\uDCC1", leftSpace, 60, 40, 25, () -> {
            resetInput();
            emuForm.app.openFileChooser();
        });
        addAction("", "\uE717", getW() - 40 - rightSpace, 30, 40, 25, () -> {
            resetInput();
            long display = GCallBack.getInstance().getDisplay();
            int orientation = Glfm.glfmGetSupportedInterfaceOrientation(display);
            Glfm.glfmSetSupportedInterfaceOrientation(display,
                    orientation == Glfm.GLFMInterfaceOrientationPortrait
                            ? Glfm.GLFMInterfaceOrientationLandscapeLeft : Glfm.GLFMInterfaceOrientationPortrait);
            Glfm.glfmSetDisplayChrome(display, Glfm.GLFMUserInterfaceChromeFullscreen);
        });
        addKey("", "☰", getW() - 40 - rightSpace, 60, 40, 25,
                KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN, false);

        float leftPos = (getW() > getH() ? 10 : 2) + leftSpace;
        float top = getH() - 240;
        addKey("", "⚏", leftPos, top, 48, 35,
                KeyEvent.VK_Q, 'q', KeyEvent.KEY_LOCATION_UNKNOWN, false);
        addKey("", "⇆", leftPos + 96, top, 48, 35,
                KeyEvent.VK_W, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN, false);

        // Use the same slot/stick images and rendering as GamePanel's joystick.
        joystick = new DirectionStick(leftPos, top + 48, 144);
        joystick.setJoystickListener(this::onJoystickMove);
        widgets.add(joystick);

        float buttonW = 240 * .24f;
        float buttonH = 35;
        float spacingX = getW() > getH() ? 6 : 2;
        float numberX = getW() - (buttonW + spacingX) * 3 - rightSpace;
        for (int i = 0; i < form.NUM_TAG.length; i++) {
            addKey(form.NUM_TAG[i], null, numberX + (i % 3) * (buttonW + spacingX),
                    top + (i / 3) * 41, buttonW, 35, form.NUM_KEYCODE[i], form.NUM_CHAR[i],
                    KeyEvent.KEY_LOCATION_NUMPAD, true);
        }
        float actionY = top + 4 * 41;
        // Keep the extra soft keys beside the joystick and under '*', without overlapping each other.
        float leftSoftX = Math.min(leftPos + joystick.getW() + spacingX, numberX - buttonW - spacingX);
        addKey("", "⚏", leftSoftX, actionY, buttonW, buttonH,
                KeyEvent.VK_Q, 'q', KeyEvent.KEY_LOCATION_UNKNOWN, false);
        addKey("", "⇆", numberX, actionY, buttonW, buttonH,
                KeyEvent.VK_W, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN, false);
        addKey("OK", null, getW() - (buttonW + spacingX) * 2 - rightSpace,
                actionY, buttonW, buttonH, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED,
                KeyEvent.KEY_LOCATION_UNKNOWN, false);
    }

    private void addAction(String label, String icon, float x, float y, float w, float h, Runnable action) {
        KeyButton button = new KeyButton(label, icon, x, y, w, h, false);
        button.clickAction = action;
        widgets.add(button);
    }

    private void onJoystickMove(int operation, float x, float y, float ignoredDegree) {
        if (operation == JoystickListener.OPERATION_RELEASE) {
            setDirections(0);
        } else {
            float dx = x - joystick.getX() - joystick.getW() * .5f;
            float dy = y - joystick.getY() - joystick.getH() * .5f;
            float deadZone = joystick.getW() * .12f;
            // Older g3d jars keep the previous angle on the vertical center line.
            float degree = (float) Math.toDegrees(Math.atan2(-dy, dx));
            int sector = ((int) Math.floor((degree + 360f + 22.5f) / 45f)) % 8;
            setDirections(dx * dx + dy * dy < deadZone * deadZone ? 0 : SECTORS[sector]);
        }
    }

    private void addKey(String label, String icon, float x, float y, float w, float h,
                        int code, char character, int location, boolean number) {
        KeyButton button = new KeyButton(label, icon, x, y, w, h, number);
        button.setListener(widget -> setKey(true, code, character, location));
        button.releaseAction = () -> setKey(false, code, character, location);
        widgets.add(button);
    }

    private void setKey(boolean pressed, int code, char character, int location) {
        HeldKey held = heldKeys.get(code);
        if (pressed) {
            if (held != null) {
                held.count++;
                return;
            }
            GFrame frame = emuForm.findJ2meFrame(0, 0);
            if (frame == null) return;
            held = new HeldKey();
            held.frame = frame;
            held.character = character;
            held.location = location;
            heldKeys.put(code, held);
            emuForm.dispatchKeyEvent(frame, true, code, character, location);
        } else if (held != null && --held.count == 0) {
            heldKeys.remove(code);
            // Release on the original game even if a dialog has since taken focus.
            emuForm.dispatchKeyEvent(held.frame, false, code, held.character, held.location);
        }
    }

    private void setDirections(int mask) {
        int previous = directionMask;
        directionMask = mask;
        for (int i = 0; i < DIRECTION_BITS.length; i++) {
            if ((previous & DIRECTION_BITS[i]) != 0 && (mask & DIRECTION_BITS[i]) == 0) {
                setKey(false, DIRECTION_KEYS[i], KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
                if (directionMask != mask) return;
            }
        }
        for (int i = 0; i < DIRECTION_BITS.length; i++) {
            if ((previous & DIRECTION_BITS[i]) == 0 && (mask & DIRECTION_BITS[i]) != 0) {
                setKey(true, DIRECTION_KEYS[i], KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN);
                if (directionMask != mask) return;
            }
        }
    }

    boolean pointerEvent(int id, int phase, int x, int y) {
        Widget owner = captures.get(id);
        if (phase == Glfm.GLFMTouchPhaseBegan) {
            if (owner != null) owner.touchEvent(id, Glfm.GLFMTouchPhaseCancelled, x, y);
            captures.remove(id);
            for (Widget widget : new ArrayList<>(widgets.getWidgets())) {
                if (widget.isEnable() && widget.isInArea(x, y)) {
                    captures.put(id, widget);
                    widget.touchEvent(id, phase, x, y);
                    flushNow();
                    return true;
                }
            }
            return false;
        }
        if (owner == null) return false;
        if (phase == Glfm.GLFMTouchPhaseEnded || phase == Glfm.GLFMTouchPhaseCancelled) captures.remove(id);
        owner.touchEvent(id, phase, x, y);
        flushNow();
        return true;
    }

    void resetInput() {
        captures.clear();
        for (Widget widget : widgets.getWidgets()) {
            if (widget instanceof KeyButton) ((KeyButton) widget).reset();
        }
        joystick.reset();
        setDirections(0);
        // Also release anything held when a control callback changed the UI.
        for (Integer code : new ArrayList<>(heldKeys.keySet())) {
            HeldKey held = heldKeys.remove(code);
            emuForm.dispatchKeyEvent(held.frame, false, code, held.character, held.location);
        }
        flushNow();
    }

    @Override
    public boolean isInArea(float x, float y) {
        if (widgets == null) return false;
        for (Widget widget : widgets.getWidgets()) {
            if (widget.isEnable() && widget.isInArea(x, y)) return true;
        }
        return false;
    }

    @Override
    public boolean paint(long vg) {
        widgets.paint(vg);
        return true;
    }

    /** WButton input with the existing GButton renderer, without a focused GButton child. */
    private class KeyButton extends WButton {
        private static final int NO_POINTER = Integer.MIN_VALUE;
        private int pointer = NO_POINTER;
        private final GButton skin;
        private Runnable releaseAction, clickAction;

        KeyButton(String label, String icon, float x, float y, float w, float h, boolean number) {
            super(null, null, x, y, w, h);
            skin = new GButton(emuForm, label, x, y, w, h) {
                @Override
                public boolean paint(long vg) {
                    touched = pointer != NO_POINTER;
                    return super.paint(vg);
                }
            };
            if (icon != null) skin.setPreIcon(icon);
            if (number) skin.setFontSize(14);
        }

        @Override
        public boolean paint(long vg) {
            skin.setLocation(getX(), getY());
            return skin.paint(vg);
        }

        @Override
        public boolean touchEvent(int id, int phase, int x, int y) {
            if (phase == Glfm.GLFMTouchPhaseBegan && pointer == NO_POINTER && isInArea(x, y)) {
                pointer = id;
                doAction();
                return true;
            }
            if (id != pointer) return false;
            if (phase == Glfm.GLFMTouchPhaseEnded || phase == Glfm.GLFMTouchPhaseCancelled) {
                boolean click = phase == Glfm.GLFMTouchPhaseEnded && isInArea(x, y);
                reset();
                if (click && clickAction != null) clickAction.run();
            }
            return true;
        }

        void reset() {
            boolean held = pointer != NO_POINTER;
            pointer = NO_POINTER;
            if (held && releaseAction != null) releaseAction.run();
        }
    }

    private class DirectionStick extends Joystick {
        private int pointer = Integer.MIN_VALUE;

        DirectionStick(float x, float y, float size) {
            super("/res/controls/yellowball.png", "/res/controls/redball.png", x, y, size, size);
        }

        @Override
        public boolean isInArea(float x, float y) {
            if (!super.isInArea(x, y)) return false;
            // On narrow screens a soft key can cover the joystick's transparent padding.
            for (Widget widget : widgets.getWidgets()) {
                if (widget instanceof KeyButton && widget.isEnable() && widget.isInArea(x, y)) return false;
            }
            return true;
        }

        @Override
        public boolean touchEvent(int id, int phase, int x, int y) {
            if (phase == Glfm.GLFMTouchPhaseBegan) {
                if (pointer != Integer.MIN_VALUE) return false;
                pointer = id;
                return super.touchEvent(id, phase, x, y);
            }
            if (id != pointer) return false;
            if (phase == Glfm.GLFMTouchPhaseMoved) {
                // Some callbacks send only touch-moved, others also send dragEvent.
                return super.dragEvent(id, 0, 0, x, y);
            }
            if (phase == Glfm.GLFMTouchPhaseEnded || phase == Glfm.GLFMTouchPhaseCancelled) {
                reset();
            }
            return true;
        }

        @Override
        public void reset() {
            pointer = Integer.MIN_VALUE;
            super.reset();
            setDirections(0);
        }
    }
}
