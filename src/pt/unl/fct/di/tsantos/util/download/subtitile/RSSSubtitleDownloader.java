package pt.unl.fct.di.tsantos.util.download.subtitile;

import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedFormatException;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedLanguageException;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import net.sourceforge.tuned.FilterIterator;
import pt.unl.fct.di.tsantos.util.Pair;
import pt.unl.fct.di.tsantos.util.net.RSSFeed;
import pt.unl.fct.di.tsantos.util.math.Distance;

public abstract class RSSSubtitleDownloader extends SubtitleDownloader {

    protected Map<RSSFeed, Pair<SyndFeed, List<SyndEntry>>> rssFeedCache;

    public RSSSubtitleDownloader(List<String> fileNames, Set<Language> langs,
            File saveDirectory) {
        super(fileNames, langs, saveDirectory);
        rssFeedCache = new HashMap<RSSFeed, Pair<SyndFeed, List<SyndEntry>>>();
    }

    public RSSSubtitleDownloader(List<String> fileNames, File saveDirectory) {
        super(fileNames, saveDirectory);
        rssFeedCache = new HashMap<RSSFeed, Pair<SyndFeed, List<SyndEntry>>>();
    }

    public RSSSubtitleDownloader(List<String> fileNames, Set<Language> langs,
            File saveDirectory, String user, String password) {
        super(fileNames, langs, saveDirectory, user, password);
        rssFeedCache = new HashMap<RSSFeed, Pair<SyndFeed, List<SyndEntry>>>();
    }

    public RSSSubtitleDownloader(List<String> fileNames, File saveDirectory,
            String user, String password) {
        super(fileNames, saveDirectory, user, password);
        rssFeedCache = new HashMap<RSSFeed, Pair<SyndFeed, List<SyndEntry>>>();
    }

    protected final RSSFeed getRSSFeed(Language lang)
            throws UnsupportedLanguageException
    {
        if (!isSupported(lang))
            throw new UnsupportedLanguageException(lang.getName() +
                " not supported by this subtitle downloader");
        return getSupportedRSSFeed(lang);
    }

    protected abstract RSSFeed getSupportedRSSFeed(Language lang);

    protected abstract List<Pair<SubtitleAttributes, SubtitleDescriptor>> 
            getInfoFromSyndEntry(SyndEntry se, Language lang)
            throws UnsupportedFormatException, IOException;

    protected abstract SyndEntry filterSyndEntry(SubtitleAttributes sa,
            SyndEntry se, Language lang);

    @Override
    protected void preDownload() throws IOException {
        super.preDownload();
        rssFeedCache.clear();
    }

    protected final SubtitleDescriptor searchSubtitle(String fileName,
            final SubtitleAttributes thisProps, final Language lang)
            throws IOException {
        String thisName = thisProps.getName();
        Pair<Integer, SortedSet<Integer>> thisSeasonEpisodes =
                thisProps.getSeasonEpisodes();
        int l2 = thisName.length();

        try {           

            RSSFeed feed = getRSSFeed(lang);

            /** cache feeds to avoid parsing **/
            Pair<SyndFeed, List<SyndEntry>> fcahce = rssFeedCache.get(feed);
            List<SyndEntry> entries = new LinkedList<SyndEntry>();
            if (fcahce == null) {
                SyndFeed sf = feed.parseFeed();
                entries = sf.getEntries();
                rssFeedCache.put(feed,
                        new Pair<SyndFeed, List<SyndEntry>>(sf, entries));
            } else {
                System.out.println("CACHE HIT!");
                entries = fcahce.getSnd();
            }
            
            SyndEntry bestMatchEntry = null;
            Pair<SubtitleAttributes, SubtitleDescriptor> bestAttributes = null;

            /** filter wrong language, wrong ep number while iterating **/
            FilterIterator<SyndEntry, SyndEntry> fit =
                    new FilterIterator<SyndEntry, SyndEntry>(entries) {
                @Override
                protected SyndEntry filter(SyndEntry sourceValue) {
                    return filterSyndEntry(thisProps, sourceValue, lang);
                }
            };

            while (fit.hasNext()) {
                SyndEntry entry = fit.next();
                List<Pair<SubtitleAttributes, SubtitleDescriptor>> 
                        thatListProps = new LinkedList<Pair<SubtitleAttributes,
                                                        SubtitleDescriptor>>();
                try {
                    thatListProps = getInfoFromSyndEntry(entry, lang);
                } catch (UnsupportedFormatException ex) {
                    logger.warning(ex.toString());
                    continue;
                } catch (IOException ex) {
                    logger.warning(ex.toString());
                    continue;
                }
                
                for (Pair<SubtitleAttributes, SubtitleDescriptor> p :
                           thatListProps) {
                    SubtitleAttributes thatProps = p.getFst();
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
                        SubtitleAttributes s = bestAttributes == null ?
                            null : bestAttributes.getFst();
                        if (SubtitleAttributes.better(thatProps,
                                s, thisProps)) {
                            bestAttributes = p;
                            bestMatchEntry = entry;
                        }
                    }
                }
                
            }
            if (bestMatchEntry == null) return null;
            SubtitleDescriptor si = getSubtitleDescriptor(bestAttributes,
                    bestMatchEntry, lang);
            return si;
        } catch (MalformedURLException ex) {
        } catch (IllegalArgumentException ex) {
        } catch (FeedException ex) {
        } catch (UnsupportedLanguageException ex) {
        }

        return null;
    }

    protected abstract SubtitleDescriptor getSubtitleDescriptor(
            Pair<SubtitleAttributes, SubtitleDescriptor> pair,
            SyndEntry entry, Language lang)
            throws IOException;
}
