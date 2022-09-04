package pt.unl.fct.di.tsantos.util.download.subtitile;

import com.sun.syndication.feed.synd.SyndEntry;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import pt.unl.fct.di.tsantos.util.Pair;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedFormatException;
import pt.unl.fct.di.tsantos.util.net.RSSFeed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pt.unl.fct.di.tsantos.util.download.subtitile.Language.*;

public class LegendasTVDownloader extends RSSSubtitleDownloader {

    public static final String WEBURL = "http://legendas.tv";

    public static final RSSFeed RSSUL =
            RSSFeed.getRSSFeedSilent(WEBURL + "/rss.html",
                                        RSSFeed.Type.ORIGINAL);
    public static final RSSFeed RSSUD =
            RSSFeed.getRSSFeedSilent(WEBURL + "/rss-destaques.html",
                                        RSSFeed.Type.ORIGINAL);
    public static final RSSFeed RSSUDS =
            RSSFeed.getRSSFeedSilent(WEBURL + "/rss-destaques-series.html",
                                        RSSFeed.Type.ORIGINAL);
    public static final RSSFeed RSSUDF =
            RSSFeed.getRSSFeedSilent(WEBURL + "/rss-destaques-filmes.html",
                                        RSSFeed.Type.ORIGINAL);

    protected final HttpClient client;
    protected HttpUriRequest currentMethod;

    private static final Language[] supportedLangs = {Language.ALL,
        PB, EN, ES, FR, DE, JA, DA, NO,
        SV, PT, AR, CS, ZH, KO, BG, IT, PL
    };

    protected Language getLangFromString(String lang) {
        if (lang.compareTo("Português-BR") == 0) return PB;
        else if (lang.compareTo("Inglês") == 0) return EN;
        else if (lang.compareTo("Espanhol") == 0) return ES;
        else if (lang.compareTo("Francês") == 0) return FR;
        else if (lang.compareTo("Alemão") == 0) return DE;
        else if (lang.compareTo("Japonês") == 0) return JA;
        else if (lang.compareTo("Dinamarquês") == 0) return DA;
        else if (lang.compareTo("Norueguês") == 0) return NO;
        else if (lang.compareTo("Sueco") == 0) return SV;
        else if (lang.compareTo("Português-PT") == 0) return PT;
        else if (lang.compareTo("Árabe") == 0) return AR;
        else if (lang.compareTo("Checo") == 0) return CS;
        else if (lang.compareTo("Chinês") == 0) return ZH;
        else if (lang.compareTo("Coreano") == 0) return KO;
        else if (lang.compareTo("Búlgaro") == 0) return BG;
        else if (lang.compareTo("Italiano") == 0) return IT;
        else if (lang.compareTo("Polonês") == 0) return PL;
        else return null;
    }

    public LegendasTVDownloader(List<String> fileNames, File saveDirectory,
            String user, String password) {
        super(fileNames, saveDirectory, user, password);
        this.client = new DefaultHttpClient();
    }

    public LegendasTVDownloader(List<String> fileNames, Set<Language> langs,
            File saveDirectory, String user, String password) {
        super(fileNames, langs, saveDirectory, user, password);
        this.client = new DefaultHttpClient();
    }

    @Override
    protected void login() throws IOException {
        HttpGet getPage = new HttpGet(WEBURL);

        client.execute(getPage);

        getPage.abort();//releaseConnection();

        HttpPost postLogin = new HttpPost();
        postLogin.setURI(URI.create(WEBURL + "/login_verificar.php"));

        /*NameValuePair[] data = {
            new BasicNameValuePair("txtLogin", user),
            new BasicNameValuePair("txtSenha", password)
        };*/
        
        List<NameValuePair> list = new LinkedList<NameValuePair>();
        list.add(new BasicNameValuePair("txtLogin", user));
        list.add(new BasicNameValuePair("txtSenha", password));
        
        UrlEncodedFormEntity newentity = new UrlEncodedFormEntity(list);

        postLogin.setEntity(newentity);
        //postLogin.setRequestBody(data);

        client.execute(postLogin);
        
        HttpResponse response = client.execute(postLogin);

        /*InputStream ins = postLogin.getResponseBodyAsStream();
        StringOutputStream os = new StringOutputStream();
        FileUtils.copy(ins, os);
        String response = os.getString();
        ins.close();
        os.close();

        postLogin.releaseConnection();*/
        
        HttpEntity entity = response.getEntity();
        String answer = EntityUtils.toString(entity);

        //postLogin.releaseConnection();
        postLogin.abort();

        if (answer.contains("Dados incorretos!"))
            throw new IOException("Cannot login. " +
                    "Incorrect username or password");
    }

    @Override
    protected void logout() throws IOException {
        HttpGet logoutPage = new HttpGet(WEBURL + "/logoff.php");
        client.execute(logoutPage);
        //logoutPage.releaseConnection();
        logoutPage.abort();
    }

    @Override
    protected RSSFeed getSupportedRSSFeed(Language lang) {
        return RSSUDS;
    }

    @Override
    protected List<Pair<SubtitleAttributes, SubtitleDescriptor>>
            getInfoFromSyndEntry(SyndEntry se, Language lang)
            throws UnsupportedFormatException {
        String title = se.getTitle();
        List<Pair<SubtitleAttributes, SubtitleDescriptor>> res =
                new LinkedList<Pair<SubtitleAttributes, SubtitleDescriptor>>();
        res.add(Pair.sndNull(SubtitleAttributes.getProperties(title),
                                SubtitleDescriptor.class));
        return res;
    }

    @Override
    protected SyndEntry filterSyndEntry(SubtitleAttributes sa,
            SyndEntry se, Language lang) {
        try {
            SubtitleAttributes sase = 
                    SubtitleAttributes.getProperties(se.getTitle());
            if (SubtitleAttributes.getBaseSimilarity(sa, sase) <= 0.5f)
                return null;
            
            SyndEntry res = se;
            String url = se.getLink();
            HttpGet method = new HttpGet(url);
            HttpResponse response = client.execute(method);
            HttpEntity entity = response.getEntity();
            //InputStream body = method.getResponseBodyAsStream();
            InputStream body = entity.getContent();            
            Source source = new Source(body);
            Element el = source.getFirstElement("class", "infoadicional", true);
            Element x = el.getFirstElement("class", "infolegenda", true);
            String c = x.getContent().toString();
            Matcher m = Pattern.compile("([^<]+)<.*",
                    Pattern.DOTALL).matcher(c);
            if (m.matches()) {
                String langi = m.group(1).trim();
                langi = StringEscapeUtils.unescapeHtml(langi);
                langi = StringUtils.strip(langi);
                langi = StringUtils.strip(langi, "\u00A0");
                //System.out.println(langi);
                Language language = getLangFromString(langi);
                if (language != null && !lang.equals(Language.ALL)
                        && !language.equals(lang)) {
                    res = null;
                }
            }
            //method.releaseConnection();
            method.abort();
            return res;
        } catch (IOException ex) {
        } catch (UnsupportedFormatException ex) {}
        return se;
    }

    @Override
    protected SubtitleDescriptor getSubtitleDescriptor(
            Pair<SubtitleAttributes, SubtitleDescriptor> pair,
            SyndEntry entry, Language lang)
        throws IOException {
        SubtitleDescriptor si = pair.getSnd();
        String name = entry.getTitle();
        String url = entry.getLink();
        String downURL = url + "&c=1";
        if (name != null && name.isEmpty()) name = null;        
        if (name == null) return null;

        if (si == null)
            si = new DefaultSubtitleDescriptor(name, lang.getISO6391code());
        else {
            si.setSubFileName(name);
            si.setISO639(lang.getISO6391code());
        }
        
        si.setSubDownloadLink(downURL);
        return si;
    }

    @Override
    protected Language[] initSupportedLanguages() {
        return supportedLangs;
    }

    protected InputStream getDownloadInputStream(SubtitleDescriptor si)
            throws IOException {
        String downloadURL = si.getSubDownloadLink();
        String downFileName = null;
        currentMethod = new HttpGet(downloadURL);
        HttpResponse response = client.execute(currentMethod);
        HttpEntity entity = response.getEntity();
        String uri = currentMethod.getURI().toString();
        int index = uri.lastIndexOf("/");
        if (index >= 0) downFileName = uri.substring(index + 1);
        si.setDownFileName(downFileName);
        return entity.getContent();
    }

    @Override
    protected void postDownloadSubtitle() throws IOException {
        super.postDownloadSubtitle();
        //if (currentMethod != null) currentMethod.releaseConnection();
        if (currentMethod != null) currentMethod.abort();
        currentMethod = null;
    }
    
    @Override
    protected List<SubtitleAttributes> getInfoFromDownloadedFile(
            String fileName) throws UnsupportedFormatException
    {
        List<SubtitleAttributes> res = new LinkedList<SubtitleAttributes>();
        res.add(SubtitleAttributes.getProperties(fileName));
        return res;
    }
}
