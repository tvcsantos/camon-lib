package pt.unl.fct.di.tsantos.util.download.subtitile;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SubtitleDownloaderTask implements Runnable {

    protected File searchDirectory;
    protected FileFilter ff;
    protected List<SubtitleDownloader> list;
    protected Logger logger = Logger.getAnonymousLogger();
    protected boolean firstMatch;

    public SubtitleDownloaderTask(File searchDirectory, FileFilter ff,
            List<SubtitleDownloader> list) {
        this(searchDirectory, ff, list, false);
    }

    public SubtitleDownloaderTask(File searchDirectory, FileFilter ff,
            List<SubtitleDownloader> list, boolean firstMatch) {
        if (searchDirectory == null) {
            throw new NullPointerException("searchDirectory must be non null");
        }
        if (!searchDirectory.isDirectory()) {
            throw new IllegalArgumentException(
                    "searchDirectory must be a directory");
        }

        this.searchDirectory = searchDirectory;
        this.ff = ff;
        this.list = list;
        this.firstMatch = firstMatch;
        logger.setUseParentHandlers(false);
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public void run() {
        File[] arr = ff == null ? searchDirectory.listFiles()
                : searchDirectory.listFiles(ff);

        if (arr == null) {
            return;
        }

        List<String> fnames = new LinkedList<String>();

        for (final File f : arr) {
            if (f == null) {
                continue;
            }
            String fname = f.getName();
            fnames.add(fname);
        }

        SortedSet<SubtitleDownloader> set = new TreeSet<SubtitleDownloader>(
                SubtitleDownloader.COMPARATOR);
        set.addAll(list);
        
        for (final SubtitleDownloader sd : set) {
            logger.info("Checking files @ " +
                    sd.getClass().getName());
            sd.setFileNames(fnames);
            try {
                //sd.download();
                Map<String, List<File>> map = sd.downloadSubs();
                if (firstMatch) fnames.removeAll(map.keySet());
            } catch (IOException ex) {
                logger.severe(ex.toString());
            }
            logger.info("All files checked @ " +
                    sd.getClass().getName());
        }

        logger.info("All files checked!");
    }

    public void addLoggerHandler(Handler handler) {
        logger.addHandler(handler);
    }

    public void addLoggerHandlers(List<Handler> handlers) {
        for (Handler handler : handlers)
            addLoggerHandler(handler);
    }

    public void setLoggerLevel(Level level) {
        logger.setLevel(level);
    }
}