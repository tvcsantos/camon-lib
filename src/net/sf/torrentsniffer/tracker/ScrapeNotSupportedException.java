/*
 * Created on Jul 12, 2004
 */
package net.sf.torrentsniffer.tracker;

/**
 * Thrown if the server does not support the Tracker Scrape Convention.
 * See http://groups.yahoo.com/group/BitTorrent/message/3275
 * 
 * @author Larry Williams
 *  
 */
public class ScrapeNotSupportedException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2990686629538163770L;

	/**
     * @param arg0
     */
    public ScrapeNotSupportedException(String message) {
        super(message);
    }
}