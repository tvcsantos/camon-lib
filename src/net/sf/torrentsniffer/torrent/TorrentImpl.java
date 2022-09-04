/*
 * Created on Jul 5, 2004
 */
package net.sf.torrentsniffer.torrent;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import net.sf.torrentsniffer.bencoding.Bencoding;
import net.sf.torrentsniffer.bencoding.BencodingException;
import net.sf.torrentsniffer.bencoding.BencodingImpl;
import net.sf.torrentsniffer.bencoding.Dictionary;
import net.sf.torrentsniffer.hash.SHA1;
import net.sf.torrentsniffer.hash.SHA1Impl;
import net.sf.torrentsniffer.tracker.ScrapeNotSupportedException;
import net.sf.torrentsniffer.tracker.Tracker;
import net.sf.torrentsniffer.tracker.TrackerImpl;

//import org.apache.log4j.Logger;

/**
 * Implementation of the Torrent.
 * 
 * @author Larry Williams
 *  
 */
public class TorrentImpl implements Torrent {

   /* private final static Logger log = Logger.getLogger(TorrentImpl.class
            .getName());*/

    private Bencoding bencoding = new BencodingImpl();

    private Dictionary root;

    private TorrentInfo torrentInfo;

    private Tracker tracker;

    private String comment;

    private String createdBy;

    private Date creationDate;

    /**
     * Initializes the data in the Torrent.
     * 
     * @param root
     */
    private void initialize(Dictionary root) {
        // Create the TrackerImpl object
        String announce = root.getString("announce");

        comment = root.getString("comment");

        createdBy = root.getString("created by");

        Integer secondsSince1970 = (Integer) root.get("creation date");

        if (secondsSince1970 != null) {
            creationDate = new Date(secondsSince1970.intValue() * 1000);
        }

        this.tracker = new TrackerImpl(announce);
    }

    /**
     * 
     * @param file
     * @throws FileNotFoundException
     */
    public TorrentImpl(String file) throws FileNotFoundException {
        // Get the Root dictionary
        root = (Dictionary) bencoding.decode(file);
        initialize(root);
    }

    /**
     * @param url
     */
    public TorrentImpl(URL url) {
        // Get the Root dictionary
        root = (Dictionary) bencoding.decode(url);
        initialize(root);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.torrentsniffer.torrent.Torrent#getTracker()
     */
    public Tracker getTracker() {
        return tracker;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.torrentsniffer.torrent.TorrentInfo#getComment()
     */
    public String getComment() {
        return comment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.torrentsniffer.torrent.Torrent#getCreationDate()
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.torrentsniffer.torrent.Torrent#getCreatedBy()
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.torrentsniffer.torrent.Torrent#getInfo()
     */
    public TorrentInfo getInfo() {
        // Initialize
        if (torrentInfo == null) {
            torrentInfo = new TorrentInfo();

            Dictionary info = (Dictionary) root.get("info");

            if (info == null) {
                throw new TorrentException(
                        "No 'info' dictionary found in torrent");
            }

            String name = info.getString("name");
            Integer pieceLength = (Integer) info.get("piece length");
            String pieces = info.getString("pieces");

            // Check required fields
            if (name == null) {
                throw new TorrentException(
                        "No 'name' key found in info dictionary");
            }
            if (pieceLength == null) {
                throw new TorrentException(
                        "No 'piece length' key found in info dictionary");
            }
            if (pieces == null) {
                throw new TorrentException(
                        "No 'piece' key found in info dictionary");
            }

            torrentInfo.setName(name);
            torrentInfo.setPieceLength(pieceLength.intValue());
            torrentInfo.setPieces(pieces);

            // Read the file information
            List filesList = (List) info.get("files");
            TorrentFile files[];
            if (filesList == null) {
                // Single file torrent
            	Number length = (Number) info.get("length");
                String md5Sum = (String) info.get("md5sum");
                String filename = info.getString("name");
                // Check required fields
                if (length == null) {
                    throw new TorrentException(
                            "No 'length' key found in single-file info dictionary");
                }
                /*if (md5Sum == null) {
                    throw new TorrentException(
                            "No 'md5sum' key found in single-file info dictionary");
                }*/
                if (filename == null) {
                    throw new TorrentException(
                            "No 'name' key found in single-file info dictionary");
                }
                torrentInfo.setLength(length.longValue());
                /*if (md5Sum.length() > 32) {
                    throw new TorrentException(
                            "MD5 sum should be 32 characters in length");
                }*/
                torrentInfo.setMd5Sum(md5Sum);
                torrentInfo.setSingleFile(true);
            } else {
                // Multi-file torrent
                files = new TorrentFile[filesList.size()];

                Dictionary dictionaries[] = (Dictionary[]) filesList
                        .toArray(new Dictionary[filesList.size()]);

                for (int i = 0; i < dictionaries.length; i++) {
                    Dictionary dictionary = dictionaries[i];
                    TorrentFile torrentFile = new TorrentFile();
                    files[i] = torrentFile;

                    Number length = (Number) dictionary.get("length");
                    String md5Sum = (String) dictionary.get("md5sum");
                    List pathList = (List) dictionary.get("path");

                    // Check required fields
                    if (length == null) {
                        throw new TorrentException(
                                "No 'length' key found in multi-file info dictionary");
                    }
                    if (pathList == null) {
                        throw new TorrentException(
                                "No 'path' key found in multi-file info dictionary");
                    }
                    if (pathList.size() < 1) {
                        throw new TorrentException(
                                "Path list in info dictionary has less than one paths. "
                                        + "It should contain one or more.");
                    }

                    torrentFile.setLength(length.longValue());
                    torrentFile.setMd5Sum(md5Sum);
                    torrentInfo.setLength( torrentInfo.getLength() + length.longValue());

                    StringBuffer path = new StringBuffer();

                    for (int j = 0; j < pathList.size(); j++) {
                        try {
                            path.append(new String((byte[]) pathList.get(j),
                                    BencodingImpl.BYTE_ENCODING));
                        } catch (UnsupportedEncodingException ex) {
                            throw new RuntimeException(ex);
                        }
                        path.append("/");
                    }
                    path.deleteCharAt(path.length() - 1);

                    torrentFile.setPath(path.toString());

                }
                torrentInfo.setMultiFile(files);
            }
        }
        return torrentInfo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.torrentsniffer.torrent.Torrent#scrape()
     */
    public TorrentState getState() {
        Bencoding bencoding = new BencodingImpl();
        TorrentState torrentState = new TorrentState();

        try {
            String scrapeUrl = getTracker().getScrapeUrl();
            // rvdk: fix for urls that already have a parameter
            if (scrapeUrl.contains("?"))
            {
            	scrapeUrl += "&info_hash=";
            }
            else
            {
            	scrapeUrl += "?info_hash=";
            }
            

            String infoHash = new String(getInfoHash(), "ISO-8859-1");
            infoHash = URLEncoder.encode(infoHash, "ISO-8859-1");

            scrapeUrl += infoHash;

         //   log.debug("Scrape URL is '" + scrapeUrl + "'");

            Dictionary dictionary = (Dictionary) bencoding.decode(new URL(
                    scrapeUrl));
            Dictionary files = (Dictionary) dictionary.get("files");

            String keys[] = files.getKeys();

            // We should only get one result back
            if (keys.length != 1) {
                throw new RuntimeException(
                        "Expected one result back from scrape of url '"
                                + scrapeUrl + "' but got " + keys.length);
            }

            Dictionary file = (Dictionary) files.get(keys[0]);

            if (file == null) {
                throw new ScrapeNotSupportedException(
                        "Scrape URL was found but did not return the 'files' Dictionary.");
            }

            Integer downloaded = (Integer) file.get("downloaded");
            if (downloaded == null) {
                throw new ScrapeNotSupportedException(
                        "Required scrape field downloaded was not found.");

            }
            torrentState.setDownloaded((downloaded).intValue());

            Integer incomplete = (Integer) file.get("incomplete");
            if (incomplete == null) {
                throw new ScrapeNotSupportedException(
                        "Required scrape field incomplete was not found.");

            }
            torrentState.setIncomplete((incomplete).intValue());
            Integer complete = (Integer) file.get("complete");
            if (complete == null) {
                throw new ScrapeNotSupportedException(
                        "Required scrape field complete was not found.");

            }
            torrentState.setComplete((complete).intValue());

            torrentState.setName(file.getString("name"));

        } catch (BencodingException ex) {
            throw new TorrentException(ex);
        } catch (MalformedURLException ex) {
            throw new TorrentException(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new TorrentException(ex);
        }
        return torrentState;
    }

    /**
     * Calculate the hash for the info data.
     * 
     * @return
     */
    public byte[] getInfoHash() {
        SHA1 sha1 = new SHA1Impl();
        // Generate the SHA1Impl hash
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Dictionary info = (Dictionary) getRoot().get("info");
        bencoding.encode(buffer, info);
        byte[] hash = sha1.digest(buffer.toByteArray());
        return hash;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.torrentsniffer.torrent.Torrent#getRoot()
     */
    public Dictionary getRoot() {
        return root;
    }
}