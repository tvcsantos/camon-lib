package pt.unl.fct.di.tsantos.util.download.subtitile;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.tuned.FileUtilities;

/**
 * Describes a subtitle on OpenSubtitles.
 * 
 * @see OpenSubtitlesXmlRpc
 */
public class OpenSubtitlesSubtitleDescriptor
        extends AbstractSubtitleDescriptor implements SubtitleDescriptor {

    protected String data;
    protected String downFileName;

    public String getSubHash() {
        return getProperty(Property.SubHash);
    }

    public void setSubHash(String hash) {
        properties.put(Property.SubHash, hash);
    }

    public String getISO639() {
        return getProperty(Property.ISO639);
    }

    public void setISO639(String iso639) {
        properties.put(Property.ISO639, iso639);
    }

    public String getSubLink() {
        return getProperty(Property.SubtitlesLink);
    }

    public void setSubLink(String subLink) {
        properties.put(Property.SubtitlesLink, subLink);
    }

    public String getSubDownloadLink() {
        return getProperty(Property.SubDownloadLink);
    }

    public void setSubDownloadLink(String subDownloadLink) {
        properties.put(Property.SubDownloadLink, subDownloadLink);
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

    public String getSubFileName() {
        return getProperty(Property.SubFileName);
    }

    public void setSubFileName(String subFileName) {
        properties.put(Property.SubFileName, subFileName);
    }

    public static enum Property {

        IDSubtitle,
        IDSubtitleFile,
        IDSubMovieFile,
        IDMovie,
        IDMovieImdb,
        SubFileName,
        SubFormat,
        SubHash,
        SubSize,
        MovieHash,
        MovieByteSize,
        MovieName,
        MovieNameEng,
        MovieYear,
        MovieReleaseName,
        MovieTimeMS,
        MovieImdbRating,
        SubLanguageID,
        ISO639,
        LanguageName,
        UserID,
        UserNickName,
        SubAddDate,
        SubAuthorComment,
        SubComments,
        SubDownloadsCnt,
        SubRating,
        SubBad,
        SubActualCD,
        SubSumCD,
        MatchedBy,
        SubtitlesLink,
        SubDownloadLink,
        ZipDownloadLink;

        public static <V> EnumMap<Property, V>
                asEnumMap(Map<String, V> stringMap) {
            EnumMap<Property, V> enumMap =
                    new EnumMap<Property, V>(Property.class);

            // copy entry set to enum map
            for (Entry<String, V> entry : stringMap.entrySet()) {
                try {
                    enumMap.put(Property.valueOf(entry.getKey()),
                            entry.getValue());
                } catch (IllegalArgumentException e) {
                    // illegal enum constant, just ignore
                }
            }

            return enumMap;
        }
    }
    private final Map<Property, String> properties;

    public OpenSubtitlesSubtitleDescriptor(Map<Property, String> properties) {
        this.properties = properties;
    }

    public String getProperty(Property key) {
        return properties.get(key);
    }

    @Override
    public String getName() {
        return FileUtilities.getNameWithoutExtension(
                getProperty(Property.SubFileName));
    }

    @Override
    public String getLanguageName() {
        return getProperty(Property.LanguageName);
    }

    @Override
    public String getType() {
        return getProperty(Property.SubFormat);
    }

    public int getSize() {
        return Integer.parseInt(
                getProperty(Property.SubSize));
    }

    public String getMovieHash() {
        return getProperty(Property.MovieHash);
    }

    public long getMovieByteSize() {
        return Long.parseLong(getProperty(Property.MovieByteSize));
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", getName(), getLanguageName());
    }

}
