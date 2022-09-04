/*
 * Created on Jul 11, 2004
 */
package net.sf.torrentsniffer.torrent;

/**
 * Represents the Dictionary that describes the file(s) of the torrent. There
 * are two possible forms: one for the case of a 'single-file' torrent with no
 * directory structure, and one for the case of a 'multi-file' torrent, which
 * can contain subdirectory trees.
 * <p>
 * The isSingleFile returns true if the torrent consists of only one file. The
 * name and length attributes contain the information of the file.
 * <p>
 * If the torrent consists of multiple files isSingleFile returns false. And the
 * getMultiFile method will return the information of all the files.
 * 
 * @author Larry Williams
 *  
 */
public class TorrentInfo {

    private String name;

    private long length;

    private String md5Sum;

    private int pieceLength;

    private boolean isSingleFile;

    private String pieces;

    private TorrentFile multiFile[];

    /**
     * @return Returns the multiFile.
     */
    public TorrentFile[] getMultiFile() {
        return multiFile;
    }

    /**
     * @param multiFile
     *            The multiFile to set.
     */
    public void setMultiFile(TorrentFile[] multiFile) {
        this.multiFile = multiFile;
    }

    /**
     * @param isSingleFile
     *            The isSingleFile to set.
     */
    public void setSingleFile(boolean isSingleFile) {
        this.isSingleFile = isSingleFile;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the isSingleFile.
     */
    public boolean isSingleFile() {
        return isSingleFile;
    }

    /**
     * @return Returns the pieceLength.
     */
    public int getPieceLength() {
        return pieceLength;
    }

    /**
     * @param pieceLength
     *            The pieceLength to set.
     */
    public void setPieceLength(int pieceLength) {
        this.pieceLength = pieceLength;
    }

    /**
     * @return Returns the pieces.
     */
    public String getPieces() {
        return pieces;
    }

    /**
     * @param pieces
     *            The pieces to set.
     */
    public void setPieces(String pieces) {
        this.pieces = pieces;
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
}