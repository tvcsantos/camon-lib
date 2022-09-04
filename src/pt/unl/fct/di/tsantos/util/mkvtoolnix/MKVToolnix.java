/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.mkvtoolnix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import pt.unl.fct.di.tsantos.util.io.ExecutableFile;
import pt.unl.fct.di.tsantos.util.Properties;
import pt.unl.fct.di.tsantos.util.app.AppUtils;
import pt.unl.fct.di.tsantos.util.exceptions.NotExecutableException;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedOSException;

/**
 *
 * @author tvcsantos
 */
public class MKVToolnix {
    private static File path;
    private static ExecutableFile mkvmerge;
    private static ExecutableFile mkvinfo;
    private static ExecutableFile mkvextract;

    private MKVToolnix() {
        throw new UnsupportedOperationException();
    }

    public static File getPath()
            throws FileNotFoundException, UnsupportedOSException {
        if (path == null) {
            ProgramDirectory pd =
                new ProgramDirectory("mkvtoolnix", new String[]{ "mkvmerge" });
            path = pd.getDirectory();
        }
        return path;
    }

    /**
     * Merge multimedia streams into a Matroska™ file
     */
    public static ExecutableFile getMKVMerge() 
            throws NotExecutableException, FileNotFoundException,
            UnsupportedOSException {
        if (mkvmerge == null) mkvmerge = getProgram("mkvmerge");
        return mkvmerge;
    }

    public static ExecutableFile getMKVInfo()
            throws NotExecutableException, FileNotFoundException,
            UnsupportedOSException {
        if (mkvinfo == null) mkvinfo = getProgram("mkvinfo");
        return mkvinfo;
    }

    public static ExecutableFile getMKVExtract()
            throws NotExecutableException, FileNotFoundException,
            UnsupportedOSException {
        if (mkvextract == null) mkvextract = getProgram("mkvextract");
        return mkvextract;
    }

    private static ExecutableFile getProgram(String name)
        throws NotExecutableException, FileNotFoundException,
            UnsupportedOSException {
        File dir = MKVToolnix.getPath();
        ExecutableFile res = null;
        if (AppUtils.osIsWindows())
            res = new ExecutableFile(dir, name + ".exe");
        else if (AppUtils.osIsLinux() || AppUtils.osIsMac())
            res = new ExecutableFile(dir, name);
        else throw new UnsupportedOSException();
        if (res == null || !res.exists() || !res.isFile())
            throw new FileNotFoundException();
        return res;
    }

     private static int getLevel(String line) {
        int index = line.indexOf("+");
        if (index < 0) {
            return -1;
        }
        String ls = line.substring(0, index + 1);
        if (!ls.contains("|")) {
            return 0;
        } else {
            String blanks = ls.substring(1, ls.length() - 1);
            return blanks.length() + 1;
        }
    }

    public static Properties getMKVInfoProperties(File source)
            throws IOException, InterruptedException,
            NotExecutableException, FileNotFoundException,
            UnsupportedOSException {
        if (source == null) {
            throw new NullPointerException("file must be non null");
        }
        if (!source.exists()) {
            throw new FileNotFoundException("file doesn't exist");
        }
        if (!source.isFile()) {
            throw new IllegalArgumentException("file must be a file");
        }
        ExecutableFile execFile = getMKVInfo();

        ExecutableFile.ExecutionResult res = execFile.execute(
                new String[]{ "\"" + source.getAbsolutePath() + "\"" });
        
        if (res.getExitCode() != 0) {
            return null;
        }
        BufferedReader bufferedreader =
                new BufferedReader(new StringReader(res.getOutput()));

        String line;
        Properties root = new Properties("File", source.getName());
        Properties current = root;
        int lastLevel = -1;
        while ((line = bufferedreader.readLine()) != null) {
            line = line.trim();
            if (line.length() <= 0) {
                continue;
            }
            int level = getLevel(line);
            String rep = line.replace("+", "").replace("|", "");
            int i = rep.indexOf(":");
            if (i < 0) {
                i = rep.indexOf(",");
            }
            String key = null;
            String value = null;
            if (i < 0) {
                key = rep.trim();
            } else {
                key = rep.substring(0, i).trim();
                value = rep.substring(i + 1).trim();
            }
            Properties newNode = new Properties(key, value);
            if (level > lastLevel) {
                current.add(newNode);
                current = newNode;
            } else if (level < lastLevel) {
                int down = lastLevel - level + 1;
                Properties node = current;
                while (down-- > 0) {
                    node = node.getParent();
                }
                node.add(newNode);
                current = newNode;
            } else {
                Properties parent = current.getParent();
                parent.add(newNode);
                current = newNode;
            }
            lastLevel = level;
        }
        return root;
    }

    public static void main(String[] args) 
            throws FileNotFoundException, IOException, 
            InterruptedException, UnsupportedOSException, NotExecutableException {
        System.setProperty("mkvtoolnix.path",
                new File("D:\\Programs\\AudioConverter\\Tools\\mkvtoolnix")
                .getAbsolutePath());
        System.out.println(MKVToolnix.getPath());
        /*System.out.println(MKVToolnix.getMKVInfoProperties(
                new File("D:\\Media\\"
                + "Burn.Notice.S05E09.720p.HDTV.X264-DIMENSION.mkv")).toFormatedString());*/
        /*String[] res = MKVToolnix.getMKVExtract().exec(new String[]{"-M", "--compression", "-1:none",
        "D:\\Media\\Shows\\Burn.Notice.S05E08.720p.HDTV.X264-DIMENSION.mkv",
        "-o","D:\\test.mkv", "--language", "0:por", "--compression", "0:none",
        "D:\\Media\\Shows\\Burn.Notice.S05E08.720p.HDTV.X264-DIMENSION.srt"
        }, new ProgressListener() {

            public void reportProgress(int progress) {
                System.out.println(progress);
            }

            public void reportCurrentTask(String taskDescription) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        if (res != null) for (String r : res) System.out.println(r);*/
        System.out.println(AppUtils.osIs64bits());

    }
}
