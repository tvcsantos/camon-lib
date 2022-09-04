/*
 * Created on Jul 10, 2004
 */
package net.sf.torrentsniffer.bencoding;

/**
 * Thrown if something went wrong while decoding or encoding bencoded data.
 * 
 * @author Larry Williams
 *  
 */
public class BencodingException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4089626122537278176L;

	/**
     * 
     * @param message
     * @param exception
     */
    public BencodingException(String message, Throwable exception) {
        super(message, exception);
    }

    /**
     * @param ex
     */
    public BencodingException(Throwable ex) {
        super(ex);
    }

    /**
     * @param string
     */
    public BencodingException(String message) {
        super(message);
    }
}