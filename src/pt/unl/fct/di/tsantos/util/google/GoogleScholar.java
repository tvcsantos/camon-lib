package pt.unl.fct.di.tsantos.util.google;


import pt.unl.fct.di.tsantos.util.swing.ProgressEvent;
import pt.unl.fct.di.tsantos.util.swing.ProgressListener;
import pt.unl.fct.di.tsantos.util.swing.EventProducerUtilities;
import bibtex.dom.BibtexAbstractEntry;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexFile;
import bibtex.parser.BibtexParser;
import bibtex.parser.ParseException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringEscapeUtils;
import pt.unl.fct.di.tsantos.util.collection.ArraysExtended;
import pt.unl.fct.di.tsantos.util.pdf.PDFUtilities;

/**
 *
 * @author tvcsantos
 */
public class GoogleScholar {

    private static String SEARCH_URL =
            "http://scholar.google.com/scholar?hl=en&q=%s";
    private static String HOME_URL =
            "http://scholar.google.com/";

    protected boolean cookieSet;
    
    protected BibtexFile bfile = new BibtexFile();
    
    protected Map<File, GoogleScholarResult> cache = 
            new HashMap<File, GoogleScholarResult>();

    public static class GoogleScholarResult implements Serializable {

        protected String title;
        protected String authors;
        protected String text;
        protected BibtexEntry bibtex;

        public GoogleScholarResult(String title, String authors,
                String text, BibtexEntry bibtex) {
            this.title = title;
            this.authors = authors;
            this.text = text;
            this.bibtex = bibtex;
        }

        public BibtexEntry getBibtex() {
            return bibtex;
        }
    }
    
    protected static GoogleScholar instance = null;
    protected HttpClient client;
    protected List<ProgressListener> listeners;

    private GoogleScholar() {
        client = new HttpClient();
        client.getParams().setCookiePolicy(
                CookiePolicy.BROWSER_COMPATIBILITY);
        client.getParams().setParameter(
                HttpMethodParams.USER_AGENT,
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 "
                + "(KHTML, like Gecko) Chrome/14.0.835.202 Safari/535.1");
        cookieSet = false;
        listeners = new LinkedList<ProgressListener>();
    }

    public static GoogleScholar getInstance() {
        if (instance == null) {
            instance = new GoogleScholar();
        }
        return instance;
    }

    protected String getScholarQueryInTitle(String p) throws IOException {
        String p_layout = p;
        String[] p_lines = p_layout.split("\n");
        String p_title = p_lines[0].trim();
        int p_title_wordcount = p_title.split("-| ").length;
        if (3 < p_title_wordcount && p_title_wordcount < 13) {
            String q = String.format("intitle:\"%s\"", p_lines[0].trim());
            return q;
        }
        return getScholarQuery(p, 0);
    }
    
    private static String[] STOPWORDS = {
        "I", "a", "about", "an", "are", "as", "at", "be", "by", "com",
        "for", "from", "how", "in", "is", "it", "of", "on", "or", "that",
        "the", "this", "to", "was", "what", "when", "where", "who",
        "will", "with", "the", "www"
    };

    protected String getScholarQuery(String p, int windex)
            throws IOException {
        String q = "";
        String p_az = p.toLowerCase();
        //# latex diacritics often as in: Inst. f" r Inf.
        p_az = p_az.replaceAll("[^a-z0-9\"]", " ");
        p_az = p_az.replaceAll(" [^ ]+\" [^ ]+ ", " ");
        p_az = p_az.replaceAll("[a-z]+[0-9] ", " ");
        //#autname1 autname2 get rid of (or keep as autname autname ?)
        //#p_az = re.sub(" +", " ", p_az)
        String[] words = p_az.split(" +");
        List<String> words2 = new ArrayList<String>();
        for (String w : words) {
            //# at least two chars long and at least one a-z in it
            if (w.length() > 1
                    && Pattern.compile("[a-z]").matcher(w).find()
                    && !ArraysExtended.contains(STOPWORDS, w)
                    && Pattern.compile("[aeiou].*[aeiou]").matcher(w).find()
                    && !words2.contains(w)) {
                words2.add(w);
            }
        }
        int upperb = windex + 99 >= words2.size()
                ? words2.size() : (windex + 99);
        for (int i = windex; i < upperb; i++) {
            String w = words2.get(i);
            q = q + w + " ";
            q = substring(q, 0, 255);
            q = q.replaceAll("[^ ]+$", ""); //# strip last unfinished word
        }
        return q;
    }

    protected GoogleScholarResult processFile(File file) throws IOException {
        String p = PDFUtilities.pdfToText(file, 10);
        String p_head = substring(p, 0, 999);
        Matcher m = Pattern.compile("(.*?)((abstract)|(introduction))",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(p_head);
        boolean bres = m.find();
        if (bres && m.group(1) != null) {
            p_head = m.group(1);
        }
        //System.out.println(p_head);
        String q = getScholarQueryInTitle(p);
        System.out.println(q);
        GoogleScholarResult res = matchScholarEntries(p_head, search(q));
        int querywordoffset = 64;
        int n = 0;
        while (res == null && n < 20) {
            q = getScholarQuery(p, n * querywordoffset);
            System.out.println(q);
            n++;
            if (q.length() < 33) {
                continue;
            }
            try {
                Thread.sleep(10 * 1000L);
            } catch (InterruptedException ex) {
            }
            res = matchScholarEntries(p_head, search(q));
            //System.out.println(q);
            //System.out.println("---------");
        }
        return res;
    }

    protected GoogleScholarResult matchScholarEntries(String p_head,
            List<GoogleScholarResult> entries) {
        String pn = normalizeTitle(p_head);
        for (GoogleScholarResult e : entries) {
            String t = e.title;
            String tn = normalizeTitle(t);
            //# test whether title contained in pdftext
            if (pn.contains(tn)) {
                return e;
            }
        }
        return null;
    }

    protected String normalizeTitle(String t) {
        //t = t.decode('utf8')
        //t = unidecode(t)
        t = t.toLowerCase();
        t = t.replaceAll("[^a-z0-9]", "");
        return t;
    }

    public static String substring(String s, int start, int end) {
        if (end >= s.length()) {
            return s.substring(start);
        } else {
            return s.substring(start, end);
        }
    }

    public void addProgressListener(ProgressListener listener) {
        listeners.add(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        listeners.add(listener);
    }

    protected List<GoogleScholarResult> search(String query)
            throws IOException {
        List<GoogleScholarResult> gslist =
                new LinkedList<GoogleScholarResult>();
        setScholarCookie();
        GetMethod method = new GetMethod(
                String.format(SEARCH_URL, URLEncoder.encode(query, "UTF-8")));
        int statusCode = client.executeMethod(method);
        if (statusCode != HttpStatus.SC_OK) {
            throw new HttpException("Method failed with status " + statusCode);
        }
        InputStream is = method.getResponseBodyAsStream();
        Source source = new Source(is);
        source.setLogger(null);
        source.fullSequentialParse();
        List<Element> list = source.getAllElements("div class=gs_r>");
        for (Element e : list) {
            /* title */
            Element tt = e.getFirstElement("div class=gs_rt");
            tt = tt.getFirstElement("h3");
            Element ttref = tt.getFirstElement("a href");
            String title = null;
            if (ttref != null) {
                tt = ttref;
                title = tt.getContent().toString();
            } else {
                String cont = tt.getContent().toString();
                if (cont.toLowerCase().contains("citation")) {
                    continue;
                    
                }
                title = cont.replaceAll("<span.*>.*</span>", "");
            }
            title = title.replaceAll("(</?b/?>)|(</?br/?>)", "").trim();
            //System.out.println(title);
            tt = e.getFirstElement("span class=gs_a");
            String authors = tt.getContent().toString();
            Matcher m = Pattern.compile("([^-]*)-.*",
                    Pattern.DOTALL).matcher(authors);
            if (m.matches()) {
                authors = m.group(1);
                
            }
            authors = StringEscapeUtils.unescapeHtml(authors);
            authors = authors.replaceAll("(</?b/?>)|(</?br/?>)", "").trim();
            //System.out.println(authors);
            tt = tt.getParentElement();
            String x = tt.getContent().toString();
            m = Pattern.compile(".*</span>(.*)<span.*",
                    Pattern.DOTALL).matcher(x);
            String text = null;
            if (m.matches()) {
                text = m.group(1);
                
            }
            if (text != null) {
                text = text.replaceAll(
                        "(</?b/?>)|(</?br/?>)", "").trim();
                //System.out.println(text);
                
            }
            tt = e.getFirstElement("span class=gs_fl");
            List<Element> hrefs = tt.getAllElements("a href");
            BibtexEntry bibtex = null;
            for (Element ref : hrefs) {
                String s = ref.getContent().toString();
                if (s.contains("Import into BibTeX")) {
                    String url = ref.getAttributeValue("href");
                    try {
                        URL bibtexURL = new URL(new URL(HOME_URL), url);
                        GetMethod bmethod = new GetMethod(bibtexURL.toString());
                        statusCode = client.executeMethod(bmethod);
                        if (statusCode != HttpStatus.SC_OK) {
                            throw new HttpException(
                                    "Method failed with status " + statusCode);
                        }
                        new BibtexParser(false).parse(bfile,
                                new InputStreamReader(
                                bmethod.getResponseBodyAsStream()));
                        Iterator<BibtexAbstractEntry> it =
                                bfile.getEntries().iterator();
                        //TODO: fix this for better efficience
                        while (it.hasNext()) {
                            BibtexAbstractEntry entry = it.next();
                            if (entry instanceof BibtexEntry) {
                                bibtex = (BibtexEntry) entry;
                                //break;
                            }
                        }
                        bmethod.releaseConnection();
                    } catch (ParseException ex) {
                    } catch (MalformedURLException ex) {
                    }
                    break;
                }
            }
            gslist.add(new GoogleScholarResult(title, authors, text, bibtex));
            //System.out.println(e.toString());
        }
        method.releaseConnection();
        return gslist;
    }

    public Map<File, GoogleScholarResult> search(File... files)
            throws IOException {
        List<File> asList = Arrays.asList(files);
        return search(asList);
    }

    public Map<File, GoogleScholarResult> search(Collection<File> files)
            throws IOException {
        EventProducerUtilities.notifyListeners(
                listeners, "progressStart", new ProgressEvent(this));
        Map<File, GoogleScholarResult> res =
                new HashMap<File, GoogleScholarResult>();
        for (File f : files) {
            if (cache.get(f) != null) {
                res.put(f, cache.get(f));
                continue;
            }
            try {
                res.put(f, processFile(f));
            } catch(IOException e) {
                //e.printStackTrace();
                EventProducerUtilities.notifyListeners(listeners, 
                        "progressInterrupt", new ProgressEvent(this));
                throw e;
                //break;
            }
            
        }
        cache.putAll(res);
        EventProducerUtilities.notifyListeners(listeners, "progressFinish",
                new ProgressEvent(this));
        return res;
    }

    private void setScholarCookie()
            throws IOException {
        if (cookieSet) return;
        GetMethod method = new GetMethod(HOME_URL);
        int statusCode = client.executeMethod(method);
        if (statusCode != HttpStatus.SC_OK) {
            throw new HttpException("Method failed with status " + statusCode);
        }
        Cookie[] cookies = client.getState().getCookies();
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            /*System.err.println(
            "Cookie: " + cookie.getName() +
            ", Value: " + cookie.getValue() +
            ", IsPersistent?: " + cookie.isPersistent() +
            ", Expiry Date: " + cookie.getExpiryDate() +
            ", Comment: " + cookie.getComment());*/
            if (cookie.getName().equals("GSP")) {
                cookie.setValue("CF=4:" + cookie.getValue());
            }
        }
        method.releaseConnection();
        cookieSet = true;
    }

    public static void main(String[] args) throws Exception {
        GoogleScholar test = GoogleScholar.getInstance();
        test.addProgressListener(new ProgressListener() {

            public void progressStart(ProgressEvent pe) {
                System.out.println("start");
            }

            public void progressUpdate(ProgressEvent pe) {
                System.out.println("update");
            }

            public void progressFinish(ProgressEvent pe) {
                System.out.println("finish");
            }

            public void progressInterrupt(ProgressEvent pe) {
                System.out.println("interrupt");
            }
        });
        /*System.out.println(
                test.pdfToText(new File("D:\\Documents\\My Dropbox\\bib\\test.pdf")));*/
        /*GoogleScholarResult processFile = test.processFile(
        new File("C:\\Users\\user\\Desktop\\pdfmeat\\test.pdf"));
        //new File("test.pdf"));
        if (processFile != null && processFile.getBibtex() != null) {
            System.out.println(processFile.bibtex);
        }*/
        /*Logger.getLogger("org.apache.commons.httpclient.HttpClient").setLevel(Level.OFF);
        Logger.getLogger("org.apache.pdfbox.encoding.Encoding").setLevel(Level.OFF);
        Logger.getLogger("org.apache.pdfbox.util.PDFStreamEngine").setLevel(Level.OFF);*/

        /*File dir = new File("C:\\Users\\user\\Desktop\\pdfmeat");
        File msc = new File("D:\\Documents\\My Dropbox\\bib\\msc");
        File phd = new File("D:\\Documents\\My Dropbox\\bib\\phd");
        File root = new File("D:\\Documents\\My Dropbox\\bib");
        List<File> dirs = new LinkedList<File>();
        dirs.add(root); 
        dirs.add(msc);
        dirs.add(phd);
        List<File> listFiles = FileUtilities.listFiles(dirs, 0);
        listFiles = FileUtilities.filter(listFiles,
                new FileUtilities.ExtensionFileFilter("pdf"));*/

        /*for (File f : listFiles) {
            PDDocument doc = PDDocument.load(f);
            //System.out.println(doc.getDocumentInformation().getMetadataKeys());
            PDDocumentCatalog documentCatalog = doc.getDocumentCatalog();
            PDMetadata metadata = documentCatalog.getMetadata();
            if (metadata == null) continue;
            //System.out.println(doc.getDocumentInformation().getTitle());
            System.out.println(metadata.getInputStreamAsString());
            doc.close();
        }*/

        /*Map<File, GoogleScholarResult> search =
                new HashMap<File, GoogleScholarResult>();

        try {
            ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("bib.ser"));
            search =
                (Map<File, GoogleScholarResult>) ois.readObject();
            ois.close();
        } catch (ClassNotFoundException e) {

        } catch (IOException e) {
            
        }

        if (search.isEmpty())
            search = test.search(listFiles);
        
        for (Entry<File, GoogleScholarResult> entry : search.entrySet()) {
            GoogleScholarResult value = entry.getValue();
            System.out.print(entry.getKey().getName());
            if (value != null && value.getBibtex() != null) {
                System.out.println("\n" + value.getBibtex());
                
            } else {
                System.out.println(" NOT FOUND");

            }
        }
        
        ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("bib.ser"));
        oos.writeObject(search);
        oos.close();*/

    }
}
