package com.ebsee.emu;

import org.mini.apploader.GApplication;
import org.mini.glfm.Glfm;
import org.mini.gui.*;
import org.mini.gui.callback.GCallBack;
import org.mini.gui.event.GSizeChangeListener;
import org.mini.layout.XContainer;
import org.mini.layout.XEventHandler;
import org.mini.layout.XObject;
import org.mini.layout.loader.XuiAppHolder;
import org.mini.layout.loader.XuiLoader;
import org.mini.layout.loader.XmlExtAssist;
import org.recompile.freej2me.FreeJ2ME;
import org.recompile.mobile.Mobile;
import org.recompile.mobile.MobilePlatform;
import org.minijvm.md2xmlui.MdConfig;
import org.minijvm.md2xmlui.browser.MdBrowser;
import org.minijvm.md2xmlui.browser.MdBrowserListener;
import org.minijvm.md2xmlui.browser.DownloadTask;
import javax.sound.midi.MidiSystem;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class J2meEmu extends GApplication implements XuiAppHolder {


    static J2meEmu mainApp;
    EmuForm gform;
    XuiLoader xuiLoader;
    private MdBrowser browser;
    private GFrame browserFrame;
    private static final String BROWSER_HOME = "https://p.ysbit.cn/static/webj2me.html";
    static String[] midlets = {"pipes-game.jar"};

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
        openAppSelectFrame();
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
        openBrowser();
    }

    void openBrowser() {
        if (browserFrame != null && browserFrame.getParent() != null) {
            GToolkit.showFrame(browserFrame);
            return;
        }
        MdConfig config = MdConfig.fromApplication(this);
        config.setDownloadDirectory(new File(getMidlletHome()));
        if (config.getApiKey().length() == 0) {
            File keyFile = new File(getSaveRoot(), "webclaw.key");
            if (keyFile.isFile()) {
                Properties settings = new Properties();
                try (FileInputStream input = new FileInputStream(keyFile)) {
                    settings.load(input);
                    config.setApiKey(settings.getProperty("webclaw.key", ""));
                } catch (IOException e) {
                    System.err.println("Unable to read local Reader key configuration.");
                }
            }
        }
        if (config.getApiKey().length() == 0) {
            try (InputStream input = getClass().getResourceAsStream("/reader-auth.properties")) {
                if (input != null) {
                    Properties settings = new Properties();
                    settings.load(input);
                    config.setApiKey(settings.getProperty("webclaw.key", ""));
                }
            } catch (IOException e) {
                System.err.println("Unable to read bundled Reader key configuration.");
            }
        }
        browserFrame = MdBrowser.createFrame(this, config);
        browserFrame.setBgColor(226, 235, 244, 255);
        browser = MdBrowser.getBrowser(browserFrame);
        browser.getWebView().setBgColor(244, 248, 252, 255);
        GObject statusBar = GToolkit.getComponent(browserFrame, "MD_STATUS_BAR");
        if (statusBar != null) statusBar.setBgColor(226, 235, 244, 255);
        browser.setListener(new MdBrowserListener() {
            @Override
            public void onDownloadCompleted(MdBrowser browser, DownloadTask task) {
                File file = task.targetFile;
                if (file != null && file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                    refreshAppSelect();
                    GObject appSelectFrame = GToolkit.getComponent(gform, "J2ME_APP_SELECT_FRAME");
                    if (appSelectFrame != null) {
                        GToolkit.showFrame(appSelectFrame,
                                appSelectFrame.getLocationLeft(), appSelectFrame.getLocationTop());
                        gform.flushNow();
                    }
                }
            }
        });
        GToolkit.showFrame(browserFrame);
        browser.open(BROWSER_HOME);
    }

    void copyFiles() {
        //复制res中的jars到midlethome中
        for (int i = 0; i < midlets.length; i++) {
            if ("true".equals(getProperty("midlet.deleted." + midlets[i], ""))) continue;
            File f = new File(getMidlletHome() + "/" + midlets[i]);
            //每次复制
            //if (!f.exists()) {
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
            //}
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

    /**
     * 打开 xml 描述的应用选择器（/ui/appselect.xml + J2meScriptLib 脚本函数库），
     * 替代启动时的文件选取框；解析失败回退旧文件选取框
     */
    public void openAppSelectFrame() {
        try {
            XEventHandler eventHandler = new XEventHandler();
            xuiLoader = new XuiLoader(this, eventHandler, new J2meScriptLib());
            XmlExtAssist assist = xuiLoader.getXmlExtAssist();
            // doAction 的守卫要求 actionListener 非 null 才执行 onClinkScript，
            // XML onclick 组件经 XObject 创建时会挂 assist.getEventHandler()，
            // 必须提供非 null 的默认 handler，否则所有 onclick 都不触发
            assist.setEventHandler(eventHandler);
            String xmlStr = GToolkit.readFileFromJarAsString("/ui/appselect.xml", "utf-8");
            XObject xobj = XContainer.parseXml(xmlStr, assist);
            if (xobj instanceof XContainer) {
                ((XContainer) xobj).build((int) gform.getW(), (int) gform.getH(), null);
            }
            GToolkit.showFrame(xobj.getGui(),(gform.getW()-xobj.getGui().getW())/2,10);
            // oninit 在 build 过程中触发，此时 frame 尚未挂到 form，
            // GuiScriptLib 的 setText/setEnable 等找不到组件，挂上后补一次首屏填充
            refreshAppSelect();
        } catch (Exception e) {
            e.printStackTrace();
            openFileChooser();
        }
    }

    void refreshAppSelect() {
        GObject tableGui = GToolkit.getComponent(gform, "PAN_APP_SELECT");
        if (tableGui != null && tableGui.getInterpreter() != null) {
            tableGui.getInterpreter().callSub("refreshlist()");
            gform.flushNow();
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
        MidiSystem.shutdownAll();
        if (browser != null) browser.close();
        if (gform != null) gform.resetVirtualInput();
    }

    @Override
    public void onPause() {
        if (gform != null) gform.resetVirtualInput();
    }

    public static void main(String args[]) {
        MidiSystem.beginApplication();
        String[] launchArgs = args == null ? new String[0] : args.clone();
        FreeJ2ME.openOrFocus(launchArgs);
    }

    @Override
    public GApplication getApp() {
        return this;
    }

    @Override
    public GContainer getWebView() {
        return browser == null ? null : browser.getWebView();
    }
}


