package pt.unl.fct.di.tsantos.util.imdb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;
import org.apache.commons.lang.StringEscapeUtils;
import pt.unl.fct.di.tsantos.util.Pair;

public class IMDB {

    private IMDB() {
        throw new UnsupportedOperationException();
    }

    public static String getIMDBID(String search)
            throws UnsupportedEncodingException,
            MalformedURLException, IOException {
        if (search == null) {
            return null;
        }
        String imdb = null;
        search = search.replace(":", "");
        search = URLEncoder.encode(search, "UTF-8");
        //System.out.println(search);
        URL url = new URL("http://www.imdb.com/find?s=tt&q="
                + search);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows; U; "
                + "Windows NT 6.0; en-GB; rv:1.9.1.2) "
                + "Gecko/20090729 "
                + "Firefox/3.5.2 (.NET CLR 3.5.30729)");
        Source source = new Source(conn.getInputStream());
        source.setLogger(null);
        String x = source.toString();
        int l = x.indexOf("div id=\"main\"");
        if (l >= 0) {
            x = x.substring(l);
            Pattern pattern2 = Pattern.compile("tt\\d{7}");
            Matcher matcher = pattern2.matcher(x);
            if (matcher.find()) {
                /*System.out.println(*/imdb = matcher.group(0)/*)*/;
            }
        } else {
            l = x.indexOf("<head>");
            if (l >= 0) {
                x = x.substring(l);
                Pattern pattern2 = Pattern.compile("tt\\d{7}");
                Matcher matcher = pattern2.matcher(x);
                if (matcher.find()) {
                    /*System.out.println(*/imdb = matcher.group(0)/*)*/;
                }
            }
        }
        if (imdb != null) {
            imdb = imdb.substring(2);
        }
        return imdb;
    }

    public static Pair<String, String>
            getOriginalTitle(String imdbLink) throws IOException {
        URL url = new URL(imdbLink);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; " +
                "Windows NT 6.0; en-GB; rv:1.9.1.2) Gecko/20090729 " +
                "Firefox/3.5.2 (.NET CLR 3.5.30729)");
        Source source = new Source(conn.getInputStream());
        source.setLogger(null);
        List<StartTag> allStartTags =
                source.getAllStartTags("h1 class=\"header\"");
        for (StartTag s : allStartTags) {
            if (s == null) continue;
            Element e = s.getElement();
            if (e == null) continue;
            String str = e.toString();
            Matcher m1 = Pattern.compile("<h1 class=\"header\">([^<]*)<.*",
                    Pattern.DOTALL).matcher(str);
            String title = null;
            if (m1.matches()) {
                title = m1.group(1);
                title = title != null ? title.trim() : title;
            }
            List<StartTag> ls = e.getAllStartTags("span class=\"title-extra\"");
            if (!ls.isEmpty()) {
                StartTag st = ls.get(0);
                if (st != null) {
                    Element e1 = st.getElement();
                    if (e1 != null) {
                        String orig = e1.getContent().toString();
                        int index = orig.indexOf("<i>(original title)</i>");
                        if (index < 0) index = orig.indexOf("(original title)");
                        if (index < 0) title = orig.trim();
                        else title = orig.substring(0, index).trim();
                    }
                }
            }
            if (title != null) {
                ls = e.getAllStartTags("span>");
                if (!ls.isEmpty()) {
                    StartTag st = ls.get(0);
                    e = st.getElement();
                    String cont = e.getContent().toString();
                    m1 = Pattern.compile("\\(<a[^>]*>([^<]*)<.*\\)",
                        Pattern.DOTALL).matcher(cont);
                    Matcher m2 = Pattern.compile("\\((.*)\\)",
                            Pattern.DOTALL).matcher(cont);
                    String year = null;
                    if (m1.matches()) {
                        year = m1.group(1);
                    } else if (m2.matches()) {
                        year = m2.group(1);
                    }
                    year = year != null ? year.trim() : year;
                    return new Pair<String, String>(title, year);
                }
            }
        }
        return null;
    }

    public static String getPoster(String imdbLink) throws IOException {
        URL url = new URL(imdbLink);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; " +
                "Windows NT 6.0; en-GB; rv:1.9.1.2) Gecko/20090729 " +
                "Firefox/3.5.2 (.NET CLR 3.5.30729)");
        Source source = new Source(conn.getInputStream());
        source.setLogger(null);
        List<Element> allElements =
                source.getAllElements("id", "img_primary", true);
        if (allElements.isEmpty()) return null;
        Element e = allElements.get(0);
        List<Element> list = e.getAllElements("img");
        if (list.isEmpty()) return null;
        e = list.get(0);
        String link = e.getAttributeValue("src");
        return link == null ? link : link.trim();
    }
    
    public static String[] getOutPlot250(String imdbLink) 
            throws MalformedURLException, IOException {
        URL url = new URL(imdbLink);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; " +
                "Windows NT 6.0; en-GB; rv:1.9.1.2) Gecko/20090729 " +
                "Firefox/3.5.2 (.NET CLR 3.5.30729)");
        Source source = new Source(conn.getInputStream());
        source.setLogger(null);

        String imdbtt = null;

        Matcher m = Pattern.compile(".+(tt\\d{7}).*").matcher(imdbLink);
        if (m.matches()) imdbtt = m.group(1);

        String outline = null;
        String plot = null;
        String top250 = null;
        
        Element e = source.getFirstElement("itemprop", "description", false);
        outline = e.getContent().toString().trim();
        outline = StringEscapeUtils.unescapeHtml(outline);

        List<Element> list = source.getAllElements("h2");
        e = null;
        for (Element x : list) {
            String content = x.getContent().toString().trim();
            if (content.equalsIgnoreCase("storyline")) {
                e = x;
                break;
            }
        }
        if (e != null) {
            EndTag et = e.getEndTag();
            Tag t = et.getNextTag();
            e = t.getElement();
            Segment s = e.getContent();
            StartTag st = s.getFirstStartTag();
            int end = st.getBegin();
            int start = s.getBegin();
            plot = source.subSequence(start, end).toString().trim();
            plot = StringEscapeUtils.unescapeHtml(plot);
        }

        if (imdbtt != null) {
            String link = "http://www.imdb.com/chart/top?" + imdbtt;
            e = source.getFirstElement("href", link, false);
            if (e != null) {
                Segment s = e.getContent();
                m = Pattern.compile("\\D*Top\\s*250\\s*#(\\d+)\\D*").
                        matcher(s.toString());
                if (m.matches()) top250 = m.group(1).trim();
            }
        }
        
        return new String[]{ outline, plot, top250 };
    }
}
