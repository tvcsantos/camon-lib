package pt.unl.fct.di.tsantos.util.download.subtitile;

import java.io.Serializable;

public class Language implements Serializable {

    /** Language that represents all languages **/
    public static final Language ALL = new Language("All", "", "");
    /** English language **/
    public static final Language EN = new Language("English", "eng", "en");
    /** Spanish language **/
    public static final Language ES = new Language("Spanish", "spa", "es");
    /** Italian language **/
    public static final Language IT = new Language("Italian", "ita", "it");
    /** French language **/
    public static final Language FR = new Language("French", "fre", "fr");
    /** Portuguese language **/
    public static final Language PT = new Language("Portuguese", "por", "pt");
    /** Brazilian language **/
    public static final Language PB = new Language("Brazilian", "pob", "pb");
    /** German language **/
    public static final Language DE = new Language("German", "ger", "de");
    /** Japanese language **/
    public static final Language JA = new Language("Japanese", "jpn", "ja");
    /** Danish language **/
    public static final Language DA = new Language("Danish", "dan", "da");
    /** Norwegian language **/
    public static final Language NO = new Language("Norwegian", "nor", "no");
    /** Swedish language **/
    public static final Language SV = new Language("Swedish", "swe", "sv");
    /** Arabic language **/
    public static final Language AR = new Language("Arabic", "ara", "ar");
    /** Czech language **/
    public static final Language CS = new Language("Czech", "cze", "cs");
    /** Chinese language **/
    public static final Language ZH = new Language("Chinese", "chi", "zh");
    /** Korean language **/
    public static final Language KO = new Language("Korean", "kor", "ko");
    /** Bulgarian language **/
    public static final Language BG = new Language("Bulgarian", "bul", "bg");
    /** Polish language **/
    public static final Language PL = new Language("Polish", "pol", "pl");
    /** Russian language **/
    public static final Language RU = new Language("Russian", "rus", "ru");
    /** Ukrainian language **/
    public static final Language UK = new Language("Ukrainian", "ukr", "uk");
    /** Greek language **/
    public static final Language EL = new Language("Greek", "ell", "el");
    /** Hungarian language **/
    public static final Language HU = new Language("Hungarian", "hun", "hu");
    /** Turkish language **/
    public static final Language TR = new Language("Turkish", "tur", "tr");
    /** Dutch language **/
    public static final Language NL = new Language("Dutch", "dut", "nl");
    /** Finnish language **/
    public static final Language FI = new Language("Finnish", "fin", "fi");
    /** Romanian language **/
    public static final Language RO = new Language("Romanian", "rum", "ro");
    /** Catalan language **/
    public static final Language CA = new Language("Catalan", "cat", "ca");
    /** Basque language **/
    public static final Language EU = new Language("Basque", "baq", "eu");
    /** Galician language **/
    public static final Language GL = new Language("Galician", "glg", "gl");
    /** Slovenian language **/
    public static final Language SL = new Language("Slovenian", "slv", "sl");
    /** Hebrew language **/
    public static final Language HE = new Language("Hebrew", "heb", "he");
    /** Slovak language **/
    public static final Language SK = new Language("Slovak", "slo", "sk");
    /** Croatian language **/
    public static final Language HR = new Language("Croatian", "hrv", "hr");
    /** Serbian language **/
    public static final Language SR = new Language("Serbian", "srp", "sr");
    /** Indonesian language **/
    public static final Language ID = new Language("Indonesian", "ind", "id");
    /** Malay language **/
    public static final Language MS = new Language("Malay", "may", "ms");
    /** Persian language **/
    public static final Language FA = new Language("Persian", "per", "fa");
    /** Bosnian language **/
    public static final Language BS = new Language("Bosnian", "bos", "bs");
    /** Vietnamese language **/
    public static final Language VI = new Language("Vietnamese", "vie", "vi");
    /** Thai language **/
    public static final Language TH = new Language("Thai", "tha", "th");

    protected String name;
    protected String ISO6392code;
    protected String ISO6391code;

    public Language(String name, String ISO6392code, String ISO6391code) {
        this.name = name;
        this.ISO6391code = ISO6391code;
        this.ISO6392code = ISO6392code;
    }

    public String getISO6391code() {
        return ISO6391code;
    }

    public void setISO6391code(String ISO6391code) {
        this.ISO6391code = ISO6391code;
    }

    public String getISO6392code() {
        return ISO6392code;
    }

    public void setISO6392code(String ISO6392code) {
        this.ISO6392code = ISO6392code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return ISO6392code + " (" + name + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Language other = (Language) obj;
        if ((this.name == null) ? (other.name != null)
                : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.ISO6392code == null) ? (other.ISO6392code != null)
                : !this.ISO6392code.equals(other.ISO6392code)) {
            return false;
        }
        if ((this.ISO6391code == null) ? (other.ISO6391code != null)
                : !this.ISO6391code.equals(other.ISO6391code)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 29 * hash + (this.ISO6392code != null ?
            this.ISO6392code.hashCode() : 0);
        hash = 29 * hash + (this.ISO6391code != null ?
            this.ISO6391code.hashCode() : 0);
        return hash;
    }

    
}
