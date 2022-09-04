package pt.unl.fct.di.tsantos.util.wdtv;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import pt.unl.fct.di.tsantos.util.download.subtitile.Language;

public class Configuration {
    protected Map<File, MKVSubtitle> subtitles;
    protected int bitRate;
    protected boolean convertAC32DTS;
    protected boolean keepOT;

    public Configuration() {
        subtitles = new HashMap<File, MKVSubtitle>();
        bitRate = - Integer.MAX_VALUE;
        convertAC32DTS = true;
        keepOT = false;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public boolean addSubtitle(File file, Language language,
            boolean isDefault) {
        MKVSubtitle s = new MKVSubtitle(file, language, isDefault);
        boolean contains = this.subtitles.containsKey(file);
        if (!contains) this.subtitles.put(file, s);
        return !contains;
    }

    public boolean removeSubtitle(File file) {
        return this.subtitles.remove(file) != null;
    }

    public boolean hasSubtitles() {
        return !subtitles.isEmpty();
    }

    public Collection<MKVSubtitle> getSubtitles() {
        return subtitles.values();
    }

    public int getBitRate() {
        return bitRate;
    }

    public boolean isConvertAC32DTS() {
        return convertAC32DTS;
    }

    public void setConvertAC32DTS(boolean convertAC32DTS) {
        this.convertAC32DTS = convertAC32DTS;
    }

    public boolean isKeepOT() {
        return keepOT;
    }

    public void setKeepOT(boolean keepOT) {
        this.keepOT = keepOT;
    }

}