package pt.unl.fct.di.tsantos.util.tmdb;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.sourceforge.tuned.FileUtilities;
import net.sourceforge.tuned.FilterIterator;
import net.sourceforge.tuned.StringUtilities;
import org.apache.commons.httpclient.HttpException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pt.unl.fct.di.tsantos.util.Pair;
import pt.unl.fct.di.tsantos.util.collection.RandomList;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedFormatException;
import pt.unl.fct.di.tsantos.util.imdb.IMDB;
import pt.unl.fct.di.tsantos.util.net.WebRequest;
import pt.unl.fct.di.tsantos.util.string.StringUtils;
import static pt.unl.fct.di.tsantos.util.xml.XMLUtilities.*;

public class TMDbSearch {

    private static final String API_KEY = "d4ad46ee51d364386b6cf3b580fb5d8c";
    private static String MOVIE_SEARCH =
            "http://api.themoviedb.org/2.1/Movie.search/en/xml/" + 
            API_KEY + "/";
    private static String IMD_LOOKUP =
            "http://api.themoviedb.org/2.1/Movie.imdbLookup/"
                + "en/xml/" + API_KEY + "/";
    private static String GET_INFO =
            "http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/"
                + API_KEY + "/";

    private static final Logger logger =
            Logger.getLogger(TMDbSearch.class.getName());

    public static Logger getLogger() {
        return logger;
    }

    private TMDbSearch() {
        throw new UnsupportedOperationException();
    }
		
    public static List<SearchResult> search(String query) throws HttpException,
        IOException, SAXException, ParserConfigurationException,
        XPathExpressionException {
        checkParameter(query);
        //query = query.replaceAll(" ", "+");
        query = query.replaceAll(":", "");
        query = query.replaceAll("-", " ");
        query = URLEncoder.encode(query, "UTF-8");
        Document response = WebRequest.getDocument(
                new URL(new URL(MOVIE_SEARCH),query));

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();

        //Get all search Result nodes
        NodeList nodes = 
            (NodeList) xPath.evaluate("/OpenSearchDescription/movies/movie",
                                        response, XPathConstants.NODESET);
        int nodeCount = nodes.getLength();

        List<SearchResult> l = new ArrayList<SearchResult>();

        //iterate over search Result nodes
        for (int i = 0; i < nodeCount; i++) {
            Node n = nodes.item(i);
            String name = (String) xPath.evaluate("name", n,
                                                    XPathConstants.STRING);
            if (name == null || name.length() <= 0) {
                continue;
            }
            //System.out.println(name);
            String id = (String) xPath.evaluate("id", n,
                                                    XPathConstants.STRING);
            String imdbID = (String) xPath.evaluate("imdb_id", n,
                                                    XPathConstants.STRING);
            //URL url = null;
            URL url = new URL((String) xPath.evaluate("url", n,
                                                    XPathConstants.STRING));
            NodeList imgNodes = (NodeList) xPath.evaluate("images/image", n,
                                                    XPathConstants.NODESET);
            String imgURL = null;
            for (int j = 0; j < imgNodes.getLength(); j++) {
                NamedNodeMap map = imgNodes.item(j).getAttributes();
                imgURL = map.getNamedItem("url").getTextContent();
                String type = map.getNamedItem("type").getTextContent();
                String size = map.getNamedItem("size").getTextContent();
                if (type.compareTo("poster") == 0
                        && size.compareTo("mid") == 0) {
                    break;
                }
            }
            l.add(new SearchResult(name, id,
                    imgURL == null ? null : new URL(imgURL), url, imdbID));
        }

        return l;
    }

    public static List<String> getOriginalLanguages(SearchResult sr)
            throws IOException {
        //PARSE sr.url if not found parse sr.imdbID
        Source source = new Source(sr.url);
        source.setLogger(null);
        List<StartTag> allStartTags =
            source.getAllStartTags("div class=\"fact\"");
        List<String> res = new LinkedList<String>();
        for (StartTag s : allStartTags) {
            if (s == null) continue;
            Element e = s.getElement();
            if (e == null) continue;
            List<Element> childs = e.getChildElements();
            if (childs == null) continue;
            if (childs.size() < 2 || childs.size() > 2) continue;
            Element c1 = childs.get(0);
            if (c1 == null) continue;
            Segment s1 = c1.getContent();
            if (s1 == null) continue;
            String a = s1.toString();
            if (a == null) continue;
            if (a.compareTo("Languages (original):") != 0) continue;
            Element c2 = childs.get(1);
            if (c2 == null) continue;
            Segment s2 = c2.getContent();
            if (s2 == null) continue;
            String b = s2.toString();
            if (b == null) continue;
            b = b.trim();
            StringTokenizer st = new StringTokenizer(b, ".,|- ");
            while (st.hasMoreTokens()) res.add(st.nextToken());
            if (!res.isEmpty()) return res;
        }
        //NOT FOUND BECAUSE NOT RETURNED
        URL url = new URL("http://www.imdb.com/title/" + sr.getImdbID() + "/");
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; " +
                "Windows NT 6.0; en-GB; rv:1.9.1.2) Gecko/20090729 " +
                "Firefox/3.5.2 (.NET CLR 3.5.30729)");
        source = new Source(conn.getInputStream());
        source.setLogger(null);
        allStartTags = source.getAllStartTags("div class=\"info\"");
        for (StartTag s : allStartTags) {
            if (s == null) continue;
            Element e = s.getElement();
            if (e == null) continue;
            List<Element> h5elems = e.getAllElements(HTMLElementName.H5);
            if (h5elems == null) continue;
            if (h5elems.size() != 1) continue;
            Element h5 = h5elems.get(0);
            if (h5 == null) continue;
            Segment ch5 = h5.getContent();
            if (ch5 == null) continue;
            String sch5 = ch5.toString();
            if (sch5 == null) continue;
            if (sch5.compareTo("Language:") != 0) continue;
            List<Element> childs = e.getChildElements();
            if (childs.size() <= 1) continue;
            for (int c = 1; c < childs.size(); c++) {
                Element child = childs.get(c);
                if (child == null) continue;
                Segment content = child.getContent();
                if (content == null) continue;
                List<Element> elems = content.getAllElements();
                if (elems == null || elems.isEmpty()) {
                    String lang = content.toString();
                    res.add(lang.trim());
                    continue;
                }
                for (Element elem : elems) {
                    if (!elem.getName().equals(HTMLElementName.A))
                        continue;
                    Segment content2 = elem.getContent();
                    if (content2 == null) continue;
                    String lang = content2.toString();
                    res.add(lang.trim());
                }                
            }
            return res;
        }
        return new ArrayList<String>();
    }

    public static String getID(String imdbID)
        throws HttpException,
        IOException, SAXException, ParserConfigurationException,
        XPathExpressionException {
        checkParameter(imdbID);
        Document response = WebRequest.getDocument(
                new URL(new URL(IMD_LOOKUP), imdbID));

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();

        //Get all search Result nodes
        NodeList nodes =
            (NodeList) xPath.evaluate("/OpenSearchDescription/movies/movie",
                                        response, XPathConstants.NODESET);
        int nodeCount = nodes.getLength();

        if (nodeCount < 1) return null;

        Node n = nodes.item(0);
        String id = (String) xPath.evaluate("id", n, XPathConstants.STRING);

        if (id == null || id.length() <= 0) return null;
        else return id;

    }

    public static Document getInfo(String mdbID) throws HttpException,
        IOException, SAXException, ParserConfigurationException,
        XPathExpressionException {
        checkParameter(mdbID);
        Document doc = WebRequest.getDocument(
                new URL(new URL(GET_INFO),mdbID));
        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();

        //Get all search Result nodes
        NodeList nodes =
            (NodeList) xPath.evaluate("/OpenSearchDescription/movies/movie",
                                        doc, XPathConstants.NODESET);
        int nodeCount = nodes.getLength();

        if (nodeCount < 1) return null;

        Node n = nodes.item(0);
        
        String title =
                (String) xPath.evaluate("name", n, XPathConstants.STRING);

        String originaltitle =
                (String) xPath.evaluate("original_name",
                                        n, XPathConstants.STRING);

        String rating =
                (String) xPath.evaluate("rating", n, XPathConstants.STRING);

        String premiered =
                (String) xPath.evaluate("released", n, XPathConstants.STRING);

        Matcher m = Pattern.compile("\\s*(\\d{4})-\\d{2}-\\d{2}\\s*").
                matcher(premiered);

        String year = null; // obtain from premiered date

        if (m.matches()) year = m.group(1);

        String tagline =
                (String) xPath.evaluate("tagline", n, XPathConstants.STRING);

        String runtime =
                (String) xPath.evaluate("runtime", n, XPathConstants.STRING);

        String imdbID =
                (String) xPath.evaluate("imdb_id", n, XPathConstants.STRING);

        String thumb = IMDB.getPoster("http://www.imdb.com/title/" + imdbID);

        NodeList images = 
                (NodeList) xPath.evaluate("images/image",
                    n, XPathConstants.NODESET);

        nodeCount = images.getLength();

        RandomList<String> filtered = new RandomList<String>(nodeCount);
        //iterate over search Result nodes
        for (int i = 0; i < nodeCount; i++) {
            Node image = images.item(i);
            NamedNodeMap attributes = image.getAttributes();
            Node namedItem = attributes.getNamedItem("type");
            String type = namedItem.getTextContent();
            if (!type.equalsIgnoreCase("backdrop")) continue;
            namedItem = attributes.getNamedItem("size");
            String size = namedItem.getTextContent();
            if (!size.equalsIgnoreCase("original")) continue;
            namedItem = attributes.getNamedItem("url");
            String url = namedItem.getTextContent();
            filtered.add(url);
        }

        String fanart = filtered.randomElement();

        String certification = (String) xPath.evaluate("certification",
                n, XPathConstants.STRING);

        String trailer = (String) xPath.evaluate("trailer",
                n, XPathConstants.STRING);

        List<String> genres = new LinkedList<String>();

        NodeList categories =
                (NodeList) xPath.evaluate("categories/category",
                    n, XPathConstants.NODESET);

        nodeCount = categories.getLength();
        for (int i = 0; i < nodeCount; i++) {
            Node category = categories.item(i);
            NamedNodeMap attributes = category.getAttributes();
            Node namedItem = attributes.getNamedItem("type");
            String type = namedItem.getTextContent();
            if (!type.equalsIgnoreCase("genre")) continue;
            namedItem = attributes.getNamedItem("name");
            String name = namedItem.getTextContent();
            genres.add(name);
        }

        List<String> writers = new LinkedList<String>();
        List<String> directing = new LinkedList<String>();
        List<Pair<String, String>> actors =
                new LinkedList<Pair<String, String>>();

        NodeList persons =
                (NodeList) xPath.evaluate("cast/person",
                    n, XPathConstants.NODESET);

        nodeCount = persons.getLength();
        for (int i = 0; i < nodeCount; i++) {
            Node person = persons.item(i);
            NamedNodeMap attributes = person.getAttributes();
            Node namedItem = attributes.getNamedItem("department");
            String department = namedItem.getTextContent();
            namedItem = attributes.getNamedItem("name");
            String name = namedItem.getTextContent();
            if (department.equalsIgnoreCase("actors")) {
                namedItem = attributes.getNamedItem("character");
                String role = namedItem.getTextContent();
                actors.add(new Pair<String, String>(name, role));
            } else if (department.equalsIgnoreCase("directing")) {
                directing.add(name);
            } else if (department.equalsIgnoreCase("writing")) {
                writers.add(name);
            }            
        }

        String director = StringUtilities.join(directing, ", ");

        SortedSet<String> studs = new TreeSet<String>(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });

        NodeList studios =
                (NodeList) xPath.evaluate("studios/studio",
                    n, XPathConstants.NODESET);
        nodeCount = studios.getLength();
        for (int i = 0; i < nodeCount; i++) {
            Node studio = studios.item(i);
            NamedNodeMap attributes = studio.getAttributes();
            Node namedItem = attributes.getNamedItem("name");
            String name = namedItem.getTextContent();
            studs.add(name);
        }

        String company = StringUtilities.join(studs, ", ");

        List<String> countries = new LinkedList<String>();

        NodeList countrs =
                (NodeList) xPath.evaluate("countries/country",
                    n, XPathConstants.NODESET);
        nodeCount = countrs.getLength();
        for (int i = 0; i < nodeCount; i++) {
            Node country = countrs.item(i);
            NamedNodeMap attributes = country.getAttributes();
            Node namedItem = attributes.getNamedItem("code");
            String code = namedItem.getTextContent();
            countries.add(code);
        }

        String country = StringUtilities.join(countries, ", ");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document result = db.newDocument();
        org.w3c.dom.Element root = result.createElement("movie");

        createAndAppend(result, "title", title, root);
        createAndAppend(result, "originaltitle", originaltitle, root);
        createAndAppend(result, "rating", rating, root);
        createAndAppend(result, "year", year, root);

        String[] imdbInfo =
                IMDB.getOutPlot250("http://www.imdb.com/title/" + imdbID);
        String outline = imdbInfo[0];
        String plot = imdbInfo[1];
        String top250 = imdbInfo[2];

        createAndAppend(result, "top250", top250, root);
        createAndAppend(result, "outline", outline, root);
        createAndAppend(result, "plot", plot, root);
        createAndAppend(result, "tagline", tagline, root);
        createAndAppend(result, "runtime", runtime, root);
        createAndAppend(result, "premiered", premiered, root);
        createAndAppend(result, "thumb", thumb, root);
        createAndAppend(result, "fanart", fanart, root);
        createAndAppend(result, "certification", certification, root);
        createAndAppend(result, "id", "-1", root);
        createAndAppend(result, "id", imdbID, root,
                new String[]{ "moviedb" }, new String[]{ "imdb" });
        createAndAppend(result, "id", mdbID, root,
                new String[]{ "moviedb" }, new String[]{ "themoviedb" });
        createAndAppend(result, "trailer", trailer, root);
        for (String genre : genres)
            createAndAppend(result, "genre", genre, root);
        org.w3c.dom.Element node = result.createElement("credits");
        for (String writer : writers)
            createAndAppend(result, "writer", writer, node);
        root.appendChild(node);
        createAndAppend(result, "director", director, root);
        createAndAppend(result, "company", company, root);
        createAndAppend(result, "country", country, root);
        node = result.createElement("actor");
        for (Pair<String, String> actor : actors) {
            createAndAppend(result, "name", actor.getFst(), node);
            createAndAppend(result, "role", actor.getSnd(), node);
        }
        root.appendChild(node);

        result.appendChild(root);
        
        return result;
    }

    public static void main(String[] args) throws Exception {
        /*Scanner sc =
                new Scanner(new FileInputStream(new File("moviesXpto.txt")));*/
        Scanner sc =
                new Scanner(new FileInputStream(new File("tmp.txt")));

        Set<String> failed = new HashSet<String>();
        SortedSet<String> names = new TreeSet<String>();
        List<Document> ldoc = new LinkedList<Document>();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            line = line.trim();
            if (line.isEmpty()) continue;
            names.add(line.toLowerCase());
        }
        for (String name : names) {
            List<SearchResult> list = search(name);
            if (list.isEmpty()) failed.add(name);
            SearchResult r = list.get(0);
            Document doc = getTMDbInfo(r.movieID);
            ldoc.add(doc);
        }        
        Collections.sort(ldoc, new MovieComparator());
        FilterIterator<Document, String> it =
                new FilterIterator<Document, String>(ldoc) {
            @Override
            protected String filter(Document sourceValue) {
                try {
                    XPathFactory factory = XPathFactory.newInstance();
                    XPath xPath = factory.newXPath();
                    Node n = sourceValue.getFirstChild();
                    String originaltitle = (String)
                            xPath.evaluate("original_name",
                                        n, XPathConstants.STRING);
                    return originaltitle;
                } catch (XPathExpressionException ex) {
                    return null;
                }
            }
        };
        while (it.hasNext()) System.out.println(it.next());
        System.out.println("------ FAILED ------");
        for (String fail : failed) System.out.println(fail);
        /*System.out.println(getID("tt1193631"));
        Document doc = getTMDbInfo("20526");
        //doc.
        WebRequest.emitDocument(doc, System.out, "UTF-8");*/
    }

    private static void checkParameter(String s) {
        if (s == null) throw new NullPointerException(
                "the parameter can't be null");
        if (s.isEmpty()) throw new IllegalArgumentException(
                "the parameter can't be empty");
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

    public static void saveXMLInfoFiles(Collection<File> files, 
            FileFilter filter, File save, String ext, boolean forced)
            throws ParserConfigurationException,
            XPathExpressionException, IOException, SAXException, XMLStreamException {
        //// LOG INFO
        String sourceMethod = "saveXMLInfoFiles";

        ///////////// CACHE SPEED UP /////////////
        File cache = new File(save, TMDbSearch.class.getName() + "." +
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

        String[] ignore = new String[]{
            "unrated", "directors cut",
            "extended cut", "final cut", "remastered",
            "extended version", "2in1"
        };

        String[] replacements = new String[ignore.length];
        Arrays.fill(replacements, "");

        int j = 0;
        for (File file : filtered) {
            logger.logp(Level.INFO, TMDbSearch.class.getName(),
                    sourceMethod, "Parsing file (" + (++j) +
                    "/" + filtered.size() + "): " + file.getName());
            
            String nameWithoutExt =
                    FileUtilities.getNameWithoutExtension(file.getName());
            File output = new File(save, nameWithoutExt + "." + ext);

            ///////////// CACHE SPEED UP /////////////
            if (output.exists()) {
                Long lastModified = map.get(file.getName());
                if (lastModified != null
                        && file.lastModified() <= lastModified) {
                    continue;
                }
            }
            //////////////////////////////////////////

            Pattern p = Pattern.compile("(.+\\.)(\\d{4})\\..*");
            Matcher m = p.matcher(nameWithoutExt);
            
            if (m.matches()) { // hack
                String news = null;
                if (nameWithoutExt.toLowerCase().contains(".1080.") &&
                    !nameWithoutExt.toLowerCase().contains(".1080p.") &&
                    !nameWithoutExt.toLowerCase().contains(".720p."))
                    news = nameWithoutExt.replaceAll(".1080.", ".1080p");
                if (nameWithoutExt.toLowerCase().contains(".720.") &&
                    !nameWithoutExt.toLowerCase().contains(".1080p.") &&
                    !nameWithoutExt.toLowerCase().contains(".720p."))
                    news = nameWithoutExt.replaceAll(".720.", ".720p");
                if (news != null) {
                    m = p.matcher(news);
                }
            }

            if (m.matches()) {
                String movieNameDotted = m.group(1);
                String year = m.group(2);
                movieNameDotted = movieNameDotted.replaceAll(".DC.", "").
                        replaceAll(".Pt.", ".Part.");
                String movieName = normalizeName(movieNameDotted.toLowerCase());
                movieName = StringUtils.replace(movieName,
                        ignore, replacements);
                movieName = normalizeName(movieName);
                String imdbID = null;
                String mdbID = null;
                List<SearchResult> resList =
                        TMDbSearch.search(movieName + " " + year);
                if (!resList.isEmpty()) {
                    SearchResult first = resList.get(0);
                    mdbID = first.getMovieID();
                    imdbID = first.getImdbID();
                    imdbID = imdbID.replaceFirst("tt", "");
                } else {
                    imdbID = IMDB.getIMDBID(movieName);
                }
                if (imdbID == null) {
                    continue;
                }
                XMLOutputFactory of =
                        javax.xml.stream.XMLOutputFactory.newInstance();
                XMLStreamWriter sw =
                        of.createXMLStreamWriter(new OutputStreamWriter(
                        new FileOutputStream(output), "UTF-8"));
                sw.writeStartDocument();
                sw.writeStartElement("movie");
                sw.writeStartElement("id");
                sw.writeAttribute("moviedb", "imdb");
                sw.writeCData(imdbID);
                sw.writeEndElement();
                if (mdbID != null) {
                    sw.writeStartElement("id");
                    sw.writeAttribute("moviedb", "themoviedb");
                    sw.writeCData(mdbID);
                    sw.writeEndElement();
                }
                sw.writeEndElement();
                sw.writeEndDocument();
                sw.flush();
                sw.close();
                
                ///////////// CACHE SPEED UP /////////////
                map.put(file.getName(), file.lastModified());

                if (j % 30 == 0) { // INTERMEDIATE SAVE
                    ObjectOutputStream oos =
                            new ObjectOutputStream(new FileOutputStream(cache));
                    oos.writeObject(map);
                    oos.close();
                }
            } else {
                logger.logp(Level.SEVERE, TMDbSearch.class.getName(),
                    sourceMethod, "Failed parsing file: " + file.getName());
            }
        }

        ObjectOutputStream oos = 
                new ObjectOutputStream(new FileOutputStream(cache));
        oos.writeObject(map);
        oos.close();
    }

    private static String normalizeName(String name) {
        name = name.replaceAll("'", "");
        //name = name.replaceAll("\\p{Punct}", " ");
        name = name.replaceAll("\\.", " ");
        name = name.replaceAll("\\p{Space}+", " ");
        name = name.trim();
        return name;
    }
    
    public static Document getTMDbInfo(String mdbID) throws HttpException,
        IOException, SAXException, ParserConfigurationException,
        XPathExpressionException {
        checkParameter(mdbID);
        Document doc = WebRequest.getDocument(
                new URL(new URL(GET_INFO),mdbID));
        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();

        //Get all search Result nodes
        NodeList nodes =
            (NodeList) xPath.evaluate("/OpenSearchDescription/movies/movie",
                                        doc, XPathConstants.NODESET);
        int nodeCount = nodes.getLength();

        if (nodeCount < 1) return null;

        Node n = nodes.item(0);
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document result = db.newDocument();
        Node adopt = result.adoptNode(n);
        result.appendChild(adopt);
       
        return result;
    }

    static class MovieComparator implements Comparator<Document> {
        public int compare(Document o1, Document o2) {
            return -compareMethod(o1, o2);
        }

        private int compareMethod(Document o1, Document o2) {
            try {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xPath = factory.newXPath();
                Node n1 = o1.getFirstChild();
                Node n2 = o2.getFirstChild();

                /*String title1 = (String) xPath.evaluate("name",
                        n1, XPathConstants.STRING);*/
                String originaltitle1 = (String) xPath.evaluate("original_name",
                                        n1, XPathConstants.STRING);
                double rating1 = ((Double) xPath.evaluate("rating",
                        n1, XPathConstants.NUMBER)).doubleValue();

                NodeList categories =
                        (NodeList) xPath.evaluate("categories/category",
                                                n1, XPathConstants.NODESET);
                NodeList languagesSpoken = (NodeList) xPath.evaluate(
                        "languages_spoken/language_spoken", n1,
                                            XPathConstants.NODESET);

                Set<Integer> genres1 = getGenresIDs(categories);
                Set<String> langsSpoken1 = getLanguagesSpoken(languagesSpoken);
                boolean foreign1 = !langsSpoken1.contains("en");

                /*String title2 = (String) xPath.evaluate("name",
                        n2, XPathConstants.STRING);*/
                String originaltitle2 = (String) xPath.evaluate("original_name",
                                        n2, XPathConstants.STRING);
                double rating2 = ((Double) xPath.evaluate("rating",
                        n2, XPathConstants.NUMBER)).doubleValue();
                
                categories =
                        (NodeList) xPath.evaluate("categories/category",
                                                n2, XPathConstants.NODESET);
                languagesSpoken = (NodeList) xPath.evaluate(
                        "languages_spoken/language_spoken", n2,
                                            XPathConstants.NODESET);

                Set<Integer> genres2 = getGenresIDs(categories);
                Set<String> langsSpoken2 = getLanguagesSpoken(languagesSpoken);
                boolean foreign2 = !langsSpoken2.contains("en");

                double points1 = getPoints(genres1);
                double points2 = getPoints(genres2);

                if (foreign1 && !foreign2) return -1;
                else if (!foreign1 && foreign2) return 1;
                else if (!foreign1 && !foreign2) {
                    if (points1 < points2) return -1;
                    else if (points1 > points2) return 1;
                }
                // compare ratings
                if (rating1 < rating2) return -1;
                else if (rating1 > rating2) return 1;
                else // compare names
                    return originaltitle1.compareToIgnoreCase(
                            originaltitle2);
            } catch (XPathExpressionException ex) {
                return 0;
            }
        }

        private static Set<Integer> getGenresIDs(NodeList categories) {
            Set<Integer> result = new HashSet<Integer>();
            int nodeCount = categories.getLength();
            for (int i = 0; i < nodeCount; i++) {
                Node category = categories.item(i);
                NamedNodeMap attributes = category.getAttributes();
                Node namedItem = attributes.getNamedItem("type");
                String type = namedItem.getTextContent();
                if (!type.equalsIgnoreCase("genre")) {
                    continue;
                }
                namedItem = attributes.getNamedItem("id");
                Integer id = Integer.valueOf(namedItem.getTextContent());
                result.add(id);
            }
            return result;
        }

        private static Set<String> getLanguagesSpoken(NodeList languagesSpoken)
        {
            Set<String> result = new HashSet<String>();
            int nodeCount = languagesSpoken.getLength();
            for (int i = 0; i < nodeCount; i++) {
                Node langSpoken = languagesSpoken.item(i);
                NamedNodeMap attributes = langSpoken.getAttributes();
                Node namedItem = attributes.getNamedItem("code");
                String code = namedItem.getTextContent();
                result.add(code);
            }
            return result;
        }

        private static double getPoints(Set<Integer> genres) {
            double res = 0;
            int count = 0;
            for (Integer genre : genres) {
                switch (genre) {
                    // "Action"
                    case 28: res += 5; break;
                    // "Action & Adventure"
                    case 10759: res += 5; break;
                    // "Adventure"
                    case 12: res += 5; break;
                    // "Animation"
                    case 16: res += 4; break;
                    // "British"
                    case 10760: res += 2; break;
                    // "Comedy"
                    case 35: res += 1; break;
                    // "Crime"
                    case 80: res += 3; break;
                    // "Disaster"
                    case 105: res += 3; break;
                    // "Documentary"
                    case 99: res += 1; break;
                    // "Drama"
                    case 18: res += 1; break;
                    // "Eastern"
                    case 82: res += 1; break;
                    // "Education"
                    case 10761: res += 1; break;
                    // "Erotic"
                    case 2916: res += - Integer.MAX_VALUE; break;
                    // "Family"
                    case 10751: res += 4; break;
                    // "Fan Film"
                    case 10750: count--; break; //ignore
                    // "Fantasy"
                    case 14: res += 4; break;
                    // "Film Noir"
                    case 10753: count--; break; //ignore
                    // "Foreign"
                    case 10769: count--; break; //ignore
                    // "History"
                    case 36: res += 2; break;
                    // "Holiday"
                    case 10595: count--; break; //ignore
                    // "Horror"
                    case 27: res += 3; break;
                    // "Indie"
                    case 10756: count--; break; //ignore
                    // "Kids"
                    case 10762: res += 4; break;
                    // "Music"
                    case 10402: res += 4; break;
                    // "Musical"
                    case 22: res += 4; break;
                    // "Mystery"
                    case 9648: res += 3; break;
                    // "Neo-noir"
                    case 10754: count--; break; //ignore
                    // "News"
                    case 10763: count--; break; //ignore
                    // "Reality"
                    case 10764: count--; break; //ignore
                    // "Road Movie"
                    case 1115: count--; break; //ignore
                    // "Romance"
                    case 10749: res += 3; break;
                    // "Sci-Fi & Fantasy"
                    case 10765: res += 4; break;
                    // "Science Fiction"
                    case 878: res += 4; break;
                    // "Short"
                    case 10755: count--; break; //ignore
                    // "Soap"
                    case 10766: count--; break; //ignore
                    // "Sport"
                    case 9805: count--; break; //ignore
                    // "Sporting Event"
                    case 10758: count--; break; //ignore
                    // "Sports Film"
                    case 10757: count--; break; //ignore
                    // "Suspense"
                    case 10748: res += 3; break;
                    // "Talk"
                    case 10767: count--; break; //ignore
                    // "Thriller"
                    case 53: res += 3; break;
                    // "War"
                    case 10752: res += 2; break;
                    // "War & Politics"
                    case 10768: res += 2; break;
                    // "Western"
                    case 37: res += 1; break;
                    default: break;
                }
                count++;
            }
            return res/count;
        }
        
    }

}
