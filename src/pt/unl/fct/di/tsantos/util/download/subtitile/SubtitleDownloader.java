package pt.unl.fct.di.tsantos.util.download.subtitile;

import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedFormatException;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedLanguageException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Comparator;
import pt.unl.fct.di.tsantos.util.collection.DoubleHashMap;
import pt.unl.fct.di.tsantos.util.collection.DoubleMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.tuned.FileUtilities;
import org.apache.commons.codec.binary.Base64;
import pt.unl.fct.di.tsantos.util.FileUtils;
import pt.unl.fct.di.tsantos.util.io.MD5Checksum;
import pt.unl.fct.di.tsantos.util.Pair;
import pt.unl.fct.di.tsantos.util.collection.CollectionUtilities;
import pt.unl.fct.di.tsantos.util.download.Downloader;

public abstract class SubtitleDownloader implements Downloader {

    public static final Comparator<SubtitleDownloader> COMPARATOR =
            new Comparator<SubtitleDownloader>() {
        public int compare(SubtitleDownloader o1, SubtitleDownloader o2) {
            if (o1.getPriority() < o2.getPriority()) return -1;
            else if (o1.getPriority() > o2.getPriority()) return 1;
            else return 0;
        }
    };

    /** md5ChecksumCache shared by all instances **/
    static DoubleMap<File, String> md5ChecksumCache =
            new DoubleHashMap<File, String>();

    /*static DoubleMap<File, String> md5ChecksumCache1 =
            new DoubleHashMap<File, String>();*/

    /** **/
    static final Logger sharedLogger;


    static {
        sharedLogger = Logger.getLogger(SubtitleDownloader.class.getName());
        try {
            sharedLogger.addHandler(new FileHandler("D:\\log.txt", true));
        } catch (Exception ex) {}
    }

    public static IOException warp(Exception ex) {
        if (ex instanceof IOException) return (IOException)ex;
        IOException ioe = new IOException();
        ioe.initCause(ex);
        return ioe;
    }

    public static boolean covers(Pair<Integer,SortedSet<Integer>> p1,
            Pair<Integer,SortedSet<Integer>> p2) {
        if (!p1.getFst().equals(p2.getFst())) return false;
        SortedSet<Integer> l1 = p1.getSnd();
        SortedSet<Integer> l2 = p2.getSnd();
        if (l1.isEmpty() && l2.isEmpty()) return true;
        else if (!l1.isEmpty() && l2.isEmpty()) return false;
        else if (l1.isEmpty() && !l2.isEmpty()) return false;
        else if (!l1.first().equals(l2.first())) return false;
        return true;
    }

    protected List<String> fileNames;
    protected Set<Language> langs;
    protected File saveDirectory;
    protected long countID;
    protected Language[] supportedLanguages;
    protected int priority;

    /** for downloaders with authentication **/
    protected String user;
    protected String password;
    protected boolean authenticationNeeded;

    /** subtitle downloader logger **/
    @SuppressWarnings("NonConstantLogger")
    protected Logger logger = Logger.getAnonymousLogger();

    /** download inputstream for subtitles **/
    protected InputStream is = null;

    /// CACHE TEST ///
    //protected DoubleMap<String, File> md5ChecksumCache;
    //////////////////
    
    public SubtitleDownloader(List<String> fileNames, Set<Language> langs,
            File saveDirectory) {
        this.fileNames = fileNames;
        this.langs = langs;
        this.saveDirectory = saveDirectory;
        this.countID = 0L;
        this.supportedLanguages = initSupportedLanguages();
        this.logger.setUseParentHandlers(false);
        this.priority = 0;
        this.authenticationNeeded = false;
    }

    public SubtitleDownloader(List<String> fileNames, File saveDirectory) {
        this(fileNames, new HashSet<Language>(), saveDirectory);
        this.langs.add(Language.ALL);
    }

    public SubtitleDownloader(List<String> fileNames, Set<Language> langs,
            File saveDirectory, String user, String password) {
        this(fileNames, langs, saveDirectory);
        if (user == null || password == null) throw new NullPointerException();
        this.user = user;
        this.password = password;
        authenticationNeeded = true;
    }

    public SubtitleDownloader(List<String> fileNames, File saveDirectory,
            String user, String password) {
        this(fileNames, saveDirectory);
        if (user == null || password == null) throw new NullPointerException();
        this.user = user;
        this.password = password;
        authenticationNeeded = true;
    }

    public boolean isAuthenticationNeeded() {
        return authenticationNeeded;
    }

    public boolean isAnonimousAuthentication() {
        if (isAuthenticationNeeded())
            return user.isEmpty() || password.isEmpty();
        return true;
    }

    public Logger getLogger() {
        return logger;
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

    public boolean isSupported(Language lang) {
        if (lang == null) return false;
        for (Language l : supportedLanguages)
            if (lang.equals(l)) return true;
        return false;
    }

    public void setFileNames(List<String> fileNames) {
        synchronized(this) {
            this.fileNames = fileNames;
        }
    }

    public boolean addFileName(String fileName) {
        synchronized(this) {
            return this.fileNames.add(fileName);
        }
    }

    public boolean removeFileName(String fileName) {
        synchronized(this) {
            return this.fileNames.remove(fileName);
        }
    }

    protected abstract Language[] initSupportedLanguages();

    protected SubtitleAttributes getInfoFromDiskFile(String fileName)
            throws UnsupportedFormatException
    {
        return SubtitleAttributes.getProperties(fileName);
    }

    protected abstract List<SubtitleAttributes> getInfoFromDownloadedFile(
            String fileName) throws UnsupportedFormatException;

    protected final void preDownloadM() throws IOException {
        login();
        preDownload();
    }
    
    protected void preDownload() throws IOException { }

    protected abstract void login() throws IOException;

    protected void fillMD5ChecksumMap() {
        fillMD5ChecksumMapDir(saveDirectory);
        System.out.println(md5ChecksumCache);
    }

    protected void fillMD5ChecksumMapDir(File dir) {
        for (File f : dir.listFiles()) {
            if (f == null) continue;
            else if(f.isDirectory()) fillMD5ChecksumMapDir(f);
            else {
                try {
                    if (md5ChecksumCache.containsKey(f)) continue;
                    String md5 = MD5Checksum.getMD5Checksum(f);
                    md5ChecksumCache.put(f, md5);
                } catch (NoSuchAlgorithmException ex) {
                } catch (IOException ex) {
                }
            }
        }
    }

    public Map<String, List<File>> downloadSubs() throws IOException {
        Map<String, List<File>> res = new HashMap<String, List<File>>();
        fillMD5ChecksumMap();
        preDownloadM();
        for (String dfileName : fileNames) {
            int counter = 0;
            int j = dfileName.lastIndexOf('.');
            String fileName = dfileName.substring(0, j);
            logger.info("Searching subtitles for " + dfileName);
            SubtitleAttributes thisProps = null;
            try {
                thisProps = getInfoFromDiskFile(fileName);
            } catch (UnsupportedFormatException ex) {
                logger.warning(ex.toString());
                continue;
            }
            //System.out.println(thisProps);
            List<SubtitleDescriptor> subs =
                    new LinkedList<SubtitleDescriptor>();
            for (Language lang : langs) {
                logger.fine("Searching in " + lang.getName());
                if (!isSupported(lang)) {
                    logger.warning(lang.getName() + " is not supported");
                    continue;
                }
                try {
                    SubtitleDescriptor ss = searchSubtitle(fileName,
                        thisProps, lang);
                    if (ss != null) subs.add(ss);
                } catch(UnsupportedLanguageException ex) {
                    logger.warning(lang.getName() + " is not supported");
                    continue;
                }
            }

            List<SubtitleDescriptor> fsubs =
                    new LinkedList<SubtitleDescriptor>();
            //Set<String> hashCheckCache = new HashSet<String>();
            logger.fine("Filtering duplicated subtitles");
            for (SubtitleDescriptor si : subs) {
                File save = saveDirectory;
                String lang = si.getISO639();
                if (lang != null && !lang.isEmpty()) {
                    save = new File(saveDirectory, lang);
                }
                if (!save.exists()) save.mkdirs();
                String hash = si.getSubHash();

                // check in the save directory
                // for files with the same hash
                // if there is continue looping subs
                // else go to next step
                if (hash != null) {
                    String hashU = hash.toUpperCase();
                    if (md5ChecksumCache.containsValue(hashU))
                        continue;
                    else {
                        File f = MD5Checksum.getFileWithCheckSum(save, hashU);
                        if (f != null) {
                            CollectionUtilities.put(dfileName, f, res);
                            md5ChecksumCache.put(f, hashU);
                            continue;
                        }
                    }

                    sharedLogger.log(Level.INFO, hashU + "not in md5cache",
                            md5ChecksumCache.toString());
                }

                fsubs.add(si);
            }

            List<SubtitleDescriptor> downs = downloadSubtitles(fsubs);

            logger.fine("Downloading subtitles");
            for (SubtitleDescriptor di : downs) {
                File save = (di.getISO639() == null || di.getISO639().isEmpty())
                        ? saveDirectory :
                            new File(saveDirectory, di.getISO639());
                if (!save.exists()) {
                    save.mkdirs();
                }

                String hash = di.getSubHash();
                String hashU = hash == null ? hash : hash.toUpperCase();
                System.out.println(hash);
                if (md5ChecksumCache.containsValue(hashU))
                    continue;
                else {
                    File f = MD5Checksum.getFileWithCheckSum(save, hashU);
                    if (f != null) {
                        CollectionUtilities.put(dfileName, f, res);
                        md5ChecksumCache.put(f, hashU);
                        continue;
                    }
                }

                sharedLogger.log(Level.INFO, hashU + "not in md5cache",
                            md5ChecksumCache.toString());

                File theFile = new File(save,
                        di.getDownFileName());
                boolean skip = false;
                while (theFile.exists()) {
                    if (!FileUtilities.hasExtension(theFile, "rar", 
                            "zip", "gzip")) {
                        float fcmp = FileUtils.compareTextFiles(
                                        FileUtils.getContent(theFile),
                                            AbstractSubtitleDescriptor.
                                                getSubtitleFileContent(di));
                        if (fcmp > 0.5) { // equal files
                            System.out.println("EQUAL FILES!");
                            skip = true;
                            break;
                        }
                    }
                    if (countID >= Long.MAX_VALUE)
                        countID = 0;
                    theFile = new File(save,
                            (countID++) +
                            "_" + di.getDownFileName());
                }

                if (skip) continue;

                di.saveData(theFile);

                CollectionUtilities.put(dfileName, theFile, res);
                
                logger.log(Level.INFO, "Downloaded" +
                        ((di.getISO639() == null || di.getISO639().isEmpty()) ?
                        "" : (" " + di.getISO639())) +
                        " subtitle for " + dfileName, di);
                counter++;
            }
            if (counter <= 0) logger.info("No subtitles found for " + dfileName);
            else if (counter == 1)
                logger.info(counter + " subtitile found for " + dfileName);
            else logger.info(counter + " subtitiles found for " + dfileName);

        }
        postDownloadM();
        return res;
    }

    public List<File> download() throws IOException {
        List<File> res = new LinkedList<File>();
        Collection<List<File>> col = downloadSubs().values();
        for (List<File> l : col)
            res.addAll(l);
        return res;
    }

    protected final void postDownloadM() throws IOException {
        postDownload();
        logout();
    }

    protected void postDownload() throws IOException {}

    protected abstract void logout() throws IOException;
    
    protected abstract SubtitleDescriptor searchSubtitle(String fileName,
            SubtitleAttributes thisProps, Language lang) throws IOException,
            UnsupportedLanguageException;

    protected List<SubtitleDescriptor> downloadSubtitles(
            List<SubtitleDescriptor> subs)
            throws IOException {
        List<SubtitleDescriptor> res = new LinkedList<SubtitleDescriptor>();
        for (SubtitleDescriptor si : subs) {
            preDownloadSubtitle();
            res.addAll(downloadSubtitle(si));
            postDownloadSubtitle();
        }
        return res;
    }
    
    protected abstract InputStream
            getDownloadInputStream(SubtitleDescriptor si) throws IOException;

    private List<SubtitleDescriptor> downloadSubtitle(SubtitleDescriptor si)
            throws IOException {
        List<SubtitleDescriptor> res = new LinkedList<SubtitleDescriptor>();
        is = getDownloadInputStream(si);
        if (is == null) return res;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int j;
        while ((j = is.read()) != -1) {
            bos.write(j);
        }
        byte[] arr = bos.toByteArray();
        bos.flush();
        bos.close();
        String data = Base64.encodeBase64String(arr);
        String hash = si.getSubHash();
        if (hash == null) {
            try {
                hash = MD5Checksum.getMD5Checksum(
                        new ByteArrayInputStream(arr));
            } catch (NoSuchAlgorithmException ex) {}
        }
        si.setSubHash(hash);
        si.setData(data);
        res.add(si);
        return res;
    }

    protected void preDownloadSubtitle() throws IOException {
        is = null;
    }

    protected void postDownloadSubtitle() throws IOException {
        if (is != null) is.close();
        is = null;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
