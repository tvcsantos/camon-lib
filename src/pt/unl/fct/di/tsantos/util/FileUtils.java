package pt.unl.fct.di.tsantos.util;

import pt.unl.fct.di.tsantos.util.io.MD5Checksum;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import net.sourceforge.tuned.FileUtilities;
import net.sourceforge.tuned.FileUtilities.ExtensionFileFilter;
import pt.unl.fct.di.tsantos.util.string.StringUtils;

public class FileUtils {

    protected FileUtils() {}

    static diff_match_patch dmp = new diff_match_patch();

    public static float compareFiles(File f1, File f2) throws IOException {
        String t1 = getContent(f1);
        String t2 = getContent(f2);
        LinkedList<Diff> res = dmp.diff_main(t1, t2);
        int maxString = Math.max(t1.length(), t2.length());
        float levDist = dmp.diff_levenshtein(res);
        float eq = 1 - (maxString == 0 ? 0 : (levDist / maxString));
        return eq;
    }

    public static float compareTextFiles(String t1, String t2) {
        LinkedList<Diff> res = dmp.diff_main(t1, t2);
        int maxString = Math.max(t1.length(), t2.length());
        float levDist = dmp.diff_levenshtein(res);
        float eq = 1 - (maxString == 0 ? 0 : (levDist / maxString));
        return eq;
    }

    public static String getContent(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        return StringUtils.getString(fis);
    }

    /** Fast & simple file copy. */
    public static void copy(File source, File dest) throws IOException {
        FileChannel in = null, out = null;
        try {
            in = new FileInputStream(source).getChannel();
            out = new FileOutputStream(dest).getChannel();

            long size = in.size();
            MappedByteBuffer buf = in.map(
                    FileChannel.MapMode.READ_ONLY, 0, size);

            out.write(buf);

        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    public static void copy(InputStream is, OutputStream os)
            throws IOException {
        int i = -1;
        while ((i = is.read()) != -1) {
            os.write(i);
        }
        os.flush();
    }
}
