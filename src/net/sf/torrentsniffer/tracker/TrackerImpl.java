/*
 * Created on Jul 12, 2004
 */
package net.sf.torrentsniffer.tracker;

import java.net.MalformedURLException;
import java.net.URL;

import net.sf.torrentsniffer.bencoding.Bencoding;
import net.sf.torrentsniffer.bencoding.BencodingException;
import net.sf.torrentsniffer.bencoding.BencodingImpl;
import net.sf.torrentsniffer.bencoding.Dictionary;
import net.sf.torrentsniffer.torrent.TorrentException;
import net.sf.torrentsniffer.torrent.TorrentState;

/**
 * Implementation of the Torrent's TrackerImpl.
 * 
 * @author Larry Williams
 *  
 */
public class TrackerImpl implements Tracker {

    private String announce;

    private String scrapeUrl;

    public TrackerImpl(String url) {
        this.announce = url;
        if (url == null) {
            throw new TorrentException("Announce URL cannot be null");
        }

    }

    /**
     * @return Returns the url.
     */
    public String getAnnounce() {
        return announce;
    }

    /**
     * @param url
     *            The url to set.
     */
    public void setAnnounce(String url) {
        this.announce = url;
    }

    /**
     * Tries to retrieve the state of all torrents tracked by this server. Uses
     * scraping to do this. Not all trackers support this.
     * 
     * @return
     */
    public TorrentState[] getTrackerState() {
        Bencoding bencoding = new BencodingImpl();
        try {
            String scrapeUrl = getScrapeUrl();

            Dictionary dictionary = (Dictionary) bencoding.decode(new URL(
                    scrapeUrl));
            // The list of torrents tracked by this tracker
            Dictionary files = (Dictionary) dictionary.get("files");
            TorrentState torrentStates[] = new TorrentState[files.size()];

            String keys[] = files.getKeys();
            for (int i = 0; i < keys.length; i++) {
                Dictionary file = (Dictionary) files.get(keys[i]);
                TorrentState torrentState = new TorrentState();
                torrentStates[i] = torrentState;
                torrentState.setDownloaded(((Integer) file.get("downloaded"))
                        .intValue());
                torrentState.setIncomplete(((Integer) file.get("incomplete"))
                        .intValue());
                torrentState.setComplete(((Integer) file.get("complete"))
                        .intValue());
                torrentState.setName((String) file.get("name"));
            }
        } catch (BencodingException ex) {
            throw new TorrentException(ex);
        } catch (MalformedURLException ex) {
            throw new TorrentException(ex);
        }
        return null;
    }

    /**
     * Retrieves the scrape url for the tracker.
     * 
     * Taken from http://groups.yahoo.com/group/BitTorrent/message/3275
     * 
     * Take the tracker url. Find the last '/' in it. If the text immediately
     * following that '/' isn't 'announce' it will be taken as a sign that that
     * tracker doesn't support the scrape convention. If it does, substitute
     * 'scrape' for 'announce' to find the scrape page.
     * 
     * @return
     */
    public String getScrapeUrl() {
        if (scrapeUrl == null) {
            int slashIndex = announce.lastIndexOf('/');
            if (slashIndex == -1) {
                throw new ScrapeNotSupportedException(
                        "Could not find a / in the announce URL '" + announce
                                + "'");
            }

            int announceIndex = announce.indexOf("announce", slashIndex);
            if (announceIndex == -1) {
                throw new ScrapeNotSupportedException(
                        "Could not find 'announce' after the last / in the announce URL '"
                                + announce + "'");
            }

            if ((slashIndex == (announceIndex - 1)) == false) {
                throw new ScrapeNotSupportedException(
                        "Could not find 'announce' after the last / in the announce URL '"
                                + announce + "'");
            }

            // Get text including the last slash
            scrapeUrl = announce.substring(0, slashIndex + 1);
            // Replace the announce with scrape
            scrapeUrl += "scrape";
            // Get text after announce
            scrapeUrl += announce.substring(
                    announceIndex + "announce".length(), announce.length());

        }
        return scrapeUrl;
    }
}