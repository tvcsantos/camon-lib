package pt.unl.fct.di.tsantos.util.pch;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.tuned.FileUtilities;
import net.sourceforge.tuned.FileUtilities.ExtensionFileFilter;
import pt.unl.fct.di.tsantos.util.FileMultipleFilter;
import pt.unl.fct.di.tsantos.util.string.StringUtils;

public class ListFiles {

    private ListFiles() {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) throws Exception {
        
        List<File> list = new LinkedList<File>();
        //list.add(new File(new File("\\\\remote\\Shared"), "watched"));
        list.add(new File(new File("Z:"), "watched"));
        //list.add(new File("D:/Media/Movies"));
        //list.add(new File("E:/Movies"));
        //list.add(new File("I:/Movies"));
        //list.add(new File("Z:/Movies"));
        //listFiles(list, new FileOutputStream("movies.txt"),
          //      new MovieFileComparator());
        //listFiles(list, System.out, null, "<file>", "</file>");
        List<File> res = FileUtilities.flatten(list, Integer.MAX_VALUE);
        System.out.println(res);
    }

    public static void listFiles(List<File> list, OutputStream os,
            Comparator<File> comparator)
            throws IOException {
        listFiles(list, os, comparator, null, null);
    }

    public static void listFiles(List<File> list, OutputStream os,
            Comparator<File> comparator, String prefix, String suffix)
            throws IOException {
        list = FileUtilities.flatten(list, Integer.MAX_VALUE);

        FileFilter fileFilter = new FileMultipleFilter(
                new ExtensionFileFilter("mkv", "avi"),
                new FileFilter() {
                    public boolean accept(File pathname) {
                        return !pathname.getName().toLowerCase().
                                contains("sample");
                    }
                });

        list = FileUtilities.filter(list, fileFilter);

        SortedSet<File> set = new TreeSet<File>(comparator);
        set.addAll(list);

        OutputStreamWriter osw = new OutputStreamWriter(os);
        for (File f : set) 
            osw.write((prefix == null ? "" : prefix) +
                            f.getName() +
                      (suffix == null ? "" : suffix) + "\n");
        osw.close();
    }

    /*public static void main(String[] args) throws Exception {
        List<File> dirs = new LinkedList<File>();
        dirs.add(new File("D:/Media/Movies"));
        dirs.add(new File("D:/Media/Shows"));
        dirs.add(new File("E:/Movies"));
        File output = new File("E:/PCH/watched/watchedList.xml");

        FileFilter fileFilter = new FileMultipleFilter(
                new ExtensionFileFilter("mkv", "avi"),
                new FileFilter() {
                    public boolean accept(File pathname) {
                        return !pathname.getName().toLowerCase().
                                contains("sample");
                    }
                });

        List<File> files = FileUtilities.flatten(dirs, Integer.MAX_VALUE);

        List<File> filtered = FileUtilities.filter(files, fileFilter);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document result = db.newDocument();
        org.w3c.dom.Element root = result.createElement("files");

        for (File file : filtered) {
            org.w3c.dom.Element node = result.createElement("file");
            org.w3c.dom.Text text = result.createTextNode(file.getName());
            node.appendChild(text);
            root.appendChild(node);
        }

        result.appendChild(root);

        WebRequest.emitDocument(result, new FileOutputStream(output), "UTF-8");
    }*/

    static class MovieFileComparator implements Comparator<File> {

        public MovieFileComparator() {
        }

        public int compare(File o1, File o2) {
            String[] ignore = new String[]{
                "unrated", "directors cut",
                "extended cut", "final cut", "remastered",
                "extended version", "2in1"
            };

            String[] replacements = new String[ignore.length];
            Arrays.fill(replacements, "");

            String nameWithoutExto1 =
                    FileUtilities.getNameWithoutExtension(o1.getName());
            String nameWithoutExto2 =
                    FileUtilities.getNameWithoutExtension(o2.getName());

            Pattern p = Pattern.compile("(.+\\.)(\\d{4})\\..*");
            
            Matcher m1 = p.matcher(nameWithoutExto1);
            Matcher m2 = p.matcher(nameWithoutExto2);

            if (m1.matches()) { // hack
                String news = null;
                if (nameWithoutExto1.toLowerCase().contains(".1080.") &&
                    !nameWithoutExto1.toLowerCase().contains(".1080p.") &&
                    !nameWithoutExto1.toLowerCase().contains(".720p."))
                    news = nameWithoutExto1.replaceAll(".1080.", ".1080p");
                if (nameWithoutExto1.toLowerCase().contains(".720.") &&
                    !nameWithoutExto1.toLowerCase().contains(".1080p.") &&
                    !nameWithoutExto1.toLowerCase().contains(".720p."))
                    news = nameWithoutExto1.replaceAll(".720.", ".720p");
                if (news != null) {
                    m1 = p.matcher(news);
                }
            }

            if (m2.matches()) { // hack
                String news = null;
                if (nameWithoutExto2.toLowerCase().contains(".1080.") &&
                    !nameWithoutExto2.toLowerCase().contains(".1080p.") &&
                    !nameWithoutExto2.toLowerCase().contains(".720p."))
                    news = nameWithoutExto2.replaceAll(".1080.", ".1080p");
                if (nameWithoutExto2.toLowerCase().contains(".720.") &&
                    !nameWithoutExto2.toLowerCase().contains(".1080p.") &&
                    !nameWithoutExto2.toLowerCase().contains(".720p."))
                    news = nameWithoutExto2.replaceAll(".720.", ".720p");
                if (news != null) {
                    m2 = p.matcher(news);
                }
            }

            if (m1.matches() && m2.matches()) {
                String movieNameDottedo1 = m1.group(1);
                String yearo1 = m1.group(2);
                movieNameDottedo1 = movieNameDottedo1.replaceAll(".DC.", "").
                        replaceAll(".Pt.", ".Part.");
                String movieNameo1 =
                        normalizeName(movieNameDottedo1.toLowerCase());
                movieNameo1 = StringUtils.replace(movieNameo1,
                        ignore, replacements);
                movieNameo1 = normalizeName(movieNameo1);

                String movieNameDottedo2 = m2.group(1);
                String yearo2 = m2.group(2);
                movieNameDottedo2 = movieNameDottedo2.replaceAll(".DC.", "").
                        replaceAll(".Pt.", ".Part.");
                String movieNameo2 =
                        normalizeName(movieNameDottedo2.toLowerCase());
                movieNameo2 = StringUtils.replace(movieNameo2,
                        ignore, replacements);
                movieNameo2 = normalizeName(movieNameo2);

                String[] strip = new String[]{
                    "a ","an ","the ","le ","les ", "de ", "het ",
                    "een ", "el ", "la ", "los ", "las "
                };

                movieNameo1 = StringUtils.removeFromStartArr(movieNameo1,
                        strip).trim();
                movieNameo2 = StringUtils.removeFromStartArr(movieNameo2,
                        strip).trim();
                int nameCmp = movieNameo1.compareToIgnoreCase(movieNameo2);
                if (nameCmp == 0) return yearo1.compareToIgnoreCase(yearo2);
                else return nameCmp;
                /*String imdbID = null;
                String mdbID = null;
                List<SearchResult> resList =
                        TMDbSearch.search(movieNameo1 + " " + yearo1);
                if (!resList.isEmpty()) {
                    SearchResult first = resList.get(0);
                    mdbID = first.getMovieID();
                    imdbID = first.getImdbID();
                    imdbID = imdbID.replaceFirst("tt", "");
                } else {
                    imdbID = IMDB.getIMDBID(movieNameo1);
                }*/
            } 
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
        
    }

    private static String normalizeName(String name) {
        name = name.replaceAll("'", "");
        //name = name.replaceAll("\\p{Punct}", " ");
        name = name.replaceAll("\\.", " ");
        name = name.replaceAll("\\p{Space}+", " ");
        name = name.trim();
        return name;
    }
}
