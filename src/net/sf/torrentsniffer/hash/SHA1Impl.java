/*
 * Created on Jul 12, 2004
 */
package net.sf.torrentsniffer.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class is used for encoding data with the SHA-1 algorithm.
 *  
 * @todo Could use own SHA-1 implementation
 * @author Larry Williams
 *  
 */
public class SHA1Impl implements SHA1 {

    /**
     * Hash data using the SHA1Impl algorithm.
     * 
     * @param data
     * @return
     */
    public byte[] digest(byte[] data) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        md.update(data);

        byte[] digest = md.digest();

        return digest;
    }
}