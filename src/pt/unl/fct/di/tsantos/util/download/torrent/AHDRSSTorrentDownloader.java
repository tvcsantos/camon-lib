package pt.unl.fct.di.tsantos.util.download.torrent;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pt.unl.fct.di.tsantos.util.net.RSSFeed;
import pt.unl.fct.di.tsantos.util.download.Downloader;
import pt.unl.fct.di.tsantos.util.imdb.IMDB;

/**
 *
 * @author tvcsantos
 */

class Movie {
    String name;
    int year;
}

class AHDSource {

    RSSFeed feed;

    /*Media getMedia(SyndEntry entry) {
        return null;
    }*/
}

public class AHDRSSTorrentDownloader implements Downloader {

    protected List<Movie> movieList;
    protected List<AHDSource> sourceList;

    public AHDRSSTorrentDownloader(List<Movie> list, List<AHDSource> list2) {
        movieList = list;
        sourceList = list2;
    }

    public List<File> download() throws IOException {
        //throw new UnsupportedOperationException("Not supported yet.");
        for (AHDSource s : sourceList) {
            for (Movie m : movieList) {
                try {
                    RSSFeed feed = s.feed;
                    SyndFeed parseFeed = feed.parseFeed();
                    List<SyndEntry> entries = parseFeed.getEntries();
                    for (SyndEntry entry : entries) {
                        String title = entry.getTitle();
                        Matcher ma = Pattern.compile(
                    "MOVIE:([^\\[\\]]+)\\[([^/]+)\\] -([^/]+)/([^/]+)/([^/]+)/([^/]+)/([^/]+)(?:/([^/]+))?(?:/([^/]+))?(?:/([^/]+))?",
                                Pattern.CASE_INSENSITIVE).matcher(title);
                        System.out.println(ma.groupCount());
                        if (ma.matches()) {
                            /*for (int i = 0; i <= ma.groupCount(); i++) {
                                System.out.println(ma.group(i));
                            }*/
                            String etitle = ma.group(1);
                            etitle = etitle != null ?
                                etitle.trim() : etitle;
                            String eyear = ma.group(2);
                            eyear = eyear != null ? eyear.trim() : eyear;
                            String group = ma.group(3);
                            group = group != null ? group.trim() : group;
                            String media = ma.group(4);
                            media = media != null ? media.trim() : media;
                            String resolution = ma.group(5);
                            resolution = resolution != null ? resolution.trim():
                                resolution;
                            String ftype = ma.group(6);
                            ftype = ftype != null ? ftype.trim() : ftype;
                            String audiof = ma.group(7);
                            audiof = audiof != null ? audiof.trim() : audiof;
                            String encstat1 = ma.group(8);
                            encstat1 = encstat1 != null ? encstat1.trim() : encstat1;
                            String encstat2 = ma.group(9);
                            encstat2 = encstat2 != null ? encstat2.trim() : encstat2;
                            String fl = ma.group(10);
                            fl = fl != null ? fl.trim() : fl;
                            int xi = fl != null ? fl.indexOf("%") : -1;
                            if (xi != -1) fl = fl.substring(0, xi);
                            System.out.println(etitle + " " + IMDB.getIMDBID(etitle));
                            System.out.println(eyear);
                            System.out.println(group);
                            System.out.println(media);
                            System.out.println(resolution);
                            System.out.println(ftype);
                            System.out.println(audiof);
                            System.out.println(encstat1);
                            System.out.println(encstat2);
                            System.out.println(fl);
                        }
                    }
                } catch (MalformedURLException ex) {
                    Logger.getLogger(AHDRSSTorrentDownloader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(AHDRSSTorrentDownloader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (FeedException ex) {
                    Logger.getLogger(AHDRSSTorrentDownloader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
        //Source s: iterate sourceList
        //  Media m: iterate mediaList
        //      Entry e: iterate over s entries
        //          build info from entry
        //          check best match
        //
        //download best match
    }
}