/*
 * Created on Jul 5, 2004
 */
package net.sf.torrentsniffer.bencoding;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Implements the bencoding specification.
 * 
 * See http://wiki.theory.org/index.php/BitTorrentSpecification for information
 * about bencoding.
 * 
 * @author Larry Williams
 *  
 */
public interface Bencoding {

    /**
     * Decodes the bencoded data in a torrent file.
     * 
     * @param file
     * @throws FileNotFoundException
     * @return
     */
    Dictionary decode(String file) throws FileNotFoundException;

    /**
     * Decodes the bencoded data in a stream.
     * 
     * @param input
     * @return
     */
    Dictionary decode(InputStream input);

    /**
     * Decodes the bencoded data in a URL.
     * 
     * @param url
     * @return
     */
    Dictionary decode(URL url);

    /**
     * Encodes a graph of objects.
     * 
     * @param object
     * @param out
     * @return
     */
    void encode(OutputStream out, Object object);

    /**
     * Checks if two bencoded Dictionaries are the same. Assuming a bencoded
     * file always starts with a Dictionary.
     * 
     * @return
     */
    boolean match(Dictionary bencoded1, Dictionary bencoded2);

}