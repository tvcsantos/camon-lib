package pt.unl.fct.di.tsantos.util.download.subtitile;

import java.util.Set;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedFormatException;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedLanguageException;
import pt.unl.fct.di.tsantos.util.FileUtils;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import pt.unl.fct.di.tsantos.util.Pair;
import pt.unl.fct.di.tsantos.util.net.RSSFeed;
import pt.unl.fct.di.tsantos.util.math.Distance;
import pt.unl.fct.di.tsantos.util.io.StringOutputStream;
import static pt.unl.fct.di.tsantos.util.download.subtitile.Language.*;

public class Addic7edSubtitleDownloader extends MultipleSubtitleDownloader {

    private static final String WEBURL = "http://www.addic7ed.com";

    public static final RSSFeed RSS =
            RSSFeed.getRSSFeedSilent(
            WEBURL + "/rss.php?mode=completed",
            RSSFeed.Type.ORIGINAL);

    private static final Language[] supportedLangs = { Language.ALL,
        EN, ES, IT, FR, PT, PB, DE, CA, EU, CS,
        GL, TR, NL, SV, RU, HU, PL, SL, HE, ZH,
        SK, RO, EL, FI, NO, DA, HR, JA, BG, SR,
        ID, AR, MS, KO, FA, BS, VI, TH
    };

    public Addic7edSubtitleDownloader(List<String> fileNames,
            Set<Language> langs, File saveDirectory, String user,
            String password) {
        super(fileNames, langs, saveDirectory, user, password);
    }

    public Addic7edSubtitleDownloader(List<String> fileNames,
            File saveDirectory, String user, String password) {
        super(fileNames, saveDirectory, user, password);
    }

    @Override
    protected List<SubtitleDownloader> getSubtitleDownloaders() {
        List<SubtitleDownloader> res = new LinkedList<SubtitleDownloader>();
        res.add(new Addic7edRSSSubtitleDownloader(fileNames,
                langs, saveDirectory, user, password));
        res.add(new Addic7edHTMLSubtitleDownloader(fileNames,
                langs, saveDirectory, user, password));
        return res;
    }

    protected int getCodeForLang(Language lang) {
        int i = -1;
        for (Language l : supportedLanguages) {
            i++;
            if (l.equals(lang)) break;
        }
        if (i == 2) i = i + 2;
        else if (i > 2 && i < 29) i = i + 4;
        else if (i >= 29 && i < 33) i = i + 6;
        else if (i == 33) i = i + 7;
        else if (i >= 34) i = i + 8;
        return i;
    }

    protected static Language getLangFromString(String lang) {
        if (lang.compareToIgnoreCase("All") == 0) return ALL;
        else if (lang.compareToIgnoreCase("English") == 0) return EN;
        else if (lang.compareToIgnoreCase("English (UK)") == 0) return EN;
        else if (lang.compareToIgnoreCase("English (US)") == 0) return EN;
        else if (lang.compareToIgnoreCase("Spanish") == 0) return ES;
        else if (lang.compareToIgnoreCase("Spanish (Spain)") == 0) return ES;
        else if (lang.compareToIgnoreCase("Spanish (Latin America)") == 0)
            return ES;
        else if (lang.compareToIgnoreCase("Italian") == 0) return IT;
        else if (lang.compareToIgnoreCase("French") == 0) return FR;
        else if (lang.compareToIgnoreCase("Portuguese") == 0) return PT;
        else if (lang.compareToIgnoreCase("Portuguese (Brazilian)") == 0)
            return PB;
        else if (lang.compareToIgnoreCase("German") == 0) return DE;
        else if (lang.compareToIgnoreCase("Català") == 0) return CA;
        else if (lang.compareToIgnoreCase("Euskera") == 0) return EU;
        else if (lang.compareToIgnoreCase("Czech") == 0) return CS;
        else if (lang.compareToIgnoreCase("Galego") == 0) return GL;
        else if (lang.compareToIgnoreCase("Turkish") == 0) return TR;
        else if (lang.compareToIgnoreCase("Dutch") == 0) return NL;
        else if (lang.compareToIgnoreCase("Swedish") == 0) return SV;
        else if (lang.compareToIgnoreCase("Russian") == 0) return RU;
        else if (lang.compareToIgnoreCase("Hungarian") == 0) return HU;
        else if (lang.compareToIgnoreCase("Polish") == 0) return PL;
        else if (lang.compareToIgnoreCase("Slovenian") == 0) return SL;
        else if (lang.compareToIgnoreCase("Hebrew") == 0) return HE;
        else if (lang.compareToIgnoreCase("Chinese (Traditional)") == 0)
            return ZH;
        else if (lang.compareToIgnoreCase("Slovak") == 0) return SK;
        else if (lang.compareToIgnoreCase("Romanian") == 0) return RO;
        else if (lang.compareToIgnoreCase("Greek") == 0) return EL;
        else if (lang.compareToIgnoreCase("Finnish") == 0) return FI;
        else if (lang.compareToIgnoreCase("Norwegian") == 0) return NO;
        else if (lang.compareToIgnoreCase("Danish") == 0) return DA;
        else if (lang.compareToIgnoreCase("Croatian") == 0) return HR;
        else if (lang.compareToIgnoreCase("Japanese") == 0) return JA;
        else if (lang.compareToIgnoreCase("Bulgarian") == 0) return BG;
        else if (lang.compareToIgnoreCase("Serbian (Latin)") == 0) return SR;
        else if (lang.compareToIgnoreCase("Indonesian") == 0) return ID;
        else if (lang.compareToIgnoreCase("Arabic") == 0) return AR;
        else if (lang.compareToIgnoreCase("Serbian (Cyrillic)") == 0)
            return SR;
        else if (lang.compareToIgnoreCase("Malay") == 0) return MS;
        else if (lang.compareToIgnoreCase("Chinese (Simplified)") == 0)
            return ZH;
        else if (lang.compareToIgnoreCase("Korean") == 0) return KO;
        else if (lang.compareToIgnoreCase("Persian") == 0) return FA;
        else if (lang.compareToIgnoreCase("Bosnian") == 0) return BS;
        else if (lang.compareToIgnoreCase("Vietnamese") == 0) return VI;
        else if (lang.compareToIgnoreCase("Thai") == 0) return TH;
        else return null;
    }

    @Override
    protected Language[] initSupportedLanguages() {
        return supportedLangs;
    }

    private class Addic7edRSSSubtitleDownloader extends RSSSubtitleDownloader {

        protected final HttpClient client;
        protected HttpMethod currentMethod;

        public Addic7edRSSSubtitleDownloader(List<String> fileNames,
                Set<Language> langs, File saveDirectory,
                String user, String password) {
            super(fileNames, langs, saveDirectory, user, password);
            this.client = new HttpClient();
        }

        public Addic7edRSSSubtitleDownloader(List<String> fileNames,
                File saveDirectory, String user, String password) {
            super(fileNames, saveDirectory, user, password);
            this.client = new HttpClient();
        }

        @Override
        protected void login() throws IOException {
            Addic7edSubtitleDownloader.login(client, user, password);
        }

        @Override
        protected void logout() throws IOException {
            Addic7edSubtitleDownloader.logout(client);
        }

        @Override
        protected RSSFeed getSupportedRSSFeed(Language lang) {
            return RSS;
        }

        @Override
        protected List<Pair<SubtitleAttributes, SubtitleDescriptor>>
                getInfoFromSyndEntry(SyndEntry se, Language lang)
                throws UnsupportedFormatException, IOException {
            String title = se.getTitle().toLowerCase();
            Matcher mT = Pattern.compile("(.*) - (\\d{2})x(\\d{2}) -.*",
                    Pattern.DOTALL).matcher(title);
            SubtitleAttributes sa = null;
            if (mT.matches()) {
                String name = mT.group(1).trim();
                Integer season = Integer.parseInt(mT.group(2).trim());
                SortedSet<Integer> s = new TreeSet<Integer>();
                s.add(Integer.parseInt(mT.group(3).trim()));
                sa = new SubtitleAttributes(name, season, s);
            } else {
                throw new UnsupportedFormatException(title);
            }

            String link = se.getLink();
            return getAttDescList(link, sa, lang);
        }

        @Override
        protected SubtitleDescriptor getSubtitleDescriptor(
                Pair<SubtitleAttributes, SubtitleDescriptor> pair,
                SyndEntry entry, Language lang) throws IOException {
            return pair.getSnd();
        }

        @Override
        protected Language[] initSupportedLanguages() {
            return supportedLangs;
        }

        @Override
        protected List<SubtitleAttributes> getInfoFromDownloadedFile(
                String fileName) throws UnsupportedFormatException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected InputStream getDownloadInputStream(SubtitleDescriptor si)
                throws IOException {
            String downloadURL = si.getSubDownloadLink();
            currentMethod = new GetMethod(downloadURL);
            return getStream(this, client, currentMethod, si);
        }

        @Override
        protected void postDownloadSubtitle() throws IOException {
            super.postDownloadSubtitle();
            if (currentMethod != null) currentMethod.releaseConnection();
            currentMethod = null;
        }

        @Override
        protected SyndEntry filterSyndEntry(SubtitleAttributes sa,
            SyndEntry se, Language lang) {
            if (lang.equals(Language.ALL)) return se;
            SyndContent con = se.getDescription();
            if (con == null) return se;
            String desc = con.getValue();
            if (desc == null) return se;
            Matcher m =
                    Pattern.compile(".*,(.+)", Pattern.DOTALL).matcher(desc);
            if (m.matches()) {
                String slang = m.group(1).trim();
                m = Pattern.compile("(.+)(\\([^\\(\\)]+\\))",
                        Pattern.DOTALL).matcher(slang);
                if (m.matches()) {
                    String slangp1 = m.group(1).trim();
                    String slangp2 = m.group(2).trim();
                    if (slangp1.compareToIgnoreCase(lang.getName()) == 0 ||
                            slangp2.compareToIgnoreCase(lang.getName()) == 0) {
                        return se;
                    }
                } else {
                    if (slang.compareToIgnoreCase(lang.getName()) == 0)
                        return se;
                }
            }

            return null;
        }
        
    }

    private class Addic7edHTMLSubtitleDownloader extends SubtitleDownloader {

        protected final HttpClient client;
        protected HttpMethod currentMethod;

        public Addic7edHTMLSubtitleDownloader(List<String> fileNames,
                Set<Language> langs, File saveDirectory,
                String user, String password) {
            super(fileNames, langs, saveDirectory, user, password);
            this.client = new HttpClient();
        }

        public Addic7edHTMLSubtitleDownloader(List<String> fileNames,
                File saveDirectory, String user, String password) {
            super(fileNames, saveDirectory, user, password);
            this.client = new HttpClient();
        }

        @Override
        protected Language[] initSupportedLanguages() {
            return supportedLangs;
        }

        @Override
        protected void login() throws IOException {
            Addic7edSubtitleDownloader.login(client, user, password);
        }

        @Override
        protected void logout() throws IOException {
            Addic7edSubtitleDownloader.logout(client);
        }

        @Override
        protected List<SubtitleAttributes> getInfoFromDownloadedFile(
                String fileName) throws UnsupportedFormatException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected SubtitleDescriptor searchSubtitle(String fileName, 
                SubtitleAttributes thisProps, Language lang)
                throws IOException, UnsupportedLanguageException {
            String name = thisProps.getName().replace(" ", "_");
            String link = WEBURL + "/serie/" + name + "/" +
                    thisProps.getSeason() + "/" + 
                    thisProps.getEpisodes().first() + "/" +
                    getCodeForLang(lang);

            HttpMethod m = new GetMethod(link);
            client.executeMethod(m);
            InputStream ins = m.getResponseBodyAsStream();
            StringOutputStream os = new StringOutputStream();
            FileUtils.copy(ins, os);
            String body = os.getString();
            ins.close();
            os.close();
            if (body.contains("Couldn't find any subs with the specified"
                    + " language. Filter ignored"))
                return null;

            String thisName = thisProps.getName();
            int l2 = thisName.length();
            Pair<Integer, SortedSet<Integer>> thisSeasonEpisodes =
                thisProps.getSeasonEpisodes();

            List<Pair<SubtitleAttributes, SubtitleDescriptor>>
                    list = getAttDescList(link, thisProps, lang);

            SubtitleDescriptor bestMatchSI = null;
            Pair<SubtitleAttributes, SubtitleDescriptor> bestAttributes = null;

            for (Pair<SubtitleAttributes, SubtitleDescriptor> p : list) {
                SubtitleAttributes thatProps = p.getFst();
                String thatName = thatProps.getName();
                thatName = thatName.replace(":", "");
                Pair<Integer, SortedSet<Integer>> thatSeasonEpisodes =
                        thatProps.getSeasonEpisodes();

                if (!covers(thisSeasonEpisodes, thatSeasonEpisodes)) {
                    continue;
                }

                int l1 = thatName.length();
                int d = Distance.LD(thatName, thisName);
                int upperBound = Math.max(l1, l2);
                double perc = d * 1.0 / upperBound;
                //System.out.println((d == upperBound) + " " + perc);
                if (perc < 0.5 && d != upperBound) { // close enough
                    SubtitleAttributes s = bestAttributes == null ?
                        null : bestAttributes.getFst();
                    if (SubtitleAttributes.better(thatProps,
                            s, thisProps)) {
                        bestAttributes = p;
                        bestMatchSI = p.getSnd();
                    }
                }
            }

            return bestMatchSI;
        }

        @Override
        protected InputStream getDownloadInputStream(SubtitleDescriptor si)
                throws IOException {
            String downloadURL = si.getSubDownloadLink();
            currentMethod = new GetMethod(downloadURL);
            return getStream(this, client, currentMethod, si);
        }

        @Override
        protected void postDownloadSubtitle() throws IOException {
            super.postDownloadSubtitle();
            if (currentMethod != null) currentMethod.releaseConnection();
            currentMethod = null;
        }

    }

    private static InputStream getStream(SubtitleDownloader sd,
            HttpClient client, HttpMethod method,
            SubtitleDescriptor si) throws IOException {
        String downFileName = null;
        client.executeMethod(method);
        URI uri = method.getURI();
        if (uri.getPath().compareTo("/downloadexceeded.php") == 0) {
            sd.getLogger().warning(sd.getClass().getName() +
                    " download exceeded!");
            return null;
        }
        Header h = method.getResponseHeader("Content-Disposition");
        String v = h.getValue();
        Matcher m = Pattern.compile(".*filename=\\\"(.*)\\\"",
                Pattern.DOTALL).matcher(v);
        if (m.matches()) {
            downFileName = m.group(1).trim();
        }
        si.setDownFileName(downFileName);
        return method.getResponseBodyAsStream();
    }

    private static void login(HttpClient client, String user, String password)
            throws IOException {
        GetMethod getPage = new GetMethod(WEBURL);

        client.executeMethod(getPage);

        getPage.releaseConnection();

        PostMethod postLogin = new PostMethod();
        postLogin.setURI(new URI(new URI(WEBURL + "/login.php"),
                "dologin.php"));

        NameValuePair[] data = {
            new NameValuePair("username", user),
            new NameValuePair("password", password)
        };

        postLogin.setRequestBody(data);

        client.executeMethod(postLogin);

        InputStream ins = postLogin.getResponseBodyAsStream();
        StringOutputStream os = new StringOutputStream();
        FileUtils.copy(ins, os);
        String response = os.getString();
        ins.close();
        os.close();
        //String response = postLogin.getResponseBodyAsString();

        postLogin.releaseConnection();

        if (response.contains("Wrong password"))
            throw new IOException("Cannot login. Incorrect password");
        else if (response.contains(
                "User <b>" + user + "</b> doesn't exist"))
            throw new IOException("Cannot login. Incorrect username");        
    }

    private static void logout(HttpClient client) throws IOException {
        GetMethod logoutPage = new GetMethod(WEBURL + "/logout.php");
        client.executeMethod(logoutPage);
        logoutPage.releaseConnection();
    }

    private static List<Pair<SubtitleAttributes, SubtitleDescriptor>>
                getAttDescList(String link, SubtitleAttributes sa,
                Language lang) throws IOException, MalformedURLException {
        List<Pair<SubtitleAttributes, SubtitleDescriptor>> res =
                new LinkedList<Pair<SubtitleAttributes,
                                        SubtitleDescriptor>>();
        URL url = new URL(link);
        /*Integer season = 1;
        SortedSet<Integer> set = new TreeSet<Integer>();
        set.add(10);*/
        String language = null;
        String quality = null;
        String ssource = null;
        String format = null;
        String group = null;
        String downLink = null;
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; " +
                "Windows NT 6.0; en-GB; rv:1.9.1.2) Gecko/20090729 " +
                "Firefox/3.5.2 (.NET CLR 3.5.30729)");
        Source source = new Source(conn.getInputStream());
        source.setLogger(null);
        List<StartTag> allStartTags = source.getAllStartTags("class",
                "tabel95", true);
        source.fullSequentialParse();
        Iterator<StartTag> it = allStartTags.iterator();
        if (it.hasNext()) {
            it.next();
        }
        if (it.hasNext()) {
            it.next();
        }
        while (it.hasNext()) {
            StartTag s = it.next();
            Element e = s.getElement();
            Element x = e.getFirstElement("class", "NewsTitle", true);
            List<Element> le = x.getAllElements("title",
                    Pattern.compile(".*"));
            for (Element el : le) {
                String str = el.getAttributeValue("title");
                if (str == null) {
                    continue;
                }
                if (str.contains("720")) {
                    quality = "720p";
                    break;
                } else if (str.contains("1080")) {
                    quality = "1080p";
                    break;
                }
            }
            Matcher m1 = Pattern.compile(".*Version([^\\,]*)\\,.*",
                    Pattern.DOTALL).matcher(x.toString());
            if (m1.matches()) {
                String g = m1.group(1).trim();
                g = StringUtils.strip(g);
                g = StringUtils.strip(g, "\u00A0");
                group = g.toLowerCase();
            }
            List<Element> y = e.getAllElements("class", "language", true);
            if (y == null) {
                continue;
            }
            for (Element elem : y) {
                Matcher m = Pattern.compile("<[^<>]*>([^<]*)<.*",
                        Pattern.DOTALL).matcher(elem.toString());
                if (m.matches()) {
                    String g = m.group(1).trim();
                    g = StringEscapeUtils.unescapeHtml(g);
                    g = StringUtils.strip(g);
                    g = StringUtils.strip(g, "\u00A0");
                    language = g.toLowerCase();
                    Language lfs = getLangFromString(language);
                    if (!lang.equals(Language.ALL) && 
                            (lfs == null || !lfs.equals(lang)))
                        continue;
                }
                Element z = elem.getParentElement();
                List<Element> el = z.getChildElements();
                boolean completed = true;
                for (Element a : el) {
                    String as = a.toString();
                    Matcher mas = Pattern.compile(
                        ".*(<strong>|<b>)\\s*.*%\\s*Completed\\s*(</strong>|</b>).*",
                            Pattern.DOTALL).matcher(as);
                    if (mas.matches()) {
                        completed = false;
                        break;
                    }
                    List<Element> lrefs = 
                            a.getAllElements("href", Pattern.compile(".*"));
                    for (Element xref : lrefs) {
                        String cont = xref.getContent().toString();
                        String ds = xref.getAttributeValue("href");
                        if (cont.contains("Download") ||
                                cont.contains("original") ||
                                cont.contains("most updated"))
                            downLink = WEBURL + ds;
                    }
                }
                if (!completed) continue;
                String[] regexs = {
                "([^\\p{Punct}]+)\\.([^\\p{Punct}]+)[\\.-]([^\\p{Punct}]+)",
                "([^\\p{Punct}]+)"
                };
                Matcher mx = null;
                int i = 0;
                for (String regex : regexs) {
                    mx = Pattern.compile(regex).matcher(group);
                    if (mx.matches()) {
                        break;
                    }
                    mx = null;
                    i++;
                }
                if (mx != null) {
                    switch (i) {
                        case 0:
                            {
                                ssource = mx.group(1);
                                format = mx.group(2);
                                group = mx.group(3);
                                break;
                            }
                        case 1:
                            group = mx.group(1);
                            break;
                        default:
                            group = null;
                            break;
                    }
                    if (ssource != null && (ssource.equals("720p") ||
                            ssource.equals("1080p"))) {
                        quality = ssource;
                        ssource = format;
                        format = null;
                    }
                }
                SubtitleAttributes sat = new SubtitleAttributes(
                        sa.getName(), sa.getSeason(), sa.getEpisodes());
                sat.setQuality(quality);
                sat.setSource(ssource);
                sat.setFormat(format);
                sat.setGroup(group);
                SubtitleDescriptor sd =
                        new DefaultSubtitleDescriptor(null,
                        lang.getISO6391code());
                sd.setSubDownloadLink(downLink);
                res.add(new Pair<SubtitleAttributes,
                                    SubtitleDescriptor>(sat, sd));
                /*System.out.println("L: " + language
                + " Q: " + quality
                + " S: " + ssource
                + " F: " + format
                + " G: " + group
                + " D: " + downLink);*/
            }
            if (it.hasNext()) {
                it.next();
            }
        }

        return res;
    }
}
