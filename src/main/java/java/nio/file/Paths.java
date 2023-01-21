package java.nio.file;

import java.io.File;
import java.net.URI;

public class Paths {

    public static Path get(String p, String... more) {
        StringBuilder sb = new StringBuilder(p);
        for (int i = 0; i < more.length; i++) {
            sb.append(File.separator);
            sb.append(more[i]);
        }
        File f = new File(sb.toString());
        return new Path(f.getAbsolutePath());
    }

    public static Path get(URI uri) {
        return new Path("");
    }

}
