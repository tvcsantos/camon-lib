package pt.unl.fct.di.tsantos.util.thetvdb;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.sourceforge.tuned.FileUtilities;
import net.sourceforge.tuned.FilterIterator;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pt.unl.fct.di.tsantos.util.collection.CollectionUtilities;
import pt.unl.fct.di.tsantos.util.download.subtitile.SubtitleAttributes;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedFormatException;
import pt.unl.fct.di.tsantos.util.xml.XMLDocumentWriter;
import pt.unl.fct.di.tsantos.util.xml.XMLUtilities;
import static pt.unl.fct.di.tsantos.util.xml.XMLUtilities.*;

public class TheTVDBSearch {

    private static String API_KEY = "2805AD2873519EC5";

    private static final Logger logger =
            Logger.getLogger(TheTVDBSearch.class.getName());

    public static Logger getLogger() {
        return logger;
    }

    private TheTVDBSearch() {
        throw new UnsupportedOperationException();
    }

    /* List<TheTVDBTVShow> where TheTVDBTVShow is represented by a map
     * Map<String, String> where the key correponds to a member of a tvshow,
     * and the value to the respective information associated to that member
     */
    public static List<Map<String, String>> search(String seriesName)
            throws HttpException, IOException, ParserConfigurationException,
            SAXException, XPathExpressionException {
        if (seriesName == null)
            throw new NullPointerException("seriesName must be non null");
        if (seriesName.isEmpty())
            throw new IllegalArgumentException("seriesName must be non empty");
        seriesName = seriesName.replaceAll(" ", "+");
        String request = "http://www.thetvdb.com/api/GetSeries.php?seriesname="
                + seriesName;
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(request);

        // Send GET request
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            //System.err.println("Method failed: " + method.getStatusLine());
            throw new HttpException("Method failed with status " + statusCode);
        }
        
        InputStream rstream = null;

        // Get the response body
        rstream = method.getResponseBodyAsStream();

        // Process response
        Document response = DocumentBuilderFactory.newInstance().
                                    newDocumentBuilder().parse(rstream);

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();

        //Get all search Result nodes
        NodeList nodes =
            (NodeList) xPath.evaluate("/Data/Series",
                                        response, XPathConstants.NODESET);
        int nodeCount = nodes.getLength();

        List<Map<String, String>> l = new ArrayList<Map<String, String>>();
        
        //iterate over search Result nodes
        for (int i = 0; i < nodeCount; i++) {
            Node n = nodes.item(i);

            NodeList childs = n.getChildNodes();
            Map<String, String> content = new HashMap<String, String>();
            for (int j = 0; j < childs.getLength(); j++) {
                Node nc = childs.item(j);
                //System.out.println(nc.getNodeType());
                if (nc.getNodeType() == 3) continue;
                //System.out.print(nc.getNodeName() + " : ");
                //System.out.println(nc.getTextContent());
                content.put(nc.getNodeName(), nc.getTextContent());
            }
            
            l.add(content);
        }

        rstream.close();

        return l;
    }

    public static List<Map<String, String>>
            getSeriesInformation(String seriesID, String language)
         throws HttpException, IOException, ParserConfigurationException,
            SAXException, XPathExpressionException {
        if (seriesID == null)
            throw new NullPointerException("seriesName must be non null");
        if (seriesID.isEmpty())
            throw new IllegalArgumentException("seriesName must be non empty");
        
        String request = "http://www.thetvdb.com/api/" + API_KEY + "/series/"
                + seriesID + "/all/" + language + ".zip";
        
        //System.out.println(request);
        
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(request);

        // Send GET request
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            throw new HttpException("Method failed with status " + statusCode);
        }

        InputStream rstream = null;

        // Get the response body
        rstream = method.getResponseBodyAsStream();

        ZipInputStream zipis = new ZipInputStream(rstream);

        List<Map<String, String>> l = new ArrayList<Map<String, String>>();

        ZipEntry zipEntry = null;
        while ((zipEntry = zipis.getNextEntry()) != null) {
            if (!zipEntry.getName().equalsIgnoreCase(language + ".xml"))
                continue;

            // Process response
            Document response = DocumentBuilderFactory.newInstance().
                                        newDocumentBuilder().parse(zipis);

            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();

            //Get all search Result nodes
            NodeList nodes =
                (NodeList) xPath.evaluate("/Data/Episode",
                                            response, XPathConstants.NODESET);
            int nodeCount = nodes.getLength();

            //iterate over search Result nodes
            for (int i = 0; i < nodeCount; i++) {
                Node n = nodes.item(i);

                NodeList childs = n.getChildNodes();
                Map<String, String> content = new HashMap<String, String>();
                for (int j = 0; j < childs.getLength(); j++) {
                    Node nc = childs.item(j);
                    if (nc.getNodeType() == 3) continue;
                    content.put(nc.getNodeName(), nc.getTextContent());
                }

                l.add(content);
            }

            break;
        }

        zipis.close();

        return l;
    }

    private static String getTabulation(int tabCount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tabCount; i++) sb.append("\t");
        return sb.toString();
    }

    public static void saveXMLInfoDirs(Collection<File> dirs,
            FileFilter filter,
            File save, String ext, boolean recursive, boolean forced)
            throws UnsupportedFormatException, HttpException, IOException,
            ParserConfigurationException, SAXException,
            XPathExpressionException, XMLStreamException {
        List<File> files = new LinkedList<File>();
        if (recursive) files = FileUtilities.flatten(dirs, Integer.MAX_VALUE);
        else files = FileUtilities.listFiles(dirs, 0);
        saveXMLInfoFiles(files, filter, save, ext, forced);
    }

    /*
     * <episodedetails>
     *  <season>?</season>
     *  <episode>?</episode>
     *  <title>Title Of The Episode</title>
     *  <plot>The plot of the episode</plot>
     *  <aired></aired>        <!-- the air date of the epsiode (YYYY-MM-DD) -->
     *  <airsAfterSeason></airsAfterSeason>
     *  <airsBeforeSeason></airsBeforeSeason>
     *  <airsBeforeEpisode></airsBeforeEpisode>
     * </episodedetails>
     */
    private static void saveXMLInfoFiles(Collection<File> files, 
            FileFilter filter, File save, String ext, boolean forced)
            throws UnsupportedFormatException, HttpException, IOException,
            ParserConfigurationException, SAXException,
            XPathExpressionException, XMLStreamException {
        //// LOG INFO
        String sourceMethod = "saveXMLInfoFiles";

        ///////////// CACHE SPEED UP /////////////
        File cache = new File(save, TheTVDBSearch.class.getName() + "." +
                sourceMethod + ".ser");
        Map<String, Long> map = new HashMap<String, Long>();
        if (cache.exists() && !forced) {
            ObjectInputStream ois =
                    new ObjectInputStream(new FileInputStream(cache));
            try {
                map = (Map<String, Long>) ois.readObject();
            } catch (ClassNotFoundException ex) {}
        }
        //////////////////////////////////////////
        
        List<File> filtered = FileUtilities.filter(files, filter);

        /* remove files */
        List<String> toRemove = new LinkedList<String>();
        Iterator<String> it1 = new FilterIterator<File, String>(filtered) {
            @Override
            protected String filter(File sourceValue) {
                return sourceValue.getName();
            }
        };
        for (String s : map.keySet()) {
            boolean found = false;
            while (it1.hasNext()) {
                if (it1.next().equals(s)) {
                    found = true;
                    break;
                }
            }
            if (!found) toRemove.add(s);
        }
        for (String s : toRemove) {
            map.remove(s);
            new File(save, FileUtilities.getNameWithoutExtension(s)
                            + "." + ext).delete();
        }

        // <name, <season, files>>
        Map<String, Map<Integer, SortedSet<File>>> groups =
                new HashMap<String, Map<Integer, SortedSet<File>>>();

        logger.logp(Level.INFO, TheTVDBSearch.class.getName(),
                sourceMethod, "Grouping espisodes of the same TV Show");

        Comparator<File> showComparator =
                new Comparator<File>() {
                    public int compare(File o1, File o2) {
                        try {
                            SubtitleAttributes sa1 = SubtitleAttributes.
                                    getProperties(FileUtilities.
                                        getNameWithoutExtension(o1.getName()));
                            SubtitleAttributes sa2 = SubtitleAttributes.
                                    getProperties(FileUtilities.
                                        getNameWithoutExtension(o2.getName()));
                            Integer s1 = sa1.getSeason();
                            Integer s2 = sa2.getSeason();
                            if (s1 < s2) return -1;
                            else if (s1 > s2) return 1;
                            else {
                                SortedSet<Integer> e1 = sa1.getEpisodes();
                                SortedSet<Integer> e2 = sa2.getEpisodes();
                                if (e1.first() < e2.first()) return -1;
                                else if (e1.first() > e2.first()) return 1;
                                else return 0;
                            }
                        } catch (UnsupportedFormatException ex) {
                            return 0;
                        }
                    }
                };
        
        for (File f : filtered) {
            SubtitleAttributes saf = SubtitleAttributes.getProperties(
                    FileUtilities.getNameWithoutExtension(f.getName()));
            String name = saf.getName();
            Map<Integer, SortedSet<File>> groupMap = groups.get(name);
            SortedSet<File> list = null;
            if (groupMap == null) {
                groupMap = new HashMap<Integer, SortedSet<File>>();
                list = new TreeSet<File>(showComparator);
                list.add(f);
                groupMap.put(saf.getSeason(), list);
                groups.put(name, groupMap);
            } else {
                list = groupMap.get(saf.getSeason());
                if (list == null) {
                    list = new TreeSet<File>(showComparator);
                    list.add(f);
                    groupMap.put(saf.getSeason(), list);
                } else {
                    list.add(f);
                }
            }
        }
        
        logger.logp(Level.INFO, TheTVDBSearch.class.getName(),
                sourceMethod, "Grouping ended");

        logger.logp(Level.INFO, TheTVDBSearch.class.getName(),
                sourceMethod, "Analysing each group (TV Show)");

        Set<Entry<String, Map<Integer, SortedSet<File>>>> entrySet =
                groups.entrySet();
        int j = 0;
        for (Entry<String, Map<Integer, SortedSet<File>>> entry : entrySet) {
            String show = entry.getKey();
            Map<Integer, SortedSet<File>> seasonEpisodesM = entry.getValue();

            logger.logp(Level.INFO, TheTVDBSearch.class.getName(),
                sourceMethod, "Analysing TV Show (" + (++j) + "/"
                + entrySet.size() + "): " + show);

            ///////////////////////////
            boolean skipGroup = true;
            for (SortedSet<File> episodes : seasonEpisodesM.values()) {
                boolean skipSeason = true;
                if (!episodes.isEmpty()) {
                    Iterator<File> it = episodes.iterator();
                    File first = it.next();
                    String nameWithoutext =
                        FileUtilities.getNameWithoutExtension(first.getName());
                    File out = new File(save, nameWithoutext + "." + ext);
                    /* clear info for any other episode of this season
                     * because the first episode reprents the hole season
                     */
                    while (it.hasNext()) {
                        File f = it.next();
                        nameWithoutext = FileUtilities.getNameWithoutExtension(
                                f.getName());
                        File outp = new File(save, nameWithoutext + "." + ext);
                        outp.delete();
                    }
                    if (out.exists()) {
                        /* the info for the representant file already exists.
                         * we must check if there are new episodes (or at least
                         * modified episodes in this season)
                         */
                        for (File f : episodes) {
                            Long lastModified = map.get(f.getName());
                            if (lastModified == null ||
                                    f.lastModified() > lastModified) {
                                    skipSeason = false;
                                    break;
                            }
                        }
                        
                    } else skipSeason = false;
                }
                skipGroup &= skipSeason;
            }
            if (skipGroup) continue;
            
            ///////////////////////////
            // DIRTY HACK
            if (show.compareToIgnoreCase("csi") == 0)
                show = show + " crime scene investigation";
            else if(show.compareToIgnoreCase("csi new york") == 0)
                show = "csi ny";
            else if (show.contains("hawaii five-0"))
                show = "hawaii five-0";

            List<Map<String, String>> showsSR = TheTVDBSearch.search(show);
            if (showsSR == null || showsSR.isEmpty()) continue;
            Map<String, String> showInfo = showsSR.get(0);
            String seriesID = showInfo.get("seriesid");
            String language = showInfo.get("language");
            if (seriesID == null || seriesID.isEmpty()) continue;
            if (language == null || language.isEmpty()) continue;
            List<Map<String, String>> episodesInfo =
                    TheTVDBSearch.getSeriesInformation(seriesID, language);

            Map<Integer, Map<Integer, Map<String, String>>> superMap =
                    new HashMap<Integer, Map<Integer, Map<String, String>>>();

            for (Map<String, String> episodeInfo : episodesInfo) {
                String seasonNumS = episodeInfo.get("SeasonNumber");
                if (seasonNumS == null || seasonNumS.isEmpty()) continue;
                Integer seasonNum = Integer.valueOf(seasonNumS);
                String epNumS = episodeInfo.get("EpisodeNumber");
                if (epNumS == null || epNumS.isEmpty()) continue;
                Integer epNum = Integer.valueOf(epNumS);
                Map<Integer, Map<String, String>> smap =
                        superMap.get(seasonNum);
                if (smap == null) {
                    smap = new HashMap<Integer, Map<String, String>>();
                    smap.put(epNum, episodeInfo);
                    superMap.put(seasonNum, smap);
                } else smap.put(epNum, episodeInfo);

            }

            for (Entry<Integer, SortedSet<File>> seasonEpisodes :
                seasonEpisodesM.entrySet()) {
                    Integer season = seasonEpisodes.getKey();
                    SortedSet<File> episodes = seasonEpisodes.getValue();

                XMLDocumentWriter sw = new XMLDocumentWriter();
                
                sw.writeStartDocument();
                sw.writeStartElement("xml");
                sw.writeStartElement("tvshow");
                sw.writeStartElement("id");
                sw.writeAttribute("moviedb", "thetvdb");
                sw.writeCData(seriesID + "");
                sw.writeEndElement();
                sw.writeEndElement();

               for (File episode : episodes) {
                    String nameWithoutext =
                        FileUtilities.getNameWithoutExtension(episode.getName());
                    SubtitleAttributes sa = SubtitleAttributes.getProperties(
                        nameWithoutext);
                    SortedSet<Integer> eps = sa.getEpisodes();

                    for (Integer ep : eps) {
                        Map<Integer, Map<String, String>> smap =
                                superMap.get(season);
                        if (smap == null) continue;
                        Map<String, String> episodeInfo = smap.get(ep);
                        if (episodeInfo == null) continue;

                        sw.writeStartElement("episodedetails");
                        sw.writeStartElement("season");
                        sw.writeCData(sa.getSeason() + "");
                        sw.writeEndElement();
                        sw.writeStartElement("episode");
                        sw.writeCData(ep + "");
                        sw.writeEndElement();
                        sw.writeStartElement("title");
                        sw.writeCData(episodeInfo.get("EpisodeName"));
                        sw.writeEndElement();
                        sw.writeStartElement("plot");
                        sw.writeCData(episodeInfo.get("Overview"));
                        sw.writeEndElement();
                        sw.writeStartElement("aired");
                        sw.writeCData(episodeInfo.get("FirstAired"));
                        sw.writeEndElement();
                        sw.writeEndElement();
                    }
                    //sw.flush();

                    ///////////// CACHE SPEED UP /////////////
                    map.put(episode.getName(), episode.lastModified());
                }

                sw.writeEndElement();
                sw.writeEndDocument();
                //sw.flush();
                //sw.close();

                if (!episodes.isEmpty()) {
                    File first = episodes.first();
                    String nameWithoutext =
                        FileUtilities.getNameWithoutExtension(first.getName());
                    File outFile = new File(save, nameWithoutext + "." + ext);
                    FileOutputStream os = new FileOutputStream(outFile);

                    XMLUtilities.emitDocument(sw.getDocument(), os, "UTF-8");

                }
            }
                
            logger.logp(Level.INFO, TheTVDBSearch.class.getName(),
                sourceMethod, "TV Show " + show + " analysed");
        }

        ObjectOutputStream oos =
                new ObjectOutputStream(new FileOutputStream(cache));
        oos.writeObject(map);
        oos.close();
    }
}
