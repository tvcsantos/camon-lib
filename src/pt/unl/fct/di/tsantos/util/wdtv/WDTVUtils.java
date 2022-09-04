package pt.unl.fct.di.tsantos.util.wdtv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import net.sourceforge.tuned.FileUtilities;
import pt.unl.fct.di.tsantos.util.io.ExecutableFile;
import pt.unl.fct.di.tsantos.util.app.AppUtils;
import pt.unl.fct.di.tsantos.util.ProgressListener;
import pt.unl.fct.di.tsantos.util.Properties;
import pt.unl.fct.di.tsantos.util.exceptions.NotExecutableException;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedOSException;
import pt.unl.fct.di.tsantos.util.mkvtoolnix.MKVToolnix;

class IllegalCallException extends Exception {

    public IllegalCallException(Throwable cause) {
        super(cause);
    }

    public IllegalCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalCallException(String message) {
        super(message);
    }

    public IllegalCallException() {
    }
    
}

public class WDTVUtils {
    private static WDTVUtils instance;

    private WDTVUtils() {
        //throw new UnsupportedOperationException();
    }

    public static WDTVUtils getInstance() {
        if (instance == null) instance = new WDTVUtils();
        return instance;
    }

    /** */

    public long convertX(File source, File destination, File tempDir,
            Configuration conf, ProgressListener listener) 
            throws FileNotFoundException, IOException, 
            InterruptedException, UnsupportedOSException,
            NotExecutableException {
        String ext = FileUtilities.getExtension(source);
        long start = System.currentTimeMillis();
        
        if (ext.equalsIgnoreCase("avi")) {
            File outFile = new File(tempDir, "tmp1.mkv");
            convertAVI2MKV(source, outFile);
            source = outFile;
        }

        Properties root = MKVToolnix.getMKVInfoProperties(source);

        /** obtaining video, audio, and subtitle tracks info **/
        List<Properties> tracks = root.get("Segment").get(0)
                .get("Segment tracks").get(0).get("A track");
        Properties video = null;
        List<Properties> audios = new LinkedList<Properties>();
        List<Properties> subs = new LinkedList<Properties>();
        for (Properties n : tracks) {
            String type = n.get("Track type").get(0).getValue();
            if (type.equals("video")) video = n;
            else if (type.equals("audio")) audios.add(n);
            else if (type.equals("subtitles")) subs.add(n);
        }

        extractDTSTracks(audios, source, tempDir);

        long end = System.currentTimeMillis();
        return end - start;
    }

    public static long convertAVI2MKV(File source, File destination)
            throws FileNotFoundException, IOException, 
            InterruptedException, UnsupportedOSException,
            NotExecutableException {
        long start = System.currentTimeMillis();
        ExecutableFile mkvmerge = MKVToolnix.getMKVMerge();
        mkvmerge.execute(new String[]{ "-o",
            "\"" + destination.getAbsolutePath() + "\"" ,
            "\"" + source.getAbsolutePath() + "\"" });
        long end = System.currentTimeMillis();
        return end - start;
    }

    private static List<File> extractDTSTracks(List<Properties> audios,
            File source, File destination)
            throws FileNotFoundException, IOException, 
            InterruptedException, UnsupportedOSException,
            NotExecutableException {
        long start = System.currentTimeMillis();

        List<String> list = new LinkedList<String>();
        List<File> files = new LinkedList<File>();
        //extract all dts tracks
        //String args = "tracks " + "\"" + source.getAbsolutePath() + "\"";
        for (Properties a : audios) {
            String codec = a.get("Codec ID").get(0).getValue();
            if (codec.equals("A_DTS")) {
                String id = a.get("Track number").get(0).getValue();
                File f = new File(destination,id + ".dts");
                list.add(id + ":" + "\"" + f.getAbsolutePath() + "\"");
                files.add(f);
            }
        }

        ExecutableFile mkvextract = MKVToolnix.getMKVExtract();
        String[] args = new String[2 + list.size()];
        args[0] = "tracks";
        args[1] = "\"" + source.getAbsolutePath() + "\"";
        System.arraycopy(list.toArray(), 0, args, 2, list.size());
        mkvextract.execute(args);

        long end = System.currentTimeMillis();

        return files;
    }

    public static void convertDTS2AC3(File source, File destination) {
        
    }
    
    public static long convertUnix(File dcadec, File aften,
            File source, File destination, File tempDir,
            Configuration conf, ProgressListener listener) throws 
            IOException, IllegalCallException, 
            UnsupportedOSException, InterruptedException,
            NotExecutableException {
        if (AppUtils.osIsWindows())
            throw new IllegalCallException("OS is not UNIX");
        return convert(null, dcadec, aften,
                source, destination, tempDir, conf, listener);
    }

    public static long convertWindows(File eac3to, File source,
            File destination, File tempDir,
            Configuration conf, ProgressListener listener) throws IOException,
            IllegalCallException,
            UnsupportedOSException, 
            InterruptedException,
            NotExecutableException {
        if (!AppUtils.osIsWindows())
            throw new IllegalCallException("OS is not Windows");
        return convert(eac3to, null, null,
                source, destination, tempDir, conf, listener);
    }

    public static long convert(File eac3to,
            File dcadec, File aften, File source,
            File destination, File tempDir,
            Configuration conf, ProgressListener listener)
            throws IOException, UnsupportedOSException, InterruptedException, NotExecutableException {
        if (source == null || destination == null || tempDir == null
                || conf == null)
            throw new NullPointerException();
        if (AppUtils.osIsWindows() && eac3to == null)
            throw new NullPointerException();
        if (!AppUtils.osIsWindows() && (dcadec == null || aften == null))
            throw new NullPointerException();

        long start = System.currentTimeMillis();

        String mkvmergePath = null;
        String mkvextractPath = null;
        String eac3toPath = null;
        String dcadecPath = null;
        String aftenPath = null;
        
        boolean unix = !AppUtils.osIsWindows();

        int steps = 4;
        int currentStep = 0;

        mkvmergePath = MKVToolnix.getMKVMerge().
                getAbsolutePath().replace("%20", " ");
        mkvextractPath = MKVToolnix.getMKVExtract().
                getAbsolutePath().replace("%20", " ");
        eac3toPath = eac3to == null ? null :
            eac3to.getAbsolutePath().replace("%20", " ");
        dcadecPath = dcadec == null ? null :
            dcadec.getAbsolutePath().replace("%20", " ");
        aftenPath = aften == null ? null :
            aften.getAbsolutePath().replace("%20", " ");

        List<File> toDelete = new LinkedList<File>();
        
        int extIndex = source.getName().lastIndexOf(".");

        String ext = (extIndex >= 0) ?
            source.getName().substring(extIndex + 1) : "";

        if (ext.compareToIgnoreCase("avi") == 0) {
            steps++;
            currentStep++;
            File outFile = new File(tempDir, "tmp1.mkv");
            String cmd = mkvmergePath + " -o " + "\"" +
                    outFile.getAbsolutePath() + "\"" + " " + "\"" +
                    source.getAbsolutePath() + "\"";

            //System.out.println(cmd);
            Process proc = null;
            if (unix) 
                proc = AppUtils.RUNTIME.exec(new String[]{ "sh", "-c", cmd });
            else proc = AppUtils.RUNTIME.exec(cmd);
            InputStream inputstream = proc.getInputStream();
            InputStreamReader inputstreamreader =
                    new InputStreamReader(inputstream);
            BufferedReader bufferedreader =
                    new BufferedReader(inputstreamreader);

            // read the ls output

            String step0 = "Step 0/4 (Converting AVI to MKV)\n";
            String mkvmergeOut = "";
            String progress = "0";

            String line = null;
            while ((line = bufferedreader.readLine()) != null) {
                line = line.trim();
                if (line.length() <= 0) continue;
                if (line.contains("Progress")) progress = line + "\n";
                else mkvmergeOut += line + "\n";

                listener.reportProgress((int) Math.round(
                        (currentStep - 1)*(100.0/steps) +
                        Double.valueOf(
                        progress.replace("Progress", "").replace(":","").
                        replace("%", "").trim())/steps));
                listener.reportCurrentTask(step0 + mkvmergeOut + progress);
            }

            // check for ls failure

            try {
                if (proc.waitFor() != 0) {
                    System.err.println("exit value = " + proc.exitValue());
                } else {
                    source = outFile;
                    toDelete.add(outFile);
                }
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

        String mkvextractOut = "";
        String eac3toOut = "";
        String mkvmergeOut = "";
        String step1 = "Step 1/4 (Retrieving File Info)\n";
        String step2 = "Step 2/4 (Extracting DTS tracks)\n";
        String step3 = "Step 3/4 (Encoding DTS to AC3)\n";
        String step4 = "Step 4/4 (Remuxing files to mkv)\n";
        String progress = "0";

        String cmd = null;

        //cmd = mkvinfoPath + " " + "\"" + source.getAbsolutePath() + "\"";

        Process proc = null;
        /*if (unix)
            proc = AppUtils.RUNTIME.exec(new String[]{ "sh", "-c", cmd });
        else proc = AppUtils.RUNTIME.exec(cmd);
        InputStream inputstream = proc.getInputStream();
        InputStreamReader inputstreamreader =
                    new InputStreamReader(inputstream);
        BufferedReader bufferedreader =
                    new BufferedReader(inputstreamreader);

        // read the ls output*/

        String line = null;

        Properties root = MKVToolnix.getMKVInfoProperties(source);
        currentStep++;
        listener.reportProgress((int) Math.round(
                (currentStep - 1)*(100.0/steps) + 100.0/steps));
        listener.reportCurrentTask(step1 + root.toFormatedString());


        List<Properties> tracks = root.get("Segment").get(0)
                .get("Segment tracks").get(0).get("A track");
        Properties video = null;
        List<Properties> audios = new LinkedList<Properties>();
        List<Properties> subs = new LinkedList<Properties>();
        for (Properties n : tracks) {
            String type = n.get("Track type").get(0).getValue();
            if (type.equals("video")) video = n;
            else if (type.equals("audio")) audios.add(n);
            else if (type.equals("subtitles")) subs.add(n);
        }

        //extract all dts tracks
        String args = "tracks " + "\"" + source.getAbsolutePath() + "\"";
        for (Properties a : audios) {
            String codec = a.get("Codec ID").get(0).getValue();
            if (codec.equals("A_DTS")) {
                String id = a.get("Track number").get(0).getValue();
                args += " " + id + ":" + "\"" +
                    new File(tempDir,id + ".dts").getAbsolutePath() +
                    "\"";
            }
        }

        cmd = mkvextractPath + " " + args;
        //System.out.println(cmd);

        proc = null;
        if (unix)
            proc = AppUtils.RUNTIME.exec(new String[]{ "sh", "-c", cmd });
        else proc = AppUtils.RUNTIME.exec(cmd);
        InputStream inputstream = proc.getInputStream();
        InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

        // read the ls output

        line = null;
        progress = "0";
        currentStep++;
        while ((line = bufferedreader.readLine()) != null) {
            line = line.trim();
            if (line.length() <= 0) continue;
            if (line.contains("Progress")) progress = line + "\n";
            else mkvextractOut += line + "\n";

            listener.reportProgress((int) Math.round(
                    (currentStep - 1)*(100.0/steps) +
                        Double.valueOf(
                        progress.replace("Progress", "").replace(":","").
                        replace("%", "").trim())/steps));
            listener.reportCurrentTask(step2 + mkvextractOut + progress);
        }

        // check for ls failure

        try {
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
        } catch (InterruptedException e) {
            System.err.println(e);
        }

        currentStep++;
        double d = (100.0/steps)/audios.size();
        double acum = 0.0;
        for (Properties a : audios) {
            String codec = a.get("Codec ID").get(0).getValue();
            if (codec.equals("A_DTS")) {
                String id = a.get("Track number").get(0).getValue();
                if (eac3toPath != null) {
                    args = "\"" + new File(tempDir,
                        id + ".dts").getAbsolutePath() + "\"" + " " +
                        "\"" + new File(tempDir,
                        id + ".ac3").getAbsolutePath() + "\"" + " -" +
                        conf.getBitRate() + " -libav";
                    cmd = eac3toPath + " " + args;
                } else {
                    args = "-o wavall " + "\"" + new File(tempDir,
                        id + ".dts").getAbsolutePath() + "\"" + " | " +
                        aftenPath + " -b " +
                        conf.getBitRate() + " - " + "\"" +
                        new File(tempDir,
                            id + ".ac3").getAbsolutePath() + "\"";
                    cmd = dcadecPath + " " + args;
                }

                //System.out.println(cmd);
                proc = null;
                if (unix)
                    proc = AppUtils.RUNTIME.exec(
                            new String[]{ "sh", "-c", cmd });
                else proc = AppUtils.RUNTIME.exec(cmd);

                inputstream = proc.getInputStream();
                inputstreamreader = new InputStreamReader(inputstream);
                bufferedreader = new BufferedReader(inputstreamreader);

                // read the ls output

                line = null;
                progress = "0";
                while ((line = bufferedreader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() <= 0) continue;
                    if (line.contains("Progress")) progress = line + "\n";
                    else eac3toOut += line + "\n";

                    listener.reportProgress((int) Math.round(
                            (currentStep - 1)*(100.0/steps) + acum));
                    listener.reportCurrentTask(step3 + eac3toOut);
                }

                acum += d;

                // check for ls failure

                try {
                    if (proc.waitFor() != 0) {
                        System.err.println("exit value = " +
                                                proc.exitValue());
                    }
                } catch (InterruptedException e) {
                    System.err.println(e);
                }

                new File(tempDir, id + ".dts").delete();
            }
        }

        String out = "-o " + "\"" + destination.getAbsolutePath() + "\"";
        String copy = " -a ";
        String copyO = "";
        String dts = "";
        int copyCount = 0;
        for (Properties a : audios) {
            String codec = a.get("Codec ID").get(0).getValue();
            String id = a.get("Track number").get(0).getValue();
            if (codec.equals("A_DTS")) {
                int flag = Integer.valueOf(
                        a.get("Default flag").get(0).getValue());
                String lang = a.get("Language").get(0).getValue();
                dts += (flag == 1 ? " --default-track 0 " : " ") +
                        "--language 0:" + lang + " -a 0 --compression 0:none " + "\"" +
                        new File(tempDir,
                        id + ".ac3").getAbsolutePath() + "\"";
                toDelete.add(new File(tempDir, id + ".ac3"));
            } else {
                copy += id + ",";
                copyO += "--compression " + id + ":none ";
                copyCount++;
            }
        }
        if (copyCount == 0) copy = " -A";
        else { 
            copy = copy.substring(0, copy.length() - 1);
            copyO = copyO.trim();
        }

        String subtitles = "";

        if (conf.hasSubtitles()) {
            Collection<MKVSubtitle> subsc = conf.getSubtitles();
            for (MKVSubtitle sub : subsc) {
                if (sub.isDefault()) subtitles += "--default-track 0 ";
                subtitles += "--language 0:" +
                        sub.getLanguage().getISO6392code() +
                        " --compression 0:none " + "\"" + sub.getFile().getAbsolutePath() +
                        "\"" + " ";
            }
        }
        subtitles = subtitles.trim();

        String copyVideo = 
                "--compression " +
                video.get("Track number").get(0).getValue() +
                ":none";

        cmd = mkvmergePath + " " + out + copy + " " + copyO + " " +
                copyVideo + " " + "\"" +
                source.getAbsolutePath() + "\"" + dts + " " + subtitles;

        //System.out.println(cmd);

        proc = null;
        if (unix)
            proc = AppUtils.RUNTIME.exec(new String[]{ "sh", "-c", cmd });
        else proc = AppUtils.RUNTIME.exec(cmd);
        inputstream = proc.getInputStream();
        inputstreamreader =
                new InputStreamReader(inputstream);
        bufferedreader =
                new BufferedReader(inputstreamreader);

        // read the ls output

        line = null;
        currentStep++;
        progress = "0";
        while ((line = bufferedreader.readLine()) != null) {
            line = line.trim();
            if (line.length() <= 0) continue;
            if (line.contains("Progress")) progress = line + "\n";
            else mkvmergeOut += line + "\n";

            listener.reportProgress((int) Math.round(
                    (currentStep - 1)*(100.0/steps) +
                        Double.valueOf(
                        progress.replace("Progress", "").replace(":","").
                        replace("%", "").trim())/steps));
            listener.reportCurrentTask(step4 + mkvmergeOut + progress);
        }

        // check for ls failure

        try {
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
        } catch (InterruptedException e) {
            System.err.println(e);
        }

        for (File f : toDelete) {
            f.delete();
            String parent = f.getParentFile().getAbsolutePath();
            String name = f.getName();
            int index = name.lastIndexOf(".");
            String fileExt = (index >= 0) ? name.substring(index + 1) : "";
            if (fileExt.compareToIgnoreCase("ac3") == 0) {
                if (index >= 0) name = name.substring(0, index);
                new File(parent, name + " - Log.txt").delete();
            }
        }

        long end = System.currentTimeMillis();

        listener.reportProgress(100);
        listener.reportCurrentTask(
                "Completed in " + ((end - start)/1000.0) + "s");

        return (end - start);
    }
}