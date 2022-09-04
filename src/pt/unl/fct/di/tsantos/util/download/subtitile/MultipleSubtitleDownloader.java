package pt.unl.fct.di.tsantos.util.download.subtitile;

import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedFormatException;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedLanguageException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import pt.unl.fct.di.tsantos.util.collection.CollectionUtilities;

public abstract class MultipleSubtitleDownloader extends SubtitleDownloader {
    protected List<SubtitleDownloader> list;

    protected final void initMultipleSubtitleDownloader() {
        this.list = getSubtitleDownloaders();
        if (this.list == null)
            this.list = new LinkedList<SubtitleDownloader>();
        for (SubtitleDownloader sd : list) {
            sd.getLogger().addHandler(new Handler() {

                @Override
                public void publish(LogRecord record) {
                    logger.log(record);
                }

                @Override
                public void flush() {
                    
                }

                @Override
                public void close() throws SecurityException {
                    
                }
            });
        }
    }

    public MultipleSubtitleDownloader(List<String> fileNames,
            Set<Language> langs, File saveDirectory) {
        super(fileNames, langs, saveDirectory);
        initMultipleSubtitleDownloader();
    }

    public MultipleSubtitleDownloader(List<String> fileNames,
            File saveDirectory) {
        super(fileNames, saveDirectory);
        initMultipleSubtitleDownloader();
    }

    public MultipleSubtitleDownloader(List<String> fileNames,
            Set<Language> langs, File saveDirectory,
            String user, String password) {
        super(fileNames, langs, saveDirectory, user, password);
        initMultipleSubtitleDownloader();
    }

    public MultipleSubtitleDownloader(List<String> fileNames,
            File saveDirectory, String user, String password) {
        super(fileNames, saveDirectory, user, password);
        initMultipleSubtitleDownloader();
    }

    protected abstract List<SubtitleDownloader> getSubtitleDownloaders();

    @Override
    public void setFileNames(List<String> fileNames) {
        synchronized (this) {
            for (SubtitleDownloader sd : list)
                sd.setFileNames(fileNames);
        }
    }

    @Override
    public boolean addFileName(String fileName) {
        synchronized (this) {
            boolean res = false;
            for (SubtitleDownloader sd : list) {
                boolean x = sd.addFileName(fileName);
                res = res || x;
            }
            return res;
        }
    }

    @Override
    public boolean removeFileName(String fileName) {
        synchronized (this) {
            boolean res = false;
            for (SubtitleDownloader sd : list) {
                boolean x = sd.removeFileName(fileName);
                res = res || x;
            }
            return res;
        }
    }

    @Override
    public Map<String, List<File>> downloadSubs() throws IOException {
        Map<String, List<File>> res = new HashMap<String, List<File>>();
        for (SubtitleDownloader sd : list) {
            logger.info("Loading " + sd.getClass().getName());
            CollectionUtilities.mergeD(res, sd.downloadSubs());
        }
        return res;
    }

    @Override
    protected final List<SubtitleAttributes> getInfoFromDownloadedFile(
            String fileName) throws UnsupportedFormatException {
        throw new RuntimeException("Operation delegated to the"
                + " downloaders that compose this class");
    }

    @Override
    protected final SubtitleDescriptor searchSubtitle(String fileName,
            SubtitleAttributes thisProps, Language lang)
            throws IOException, UnsupportedLanguageException {
        throw new RuntimeException("Operation delegated to the"
                + " downloaders that compose this class");
    }

    @Override
    protected final InputStream getDownloadInputStream(SubtitleDescriptor si)
            throws IOException {
        throw new RuntimeException("Operation delegated to the"
                + " downloaders that compose this class");
    }

    @Override
    protected void login() throws IOException {
        throw new RuntimeException("Operation delegated to the"
                + " downloaders that compose this class");
    }

    @Override
    protected void logout() throws IOException {
        throw new RuntimeException("Operation delegated to the"
                + " downloaders that compose this class");
    }
}
