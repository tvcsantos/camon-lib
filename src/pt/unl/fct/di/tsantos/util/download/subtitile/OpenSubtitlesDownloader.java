package pt.unl.fct.di.tsantos.util.download.subtitile;

import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedFormatException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import pt.unl.fct.di.tsantos.util.Pair;
import pt.unl.fct.di.tsantos.util.math.Distance;
import redstone.xmlrpc.XmlRpcFault;

public class OpenSubtitlesDownloader extends SubtitleDownloader {

    protected Map<String, String> map;
    protected final Map<String, String> bestMatches;
    protected OpenSubtitlesXmlRpc os;

    public OpenSubtitlesDownloader(List<String> fileNames, 
            Set<Language> langs, File saveDirectory,
            Map<String, String> map) {
        super(fileNames, langs, saveDirectory);
        this.map = map;
        this.bestMatches = new HashMap<String, String>();
        os = new OpenSubtitlesXmlRpc("Subdownloader 2.0.9.3");
    }

    public OpenSubtitlesDownloader(List<String> fileNames,
            File saveDirectory, Map<String, String> map) {
        super(fileNames, saveDirectory);
        this.map = map;
        this.bestMatches = new HashMap<String, String>();
        os = new OpenSubtitlesXmlRpc("Subdownloader 2.0.9.3");
    }

    @Override
    public void login() throws IOException {
        try {
            os.loginAnonymous();
        } catch (XmlRpcFault ex) {}
    }

    @Override
    public void logout() throws IOException {
        try {
            os.logout();
        } catch (XmlRpcFault ex) {}
    }

    private void addBestMatch(String name, String match) {
        synchronized(bestMatches) {
            bestMatches.put(name, match);
        }
    }

    private String getMapBestMatch(String name) {
        synchronized(bestMatches) {
            return bestMatches.get(name);
        }
    }

    private String getBestMatch(String name) {
        String res = getMapBestMatch(name);
        //System.out.println("NAME: " + name);
        if (res != null) {
            //System.out.println("BEST: " + res);
            return map.get(res);
        }
        //System.out.println("CHECKING KEYS");
        // get map keys from shows.xml
        Set<String> keys = map.keySet();
        String bestMatch = null;
        int distance = Integer.MAX_VALUE;
        int l2 = name.length();
        for (String match : keys) { //matches
            //System.out.println(match);
            String rep = match.replace(":","");
            int l1 = rep.length();
            int d = Distance.LD(
                    rep, name);
            int upperBound = Math.max(l1, l2);
            double perc = d*1.0/upperBound;
            //System.out.println((d == upperBound) + " " + perc);
            if (perc < 0.5 && d != upperBound && d < distance) {
                distance = d;
                bestMatch = match;
            }
        }
        //System.out.println();
        //System.out.println("BEST: " + bestMatch);
        //System.out.println();
        if (bestMatch != null) addBestMatch(name, bestMatch);
        return bestMatch == null ? null : map.get(bestMatch);
    }

    protected SubtitleDescriptor searchSubtitle(String fileName,
            SubtitleAttributes thisProps, Language lang) throws IOException {
        try {
            String thisName = thisProps.getName();
            Pair<Integer, SortedSet<Integer>> thisSeasonEpisodes =
                thisProps.getSeasonEpisodes();
            int l2 = thisName.length();
            String imdb = getBestMatch(thisName);
            if (imdb == null) {
                return null;
            }
            List<OpenSubtitlesSubtitleDescriptor> ossil =
                    os.searchSubtitles(Integer.parseInt(imdb),
                    lang.getISO6392code());
            SubtitleDescriptor bestMatchSI = null;
            SubtitleAttributes bestAttributes = null;
            for (OpenSubtitlesSubtitleDescriptor ossi : ossil) {
                String subFileName = ossi.getName();
                List<SubtitleAttributes> thatListProps =
                        new LinkedList<SubtitleAttributes>();
                try {
                    thatListProps = getInfoFromDownloadedFile(subFileName);
                } catch (UnsupportedFormatException ex) {
                    logger.warning(ex.toString());
                    continue;
                }
                for (SubtitleAttributes thatProps : thatListProps) {
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
                        if (SubtitleAttributes.better(thatProps,
                                bestAttributes, thisProps)) {
                            bestAttributes = thatProps;
                            bestMatchSI = ossi;
                        }
                    }
                }
            }
            return bestMatchSI;
        } catch (XmlRpcFault ex) {
            throw warp(ex);
        }
    }

    @Override
    protected Language[] initSupportedLanguages() {
        return supportedLangs;
    }

    private static final Language[] supportedLangs = loadSupportedLanguages();

    @Override
    protected InputStream getDownloadInputStream(
            SubtitleDescriptor si)
            throws IOException {
        URL resource = new URL(si.getSubDownloadLink());
        InputStream stream = new GZIPInputStream(resource.openStream());
        if (si.getDownFileName() == null)
            si.setDownFileName(si.getSubFileName());
        return stream;
    }

    @Override
    protected List<SubtitleAttributes> getInfoFromDownloadedFile(
            String fileName) throws UnsupportedFormatException
    {
        List<SubtitleAttributes> res = new LinkedList<SubtitleAttributes>();
        res.add(SubtitleAttributes.getProperties(fileName));
        return res;
    }

    private static Map<String, Language> loadLanguages() {
        Map<String, Language> result = new HashMap<String, Language>();
        result.put(null, Language.ALL); // special value
        InputStream is = OpenSubtitlesDownloader.class.getResourceAsStream(
                "languages.txt");
        if (is == null) return result;
        Scanner sc = new Scanner(is);
        int count = 0;
        Map<String, Integer> map = new HashMap<String,Integer>();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (++count >= 2) {
                StringTokenizer st = new StringTokenizer(line,"\t\r\n");
                int tokens = st.countTokens();
                if (tokens < 4) continue;
                String code2 = st.nextToken().trim();
                String code2U = code2.toUpperCase();
                String code1 = null;
                boolean upEnabled = false;
                boolean webEnabled = false;
                if (tokens == 4) {
                    code1 = null;
                } else {
                    code1 = st.nextToken().trim();
                }
                String name = st.nextToken().trim();
                Integer a = Integer.parseInt(st.nextToken().trim());
                if (a.intValue() == 1) upEnabled = true;
                a = Integer.parseInt(st.nextToken().trim());
                if (a.intValue() == 1) webEnabled = true;
                if (code1 != null && code1.isEmpty()) code1 = null;
                if (!upEnabled || !webEnabled) continue;
                Integer value = map.get(code2U);
                if (value == null) value = new Integer(0);
                map.put(code2U, ++value);
                result.put(code2U + (value > 1 ? value : "")
                        ,new Language(name, code2, code1));
            }
        }
        return result;
    }

    private static Language[] loadSupportedLanguages() {
        Collection<Language> coll = loadLanguages().values();
        return (Language[]) coll.toArray(new Language[coll.size()]);
    }
}
