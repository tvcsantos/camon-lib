package pt.unl.fct.di.tsantos.util.download.subtitile;

public class DefaultSubtitleDescriptor extends AbstractSubtitleDescriptor
    implements SubtitleDescriptor {
    protected String subFileName;
    protected String subHash;
    protected String ISO639;
    protected String subLink;
    protected String subDownloadLink;
    protected String data;
    protected String downFileName;

    public DefaultSubtitleDescriptor(String subFileName, String ISO639) {
        this.subFileName = subFileName;
        this.ISO639 = ISO639;
    }

    public String getISO639() {
        return ISO639;
    }

    public void setISO639(String ISO639) {
        this.ISO639 = ISO639;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDownFileName() {
        return downFileName;
    }

    public void setDownFileName(String downFileName) {
        this.downFileName = downFileName;
    }

    public String getSubDownloadLink() {
        return subDownloadLink;
    }

    public void setSubDownloadLink(String subDownloadLink) {
        this.subDownloadLink = subDownloadLink;
    }

    public String getSubFileName() {
        return subFileName;
    }

    public void setSubFileName(String subFileName) {
        this.subFileName = subFileName;
    }

    public String getSubHash() {
        return subHash;
    }

    public void setSubHash(String subHash) {
        this.subHash = subHash;
    }

    public String getSubLink() {
        return subLink;
    }

    public void setSubLink(String subLink) {
        this.subLink = subLink;
    }

    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLanguageName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}