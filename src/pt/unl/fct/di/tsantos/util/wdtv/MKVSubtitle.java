package pt.unl.fct.di.tsantos.util.wdtv;

import java.io.File;
import pt.unl.fct.di.tsantos.util.download.subtitile.Language;

public class MKVSubtitle {

    private File file;
    private Language language;
    private boolean isDefault;

    public MKVSubtitle(File file, Language language, boolean isDefault) {
        this.file = file;
        this.language = language;
        this.isDefault = isDefault;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MKVSubtitle other = (MKVSubtitle) obj;
        if (this.file != other.file && (this.file == null
                || !this.file.equals(other.file))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.file != null ? this.file.hashCode() : 0);
        return hash;
    }

    
}
