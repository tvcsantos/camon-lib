/*
 * Created on Jul 11, 2004
 */
package net.sf.torrentsniffer.torrent;

/**
 * Thrown if an error occurs while processing the Torrent.
 * 
 * @author Larry Williams
 *  
 */
public class TorrentException extends RuntimeException {
    /**
     * 
     * @param message
     * @param exception
     */
    public TorrentException(String message, Throwable exception) {
        super(message, exception);
    }

    /**
     * @param ex
     */
    public TorrentException(Throwable ex) {
        super(ex);
    }

    /**
     * @param string
     */
    public TorrentException(String message) {
        super(message);
    }
}