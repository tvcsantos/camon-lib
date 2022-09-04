/*
 * Created on Jul 15, 2004
 */
package net.sf.torrentsniffer.hash;

/**
 * Interface for classes that can encode data with the SHA-1 algorithm.
 * 
 * 
 * @author Larry Williams
 *  
 */
public interface SHA1 {
    /**
     * Hash data using the SHA1Impl algorithm.
     * 
     * @param data
     * @return
     */
    public abstract byte[] digest(byte[] data);
}