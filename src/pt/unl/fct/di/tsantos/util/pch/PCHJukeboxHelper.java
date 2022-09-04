/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.pch;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.sourceforge.tuned.FileUtilities;
import net.sourceforge.tuned.FileUtilities.ExtensionFileFilter;
import org.apache.commons.httpclient.HttpException;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pt.unl.fct.di.tsantos.util.FileMultipleFilter;
import pt.unl.fct.di.tsantos.util.ImageUtilities;
import pt.unl.fct.di.tsantos.util.collection.ArraysExtended;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedFormatException;
import pt.unl.fct.di.tsantos.util.net.WebRequest;
import pt.unl.fct.di.tsantos.util.thetvdb.TheTVDBSearch;
import pt.unl.fct.di.tsantos.util.tmdb.TMDbSearch;
import pt.unl.fct.di.tsantos.util.uTorrent.uTorrent;

/**
 *
 * @author tvcsantos
 */
public class PCHJukeboxHelper {

    private PCHJukeboxHelper() {
        throw new UnsupportedOperationException();
    }

    private static int findLength(String[] args, int start, String[] commands) {
        int i;
        for (i = start; i < args.length; i++)
            if (ArraysExtended.contains(commands, args[i]))
                return i - start;
        if (i == args.length) return i - start;
        return 0;
    }

    private static Collection<File>
            getFiles(String[] arr, int start, int length) {
        List<File> list = new LinkedList<File>();
        for (int i = 0; i < length; i++)
            list.add(new File(arr[start + i]));
        return list;
    }

    public static void main(String[] args) throws Exception {
        List<File> moviesDirs = new LinkedList<File>();
        List<File> showsDirs = new LinkedList<File>();
        File infoOutput = null;
        File xmlWatched = null;
        File watchedDir = null;
        boolean recursive = false;
        boolean forced = false;

        ////////////// TEST //////////////
        args = new String[] {
            "-r", "-i", "E:\\PCH\\NFO", "-t" ,"D:\\Media\\Shows",
            "E:\\Movies\\Persons.Unknown.S01.720p.HDTV.x264-TvT",
            "-m", "D:\\Media\\Movies", "E:\\Movies", "I:\\Movies" ,"-wi",
            "E:\\PCH\\watched\\watchedList.xml", "-wo", "E:\\PCH\\watched"
        };

        ////////////// TEST //////////////
        /*moviesDirs.add(new File("D:/Media/Movies"));
        moviesDirs.add(new File("E:/Movies"));
        showsDirs.add(new File("D:/Media/Shows"));
        showsDirs.add(
                new File("E:/Movies/Persons.Unknown.S01.720p.HDTV.x264-TvT"));
        infoOutput = new File("E:/PCH/NFO");
        xmlWatched = new File("E:/PCH/watched/watchedList.xml");
        watchedDir = new File("E:/PCH/watched");
        recursive = true;
        forced = false;*/
        //////////////////////////////////

        String[] cmds = new String[] {
            "-i", "-r", "-f", "-m", "-t", "-wi", "-wo"
        };

        for (int i = 0; i < args.length ; i++) {
            String arg = args[i];
            if (arg.toLowerCase().equals("-i"))
                infoOutput = new File(args[++i]);
            else if (arg.toLowerCase().equals("-r"))
                recursive = true;
            else if (arg.toLowerCase().equals("-f"))
                forced = true;
            else if (arg.toLowerCase().equals("-m")) {
                int length = findLength(args, i + 1, cmds);
                moviesDirs.addAll(getFiles(args, i + 1, length));
                i += length;
            } else if (arg.toLowerCase().equals("-t")) {
                int length = findLength(args, i + 1, cmds);
                showsDirs.addAll(getFiles(args, i + 1, length));
                i += length;
            } else if (arg.toLowerCase().equals("-wi"))
                xmlWatched = new File(args[++i]);
            else if (arg.toLowerCase().equals("-wo"))
                watchedDir = new File(args[++i]);
        }

        if (infoOutput == null)
            throw new NullPointerException(
                    "infoOutput directory must be non null");
        else if (!infoOutput.exists())
            infoOutput.mkdirs();
        else if (!infoOutput.isDirectory())
            throw new Exception("infoOutput must be a directory");

        start(moviesDirs, showsDirs, infoOutput, xmlWatched, 
                watchedDir, recursive, forced);

        System.out.println("FINISHED!");

    }

    public static List<File> generateWatchedFiles(File xml, File output)
        throws ParserConfigurationException,
            XPathExpressionException, IOException, SAXException {
        List<File> result = new LinkedList<File>();

        Document watchDoc =
            WebRequest.getDocument(xml.toURI().toURL());

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();

        //Get all search Result nodes
        NodeList nodes =
            (NodeList) xPath.evaluate("files/file",
                                        watchDoc, XPathConstants.NODESET);
        int nodeCount = nodes.getLength();

        for (int i = 0 ; i < nodeCount; i++) {
            Node n = nodes.item(i);
            String name = n.getTextContent();
            if (name == null || name.isEmpty()) continue;
            File file = new File(output, name + ".watched");
            file.createNewFile();
            result.add(file);
        }

        return result;
    }

    public static void start(List<File> moviesDirs, List<File> showsDirs,
        File infoOutput, File xmlWatched, File watchedDir,
        boolean recursive, boolean forced) throws ParserConfigurationException,
        UnsupportedFormatException, HttpException,
        XPathExpressionException, IOException, SAXException, XMLStreamException {
        File[] files = watchedDir.listFiles(new ExtensionFileFilter("watched"));
        for (File f: files) f.delete(); //clear all watched files

        FileFilter fileFilter = new FileMultipleFilter(
                new ExtensionFileFilter("mkv", "avi"),
                new FileFilter() {
                    public boolean accept(File pathname) {
                        return !pathname.getName().toLowerCase().
                                contains("sample");
                    }
                });

        generateWatchedFiles(xmlWatched, watchedDir);

        TheTVDBSearch.saveXMLInfoDirs(showsDirs, fileFilter,
                infoOutput, "info", recursive, forced);

        TMDbSearch.saveXMLInfoDirs(moviesDirs, fileFilter,
                infoOutput, "info", recursive, forced);
    }

    public static void watermarkDownloadingFiles(File jukebox,
            String host, int port, String uTorrentUser, String uTorrentPwd)
            throws HttpException, IOException, JSONException {
        uTorrent ut = new uTorrent(host, port);
        ut.login(uTorrentUser, uTorrentPwd);

        Collection<String> col = ut.listDownloadingFiles();
        ut.logout();

        List<File> files = new LinkedList<File>();
        for (String name : col) {
            files.add(new File(name));
        }
        FileFilter fileFilter = new FileMultipleFilter(
                new ExtensionFileFilter("mkv", "avi"),
                new FileFilter() {

                    public boolean accept(File pathname) {
                        return !pathname.getName().toLowerCase().
                                contains("sample");
                    }
                });
        files = FileUtilities.filter(files, fileFilter);
        final List<String> list = new LinkedList<String>();
        for (File f : files) {
            String name = FileUtilities.getNameWithoutExtension(f.getName());
            name = name.replace("wspÂ®", "wsp®");
            list.add(name);
        }

        File[] imgFiles = jukebox.listFiles(new FileMultipleFilter(
                new ExtensionFileFilter("jpeg", "jpg"),
                new FileFilter() {

                    public boolean accept(File pathname) {
                        String name = pathname.getName();
                        boolean result = false;
                        for (String s : list) {
                            if (name.startsWith(s)) {
                                result = true;
                                break;
                            }
                        }
                        return result;
                    }
                }));

        for (File file : imgFiles) {
            int size = 100;
            String name = file.getName();
            if (name.contains(".fanart")) continue;
            if (name.contains("_large")) size = 20;
            if (name.contains("_small")) size = 10;
            if (name.contains(".originalimg.")) continue;
            String origName = FileUtilities.getNameWithoutExtension(name);
            String ext = FileUtilities.getExtension(name);
            File original = new File(file.getParentFile(), origName + 
                    ".originalimg." + ext);
            BufferedImage img /*= ImageIO.read(file)*/;
            if (original.exists()) img = ImageIO.read(original);
            else {
                img = ImageIO.read(file);
                ImageIO.write(img, "JPG", original);
            }
            Color from = new Color(1f, 0f, 0f, 0.5f);
            Color to = new Color(1f, 1f, 1f, 0.5f);
            Font font = new Font("Arial", Font.BOLD, size);
            img = ImageUtilities.watermark(img, "DOWNLOADING", font, from, to);
            ImageIO.write(img, "JPG", file);
        }
    }
}
