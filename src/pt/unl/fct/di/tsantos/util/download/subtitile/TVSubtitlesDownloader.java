package pt.unl.fct.di.tsantos.util.download.subtitile;

import java.net.URI;
import java.util.Set;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedFormatException;
import com.sun.syndication.feed.synd.SyndEntry;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pt.unl.fct.di.tsantos.util.Pair;
import pt.unl.fct.di.tsantos.util.net.RSSFeed;
import static pt.unl.fct.di.tsantos.util.download.subtitile.Language.*;

public class TVSubtitlesDownloader extends RSSSubtitleDownloader {

    private static final String WEBURL = "http://www.tvsubtitles.net";

    private static final Language[] supportedLangs = { Language.ALL,
        EN, ES, FR, DE, PB, RU, UK, IT,
        EL, AR, HU, PL, TR, NL, PT, SV,
        DA, FI, KO, ZH, JA, BG, CS, RO
    };

    public TVSubtitlesDownloader(List<String> fileNames, Set<Language> langs,
            File saveDirectory) {
        super(fileNames, langs, saveDirectory);
    }

    public TVSubtitlesDownloader(List<String> fileNames, File saveDirectory) {
        super(fileNames, saveDirectory);
    }

    @Override
    protected Language[] initSupportedLanguages() {
        return supportedLangs;
    }

    @Override
    protected RSSFeed getSupportedRSSFeed(Language lang) {
        String suffix = lang.getISO6391code();
        if (lang.equals(PB)) suffix = "br";
        else if (lang.equals(UK)) suffix = "ua";
        else if (lang.equals(EL)) suffix = "gr";
        else if (lang.equals(ZH)) suffix = "cn";
        else if (lang.equals(JA)) suffix = "jp";
        else if (lang.equals(CS)) suffix = "cz";
        return RSSFeed.getRSSFeedSilent(WEBURL + "/rss" +
                    suffix + ".xml",
                    RSSFeed.Type.ORIGINAL);
    }

    protected Language getLanguageFromCode(String code) {
        if (code.compareTo("br") == 0) return PB;
        else if (code.compareTo("ua") == 0) return UK;
        else if (code.compareTo("gr") == 0) return EL;
        else if (code.compareTo("cn") == 0) return ZH;
        else if (code.compareTo("jp") == 0) return JA;
        else if (code.compareTo("cz") == 0) return CS;
        else {
            for (Language l : langs) {
                if (l.getISO6391code().compareTo(code) == 0)
                    return l;
            }
        }
        return null;
    }

    @Override
    protected List<Pair<SubtitleAttributes, SubtitleDescriptor>>
            getInfoFromSyndEntry(SyndEntry se, Language lang)
            throws UnsupportedFormatException {
        List<Pair<SubtitleAttributes, SubtitleDescriptor>> res =
                new LinkedList<Pair<SubtitleAttributes, SubtitleDescriptor>>();

        //if (filterSyndEntry(se, lang) == null) return res;

        String title = se.getTitle();
        Matcher m1 = Pattern.compile(
                "(.+) (\\d+)x(\\d+) (.*)",
                Pattern.CASE_INSENSITIVE).matcher(title.toLowerCase());
        String name = null;
        Integer season = null;
        SortedSet<Integer> eps = null;
        if (m1.matches()) {
            name = m1.group(1).trim();
            season = Integer.parseInt(m1.group(2));
            int episode = Integer.parseInt(m1.group(3));
            eps = new TreeSet<Integer>();
            eps.add(episode);
        } else throw new UnsupportedFormatException(title);
        String desc = se.getDescription().getValue();
        Matcher m2 = Pattern.compile(".*Rip:(.+)<.*Release:(.+)<.*",
                Pattern.CASE_INSENSITIVE |
                Pattern.DOTALL).matcher(desc);
        Matcher m3 = Pattern.compile(".*Rip:(.+)<.*",
                Pattern.CASE_INSENSITIVE |
                Pattern.DOTALL).matcher(desc);
        String source = null;
        String group = null;
        String quality = null;
        if (m2.matches()) {
            String x = m2.group(1).toLowerCase();
            StringTokenizer st = new StringTokenizer(x, " ");
            if (st.countTokens() > 1) {
                quality = st.nextToken().trim();
                source = st.nextToken().trim();
            } else {
                source = st.nextToken().trim();
            }
            group = m2.group(2).toLowerCase();
            group = group.replace("480i", "").
                    replace("576i", "").replace("576p", "").
                    replace("720p", "").replace("1080i", "").
                    replace("1080p", "").trim();
        } else if (m3.matches()) {
            String x = m3.group(1).toLowerCase();
            StringTokenizer st = new StringTokenizer(x, " ");
            if (st.countTokens() > 1) {
                quality = st.nextToken().trim();
                source = st.nextToken().trim();
            } else {
                source = st.nextToken().trim();
            }
        }

        res.add(Pair.sndNull(new SubtitleAttributes(name, season, eps,
                source, null, quality, group), SubtitleDescriptor.class));
        return res;
    }

    @Override
    protected SyndEntry filterSyndEntry(SubtitleAttributes sa,
            SyndEntry se, Language lang) {
        if (lang.equals(Language.ALL)) return se;
        String title = se.getTitle();
        Matcher m = Pattern.compile(
                "(.+) (\\d+)x(\\d+) (.*)",
                Pattern.CASE_INSENSITIVE).matcher(title.toLowerCase());
        if (m.matches()) {
            String code = m.group(4).trim();
            Language l = getLanguageFromCode(code);
            if (l != null && !lang.equals(l)) return null;
        }
        //System.out.println(lang);
        return se;
    }

    @Override
    protected SubtitleDescriptor getSubtitleDescriptor(
            Pair<SubtitleAttributes, SubtitleDescriptor> pair, 
            SyndEntry entry, Language lang) throws IOException {
        SubtitleDescriptor si = pair.getSnd();
        String link = entry.getLink();
        int i = link.lastIndexOf("subtitle");
        String fst = link.substring(0, i);
        String snd = link.substring(i).replace("subtitle", "download");
        String downloadURL = fst + snd;

        URL url = new URL(downloadURL);
        URLConnection conn = url.openConnection();
        //InputStream iss = null;
        String s = null;
        try {
            InputStream iss = conn.getInputStream(); // force redirection
            s = conn.getURL().toString();
            iss.close();
        }catch(FileNotFoundException e) {
            s = e.getMessage();
            // hack to repair ill-formed file url
            // e.g. http://.../files/Greys Anatomy_7x12_HDTV.LOL.pt.zip
            // should be http://.../files/Greys%20Anatomy_7x12_HDTV.LOL.pt.zip
        }
        downloadURL = s;
        //downloadURL = new URI(downloadURL, false).toString();
        downloadURL = URI.create(downloadURL).toString();
        //if (iss != null) iss.close();
        int j = s.lastIndexOf('/');
        String downFileName = s.substring(j + 1);

        int k = downFileName.lastIndexOf('.');
        String subFileName = downFileName.substring(0, k); // removing extension
        //String subHash = MD5Checksum.getMD5Checksum(is);
        String subHash = null;

        if (si == null)
            si = new DefaultSubtitleDescriptor(subFileName,
                lang.getISO6391code());
        else {
            si.setSubFileName(subFileName);
            si.setISO639(lang.getISO6391code());
        }
        
        si.setSubHash(subHash);
        si.setSubLink(link);
        si.setSubDownloadLink(downloadURL);
        return si;
    }
    
    @Override
    protected InputStream getDownloadInputStream(
            SubtitleDescriptor si)
            throws IOException {
        String downloadURL = si.getSubDownloadLink();
        URLConnection conn = new URL(downloadURL).openConnection();
        InputStream ris = conn.getInputStream();
        String s = conn.getURL().toString();
        s = URLDecoder.decode(s,"UTF-8");
        int j = s.lastIndexOf('/');
        String downFileName = s.substring(j + 1);
        //j = downFileName.lastIndexOf('.');
        si.setDownFileName(downFileName);
        return ris;
    }
    
    @Override
    protected List<SubtitleAttributes> getInfoFromDownloadedFile(
            String fileName) throws UnsupportedFormatException
    {
        String nname = SubtitleAttributes.getName(fileName);
        Pair<Integer, SortedSet<Integer>> thisPair =
                SubtitleAttributes.getSeasonAndEpisodes(fileName);

        String nameLower = fileName.toLowerCase();
        // _rip.release.lang release = format-group
        Matcher m1 = Pattern.compile(
                ".*_([^\\p{Punct}]*)\\.([^\\p{Punct}]*)-([^\\p{Punct}]*)" +
                "\\.([^\\p{Punct}]*)",
                Pattern.CASE_INSENSITIVE).matcher(nameLower);
        // _rip.release.lang
        Matcher m2 = Pattern.compile(
                ".*_([^\\p{Punct}]*)\\.([^\\p{Punct}]*)" +
                "\\.([^\\p{Punct}]*)",
                Pattern.CASE_INSENSITIVE).matcher(nameLower);

        String[] qualities = { 
            "480i", "576i", "576p",
            "720p", "1080i", "1080p"
        };

        String quality = null;
        String source = null;
        String format = null;
        String group = null;
        if (m1.matches()) {
            source = m1.group(1).trim();
            format = m1.group(2).trim();
            group = m1.group(3).trim();
        } else if (m2.matches()) {
            source = m1.group(1).trim();
            group = m1.group(2).trim();
        }

        for (String qual : qualities) {
            if (source.contains(qual)) {
                source = source.replace(qual, "");
                quality = qual;
            }
        }
        source = source.trim();
        List<SubtitleAttributes> res = new LinkedList<SubtitleAttributes>();
        res.add(new SubtitleAttributes(nname, thisPair.getFst(),
                thisPair.getSnd(), source, format, quality, group));
        return res;
    }

    @Override
    protected void login() throws IOException {
        // this downloader doesn't need authentication
    }

    @Override
    protected void logout() throws IOException {
        // this downloader doesn't need authentication
    }
}
