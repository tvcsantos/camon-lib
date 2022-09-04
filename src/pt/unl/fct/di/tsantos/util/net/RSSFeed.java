package pt.unl.fct.di.tsantos.util.net;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RSSFeed {

    public enum Type {

        GENERATED, DEFINED, ORIGINAL
    };
    
    private static final SyndFeedInput input = new SyndFeedInput();
    private URL url;
    private Type type;

    public RSSFeed(URL url, Type type) {
        this.url = url;
        this.type = type;
    }

    public URL getURL() {
        return this.url;
    }

    public Type getType() {
        return this.type;
    }

    public void browse() throws IOException {
        try {
            java.awt.Desktop.getDesktop().browse(url.toURI());
        } catch (URISyntaxException e) {
            IOException ex = new IOException(e);
            //ex.initCause(e);
            throw ex;
        }
    }

    public SyndFeed parseFeed() throws MalformedURLException,
            IOException, IllegalArgumentException, FeedException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        BufferedInputStream stream =
                new BufferedInputStream(conn.getInputStream());
        XmlReader xmlReader = new XmlReader(stream);
        SyndFeed feed = input.build(xmlReader);
        return feed;
    }

    @Override
    public String toString() {
        return url.toString();
    }

    public static RSSFeed getRSSFeed(URL url, RSSFeed.Type type) {
        return new RSSFeed(url, type);
    }

    public static RSSFeed getRSSFeed(String s, RSSFeed.Type type)
            throws MalformedURLException {
        return new RSSFeed(new URL(s), type);
    }
    
    public static RSSFeed getRSSFeedSilent(String s, RSSFeed.Type type) {
        try {
            return new RSSFeed(new URL(s), type);
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    public static int getSeedersFromRSS(SyndEntry item) {
        int numberOfSeeders = -1;

        // First try to get the seeders from the title of the RSS item.
        numberOfSeeders = getSeedersFromRssTitle(item.getTitle());

        if (numberOfSeeders == -1) {
            // If the seeders weren't in the title
            // try to get it from the description.
            numberOfSeeders = getSeedersFromRssDescription(item);
        }

        // Write the seeders to the log, even
        // when it isn't found (for debugging).

        return numberOfSeeders;
    }

    public static int getSeedersFromRssTitle(String rssItemTitle) {
        if (rssItemTitle.equals("")) {
            return -1;
        }

        Pattern seedersPattern = Pattern.compile("(\\[(\\d+)/(\\d+)\\])");
        Matcher seedersMatcher = seedersPattern.matcher(rssItemTitle);

        int nrOfSeeders = 0;
        if (seedersMatcher.find()) {
            String matchedString = seedersMatcher.group(2);

            try {
                // Retrieve the number from the string.
                nrOfSeeders = Integer.parseInt(matchedString);
            } catch (Exception e) {
                // Return if it has failed.
                return -1;
            }

            if (nrOfSeeders < 0) {
                // Seeders are not available in the description.
                return -1;
            }
        } else {
            // Seeders not found.
            return -1;
        }

        return nrOfSeeders;
    }

    public static int getSeedersFromRssDescription(SyndEntry rssItem) {
        // See if there is a description.
        SyndContent rssContent = rssItem.getDescription();
        if (rssContent == null) {
            // No description available.
            return -1;
        }

        // Retrieve the description.
        String description = rssContent.getValue().toLowerCase();

        // Search for the seeders in the description.
        Pattern seedersPattern = Pattern.compile("((-*)(\\d+))(.+)(seeds)");
        Matcher seedersMatcher = seedersPattern.matcher(description);

        // First see if the seeders can be found in the description.
        int nrOfSeeders = 0;
        if (seedersMatcher.find()) {
            // Retrieve seeders.
            // Group 0 is the entire matched string.
            // Group 1 is the number of seeders.
            String matchedString = seedersMatcher.group(1);

            try {
                // Retrieve the number from the string.
                nrOfSeeders = Integer.parseInt(matchedString);
            } catch (Exception e) {
                // Return if it has failed.
                return -1;
            }

            if (nrOfSeeders < 0) {
                // Seeders are not available in the description.
                return -1;
            }
        } else {
            // Seeders not found.
            return -1;
        }

        return nrOfSeeders;
    }
}
