/*
 * Created on Jul 11, 2004
 */
package net.sf.torrentsniffer.torrent;

/**
 * Represent a file in a Torrent. A torrent can contain one or more files.
 * 
 * @author Larry Williams
 *  
 */
public class TorrentFile {
    private long length;

    private String md5Sum;

    private String path;

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path
     *            The path to set.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return Returns the md5Sum.
     */
    public String getMd5Sum() {
        return md5Sum;
    }

    /**
     * @param md5Sum
     *            The md5Sum to set.
     */
    public void setMd5Sum(String md5Sum) {
        this.md5Sum = md5Sum;
    }

    /**
     * @return Returns the length.
     */
    public long getLength() {
        return length;
    }

    /**
     * @param length
     *            The length to set.
     */
    public void setLength(long length) {
        this.length = length;
    }

}