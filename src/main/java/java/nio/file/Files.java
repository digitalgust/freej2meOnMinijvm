package java.nio.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.FileAttribute;

public class Files {

    public static Path createDirectories(Path dir, FileAttribute<?>... attrs)
            throws IOException {

        File f = new File(dir.getPath());
        f.mkdirs();

        return new Path(dir.getPath());
    }
}
