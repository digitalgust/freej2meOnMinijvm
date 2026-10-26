package com.ebsee.emu;

import org.mini.gui.GContainer;
import org.mini.gui.GForm;
import org.mini.gui.GImage;
import org.mini.gui.GList;
import org.mini.gui.GListItem;
import org.mini.gui.GObject;
import org.mini.gui.GToolkit;
import org.mini.gui.event.GActionListener;
import org.mini.gui.gscript.DataType;
import org.mini.gui.gscript.Interpreter;
import org.mini.gui.gscript.Lib;
import org.mini.gui.gscript.Obj;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * J2ME 应用选择器脚本函数库：扫描 midletapp 目录、解析 jar 内 MANIFEST 的
 * 应用名与 icon、启动 midlet、GList 动态列表项。供 /ui/appselect.xml 脚本调用。
 * 写法同 org.mini.layout.guilib.GuiScriptLib，
 * 由 J2meEmu.openAppSelectFrame 经 XmlExtAssist.addExtScriptLib(new J2meScriptLib()) 注册。
 * 注意：不用 Arrays.sort(Comparator) 与 String.compareToIgnoreCase，
 * 两者在 miniJVM 运行时抛 NoSuchMethodError（排序用冒泡+toLowerCase 内联）。
 */
public class J2meScriptLib extends Lib {

    static class MidletInfo {
        File jarFile;
        String appName;   // MANIFEST MIDlet-Name，解析不到回退 jar 文件名
        String iconEntry; // jar 内 icon 条目名，可为 null
        GImage icon;      // 懒加载缓存，失败为 null
        boolean iconLoaded;

        MidletInfo(File jarFile) {
            this.jarFile = jarFile;
            this.appName = jarFile.getName();
        }
    }

    List<MidletInfo> midlets = new ArrayList<MidletInfo>();

    public J2meScriptLib() {
        methodNames.put("scanMidlets".toLowerCase(), this::scanMidlets);//
        methodNames.put("getMidletName".toLowerCase(), this::getMidletName);//
        methodNames.put("getMidletJar".toLowerCase(), this::getMidletJar);//
        methodNames.put("runMidlet".toLowerCase(), this::runMidlet);//
        methodNames.put("addListItem".toLowerCase(), this::addListItem);//
        methodNames.put("clearList".toLowerCase(), this::clearList);//
    }

    // -------------------------------------------------------------------------
    // implementation
    // -------------------------------------------------------------------------

    /**
     * 重新扫描 getMidlletHome() 下所有 *.jar（按文件名排序），解析 MANIFEST，
     * 返回应用数量
     */
    public DataType scanMidlets(ArrayList<DataType> para) {
        midlets.clear();
        try {
            String home = J2meEmu.getInstance().getMidlletHome();
            File dir = new File(home);
            File[] all = dir.listFiles();
            if (all != null) {
                int n = 0;
                for (int i = 0; i < all.length; i++) {
                    String name = all[i].getName().toLowerCase();
                    if (all[i].isFile() && name.endsWith(".jar")) {
                        all[n++] = all[i];
                    }
                }
                // 不用 Arrays.sort(Comparator)/String.compareToIgnoreCase：
                // 两者在 miniJVM 运行时抛 NoSuchMethodError，冒泡+toLowerCase 内联
                for (int i = 0; i < n; i++) {
                    for (int j = i + 1; j < n; j++) {
                        String a = all[i].getName().toLowerCase();
                        String b = all[j].getName().toLowerCase();
                        if (a.compareTo(b) > 0) {
                            File t = all[i];
                            all[i] = all[j];
                            all[j] = t;
                        }
                    }
                }
                for (int i = 0; i < n; i++) {
                    MidletInfo mi = new MidletInfo(all[i]);
                    parseManifest(mi);
                    midlets.add(mi);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return Interpreter.getCachedInt(midlets.size());
    }

    /**
     * 显示名：MANIFEST MIDlet-Name，缺省 jar 文件名
     */
    public DataType getMidletName(ArrayList<DataType> para) {
        int idx = Interpreter.popBackInt(para);
        if (idx < 0 || idx >= midlets.size()) return Interpreter.getCachedStr("");
        return Interpreter.getCachedStr(midlets.get(idx).appName);
    }

    /**
     * jar 内 icon png 懒加载解码为 GImage 并缓存在 MidletInfo（失败为 null）。
     * GImage 由 addListItem 以索引在 Java 侧直接取用，避免无图标的 Str 哨兵
     * 与 Obj 混用。此前的字符串化由 gscript 表达式类型缓存引起，非 method ref 桥。
     */
    static void loadIcon(MidletInfo mi) {
        if (mi.iconLoaded) return;
        mi.iconLoaded = true;
        if (mi.iconEntry == null) return;
        ZipFile zf = null;
        try {
            zf = new ZipFile(mi.jarFile);
            String entryName = mi.iconEntry;
            while (entryName.startsWith("/")) entryName = entryName.substring(1);
            ZipEntry entry = zf.getEntry(entryName);
            if (entry == null) entry = zf.getEntry("/" + entryName);
            if (entry != null) {
                byte[] data = readEntry(zf, entry);
                if (data != null && data.length > 0) {
                    mi.icon = GImage.createImage(data);
                }
            }
        } catch (Exception e) {
            // icon 提取失败不阻断列表
        } finally {
            closeQuietly(zf);
        }
    }

    public DataType getMidletJar(ArrayList<DataType> para) {
        int idx = Interpreter.popBackInt(para);
        if (idx < 0 || idx >= midlets.size()) return Interpreter.getCachedStr("");
        return Interpreter.getCachedStr(midlets.get(idx).jarFile.getAbsolutePath());
    }

    /**
     * 启动 midlet，等价旧文件选择框行为：file:路径 + rms 根目录
     */
    public DataType runMidlet(ArrayList<DataType> para) {
        String jarPath = Interpreter.popBackStr(para);
        if (jarPath != null && jarPath.length() > 0) {
            String[] args = new String[2];
            args[0] = "file:" + jarPath;
            args[1] = J2meEmu.getInstance().getRmsRoot();
            J2meEmu.main(args);
            GForm.flush();
        }
        return null;
    }

    /**
     * 脚本所在容器（XML 里挂 <script> 的组件）：oninit 在 build 过程中触发，
     * 此时 frame 尚未挂到 form，getComponent(form) 搜不到新树，
     * 须从脚本容器自身向下找
     */
    GContainer scriptRoot() {
        if (inp != null) {
            Object holder = inp.getGlobalVar(GObject.INTERPRETER_HOLDER_VAR_NAME);
            if (holder instanceof Obj) {
                Object v = ((Obj) holder).getVal();
                if (v instanceof GContainer) return (GContainer) v;
            }
        }
        return J2meEmu.getInstance().getForm();
    }

    /**
     * addListItem(listName, midletIdx, text, clickSub)
     * icon 由 midletIdx 在 Java 侧直接从懒加载缓存取（GImage 不经脚本层传递）；
     * 点击回调：li.onClinkScript = clickSub+"()"（与 XML onclick 同款机制，
     * 回调脚本里用 getListIdx 取选中项再取 jar 路径）
     */
    public DataType addListItem(ArrayList<DataType> para) {
        // gscript 参数按声明顺序弹出（第一个参数先 popBack，同 GuiScriptLib.showSlot）
        String listName = Interpreter.popBackStr(para);
        int idx = Interpreter.popBackInt(para);
        String text = Interpreter.popBackStr(para);
        String clickSub = Interpreter.popBackStr(para);
        GObject gobj = GToolkit.getComponent(scriptRoot(), listName);
        if (gobj instanceof GList) {
            GList list = (GList) gobj;
            GImage img = null;
            if (idx >= 0 && idx < midlets.size()) {
                MidletInfo mi = midlets.get(idx);
                loadIcon(mi);
                img = mi.icon;
            }
            GListItem li = list.addItem(img, text);
            li.setName("LI_APP_" + list.getElements().size());
            // doAction 守卫要求 actionListener 非 null 才会执行 onClinkScript
            li.setActionListener(new GActionListener() {
                @Override
                public void action(GObject gobj1) {
                }
            });
            if (clickSub != null && clickSub.length() > 0) {
                li.setOnClinkScript(clickSub + "()");
            }
        }
        return null;
    }

    public DataType clearList(ArrayList<DataType> para) {
        String listName = Interpreter.popBackStr(para);
        GObject gobj = GToolkit.getComponent(scriptRoot(), listName);
        if (gobj instanceof GList) {
            ((GList) gobj).clear();
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // manifest / zip helpers
    // -------------------------------------------------------------------------

    void parseManifest(MidletInfo mi) {
        ZipFile zf = null;
        try {
            zf = new ZipFile(mi.jarFile);
            ZipEntry entry = zf.getEntry("META-INF/MANIFEST.MF");
            if (entry == null) return;
            byte[] data = readEntry(zf, entry);
            if (data == null) return;
            Map<String, String> attrs = parseManifestAttrs(new String(data, "UTF-8"));
            String name = attrs.get("MIDlet-Name");
            if (name != null && name.trim().length() > 0) {
                mi.appName = name.trim();
            }
            // MIDlet-1: Name, iconPath, Class 第二字段优先，缺省回退 MIDlet-Icon
            String iconPath = null;
            String m1 = attrs.get("MIDlet-1");
            if (m1 != null) {
                String[] parts = m1.split(",");
                if (parts.length >= 2 && parts[1].trim().length() > 0) {
                    iconPath = parts[1].trim();
                }
            }
            if (iconPath == null) {
                String miIcon = attrs.get("MIDlet-Icon");
                if (miIcon != null && miIcon.trim().length() > 0) {
                    iconPath = miIcon.trim();
                }
            }
            mi.iconEntry = iconPath;
        } catch (Exception e) {
            // 非法 jar：保留文件名作应用名，无 icon
        } finally {
            closeQuietly(zf);
        }
    }

    /**
     * 手写 manifest 解析（避免依赖 java.util.jar）：属性名: 值，续行以单空格开头
     */
    static Map<String, String> parseManifestAttrs(String content) {
        Map<String, String> attrs = new HashMap<String, String>();
        String curKey = null;
        StringBuilder curVal = new StringBuilder();
        int start = 0;
        int len = content.length();
        for (int i = 0; i <= len; i++) {
            if (i == len || content.charAt(i) == '\n') {
                String line = content.substring(start, i);
                if (line.endsWith("\r")) line = line.substring(0, line.length() - 1);
                if (line.length() > 0) {
                    if (line.charAt(0) == ' ' && curKey != null) {
                        curVal.append(line.substring(1));
                    } else {
                        if (curKey != null) attrs.put(curKey, curVal.toString());
                        int colon = line.indexOf(':');
                        if (colon > 0) {
                            curKey = line.substring(0, colon).trim();
                            curVal.setLength(0);
                            curVal.append(line.substring(colon + 1).trim());
                        } else {
                            curKey = null;
                        }
                    }
                }
                start = i + 1;
            }
        }
        if (curKey != null) attrs.put(curKey, curVal.toString());
        return attrs;
    }

    static byte[] readEntry(ZipFile zf, ZipEntry entry) throws Exception {
        InputStream is = zf.getInputStream(entry);
        if (is == null) return null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) >= 0) {
                bos.write(buf, 0, n);
            }
            return bos.toByteArray();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

    static void closeQuietly(ZipFile zf) {
        if (zf != null) {
            try {
                zf.close();
            } catch (Exception e) {
            }
        }
    }
}
