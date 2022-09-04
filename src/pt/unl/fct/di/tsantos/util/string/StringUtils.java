package pt.unl.fct.di.tsantos.util.string;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pt.unl.fct.di.tsantos.util.FileUtils;
import pt.unl.fct.di.tsantos.util.io.StringOutputStream;

public class StringUtils {

    private StringUtils() {}

    public static String getString(InputStream is) throws IOException {
        StringOutputStream os = new StringOutputStream();
        FileUtils.copy(is, os);
        is.close();
        os.close();
        return os.getString();
    }

    public static String removeFromEnd(String s, String rem) {
        if (s.endsWith(rem)) {
            int i = s.lastIndexOf(rem);
            return s.substring(0, i);
        }
        return s;
    }

    public static String removeFromStart(String s, String rem) {
        if (s.startsWith(rem)) {
            int i = s.indexOf(rem);
            return s.substring(i + rem.length(), s.length());
        }
        return s;
    }

    public static String removeFromStartArr(String s, String[] rems) {
        for (String rem : rems) s = removeFromStart(s, rem);
        return s;
    }

    public static String toLowerCase(String s) {
        return s == null ? null : s.toLowerCase();
    }

    public static String replace(String original, CharSequence[] targets,
            CharSequence[] replacements) {
        if (original == null) return null;
        if (targets == null || replacements == null)
            throw new NullPointerException();
        if (targets.length != replacements.length)
            throw new RuntimeException();
        if (original.length() <= 0) return original;
        String res = original;
        for (int i = 0; i < targets.length; i++) {
            if (targets[i] == null || replacements[i] == null)
                throw new NullPointerException();
            res = res.replace(targets[i], replacements[i]);
        }
        return res;
    }

    public static String replaceFirst(String original,
            String[] regexs, String[] replacements) {
        if (original == null) return null;
        if (regexs == null || replacements == null)
            throw new NullPointerException();
        if (regexs.length != replacements.length)
            throw new RuntimeException();
        String res = original;
        for (int i = 0; i < regexs.length; i++) {
            if (regexs[i] == null || replacements[i] == null)
                throw new NullPointerException();
            res = res.replaceFirst(regexs[i], replacements[i]);
        }
        return res;
    }

    public static String replaceAll(String original,
            String[] regexs, String[] replacements) {
        if (original == null) return null;
        if (regexs == null || replacements == null)
            throw new NullPointerException();
        if (regexs.length != replacements.length)
            throw new RuntimeException();
        String res = original;
        for (int i = 0; i < regexs.length; i++) {
            if (regexs[i] == null || replacements[i] == null)
                throw new NullPointerException();
            res = res.replaceAll(regexs[i], replacements[i]);
        }
        return res;
    }

    public static String lead(String s) {
        return s == null ? null : s.replaceAll("^\\s+", "");
    }

    public static String trail(String s) {
        return s == null ? null : s.replaceAll("\\s+$", "");
    }

    public static String stringToHTML(String s) {
        return stringToHTML(s, false, false);
    }

    public static String stringToHTML(String s, int max) {
        return stringToHTML(s, max, false, false);
    }

    public static String stringToHTML(String s, boolean bold, boolean emph) {
        return stringToHTML(s, Integer.MAX_VALUE, bold, emph);
    }

    public static String stringToHTML(String s, int max, boolean bold,
            boolean emph) {
        List<String> words = getWords(s);
        for (String w : words) if (w.length() > max) max = w.length();
        int length = 0;
        String res = "<html>" + (bold ? "<b>" : "") + (emph ? "<i>" : "");
        Iterator<String> it = words.iterator();
        if (it.hasNext()) {
            String w = it.next();
            res += w;
            length += w.length();
        }
        int breaks = 0;
        while (it.hasNext()) {
            String w = it.next();
            if (length + 1 + w.length() > max) {
                if (breaks >= 2) {
                    res += " ...";
                    break;
                }
                res += "<br>" + w;
                length = w.length();
                breaks++;
            } else {
                res += " " + w;
                length += 1 + w.length();
            }
        }
        return res;
    }

    public static List<String> getWords(String s) {
        StringTokenizer st = new StringTokenizer(s);
        List<String> res = new LinkedList<String>();
        while (st.hasMoreTokens()) res.add(st.nextToken());
        return res;
    }

    public static java.util.List<java.io.File>
            textURIListToFileList(String data) {
        java.util.List<java.io.File> list =
                new java.util.ArrayList<java.io.File>(1);
        for (java.util.StringTokenizer st =
                new java.util.StringTokenizer(data, "\r\n");
                st.hasMoreTokens();) {
            String s = st.nextToken();
            if (s.startsWith("#")) {
                // the line is a comment (as per the RFC 2483)
                continue;
            }
            try {
                java.net.URI uri = new java.net.URI(s);
                java.io.File file = new java.io.File(uri);
                list.add(file);
            } catch (java.net.URISyntaxException e) {
                // malformed URI
            } catch (IllegalArgumentException e) {
                // the URI is not a valid 'file:' URI
            }
        }
        return list;
    }

    static String[] SEQ = new String[] {
        "\\{?\\\\`\\{?(.)\\}?\\}?", "\\{?\\\\'\\{?(.)\\}?\\}?",
        "\\{?\\\\\\^\\{?(.)\\}?\\}?", "\\{?\\\\\"\\{?(.)\\}?\\}?",
        "\\{?\\\\H\\{?(.)\\}?\\}?", "\\{?\\\\~\\{?(.)\\}?\\}?",
        "\\{?\\\\c\\{?(.)\\}?\\}?", "\\{?\\\\=\\{?(.)\\}?\\}?",
        "\\{?\\\\b\\{?(.)\\}?\\}?", "\\{?\\\\\\.\\{?(.)\\}?\\}?",
        "\\{?\\\\d\\{?(.)\\}?\\}?", "\\{?\\\\r\\{?(.)\\}?\\}?",
        "\\{?\\\\u\\{?(.)\\}?\\}?", "\\{?\\\\v\\{?(.)\\}?\\}?",
        "\\{?\\\\t\\{?(.)\\}?\\}?"
    };
      
    static String[] REP = new String[] {
        "\u0300", "\u0301",
        "\u0302", "\u0308",
        "\u030B", "\u0303",
        "\u0327", "\u0304",
        "\u0331", "\u0307",
        "\u0323", "\u030A",
        "\u0306", "\u030C",
        "\u0311"
    };
    
    static String[] REREP = new String[] {
    	"\\\\`{%s}", "\\\\'{%s}",
        "\\\\^{%s}", "\\\\\"{%s}",
        "\\\\H{%s}", "\\\\~{%s}",
        "\\\\c{%s}", "\\\\={%s}",
        "\\\\b{%s}", "\\\\.{%s}",
        "\\\\d{%s}", "\\\\r{%s}",
        "\\\\u{%s}", "\\\\v{%s}",
        "\\t\\{%s\\}"
    };
    
    static String[] SPECIALSEQ = new String[] {
    	"\\{?\\\\k\\{?a\\}?\\}?",
    	"\\{?\\\\k\\{?A\\}?\\}?",
    	"\\{?\\\\l\\}?",
        "\\\\#"
    };
    
    static String[] SPECIALREP = new String[] {
    	"\u0328a",
    	"\u0328A",
    	"\u0142",
        "#"
    };
    
    static String[] SPECIALREREP = new String[] {
    	"\\\\k{a}",
    	"\\\\k{A}",
    	"\\\\{\\\\l}",
        "\\#"
    };
    
    public static String escapeLatex(String s) {
        String norm = Normalizer.normalize(s, Normalizer.Form.NFD);
        for (int i = 0; i < REP.length; i++) {
            Matcher m = Pattern.compile(REP[i]).matcher(norm);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                //System.out.println(m.group(1));
                int index = m.start();
                char c = norm.charAt(index - 1);
                String rerep = String.format(REREP[i], c + "");
                m.appendReplacement(sb, rerep);
                sb.deleteCharAt(sb.length() - rerep.length());
                //sb.deleteCharAt(index - 1);
            }
            m.appendTail(sb);
            norm = sb.toString();
        }
        for (int i = 0; i < SPECIALREP.length; i++) {
            Matcher m = Pattern.compile(SPECIALREP[i]).matcher(norm);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                //System.out.println(m.group(1));
                m.appendReplacement(sb, SPECIALREREP[i]);
            }
            m.appendTail(sb);
            norm = sb.toString();
        }
        return norm;
    }

    public static String unescapeLatex(String s) {
        for (int i = 0; i < SEQ.length ; i++) {
            String regex = SEQ[i];
            Matcher m = Pattern.compile(regex).matcher(s);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                //System.out.println(m.group(1));
            	//if (m.groupCount() > 0)
            		m.appendReplacement(sb, m.group(1) + REP[i]);
            	//else m.appendReplacement(sb, m.group() + REP[i]);
            }
            m.appendTail(sb);
            s = sb.toString();
        }
        for (int i = 0; i < SPECIALSEQ.length ; i++) {
            String regex = SPECIALSEQ[i];
            Matcher m = Pattern.compile(regex).matcher(s);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                //System.out.println(m.group(1));
            	//if (m.groupCount() > 0)
            		m.appendReplacement(sb, SPECIALREP[i]);
            	//else m.appendReplacement(sb, m.group() + REP[i]);
            }
            m.appendTail(sb);
            s = sb.toString();
        }
        return s;
    }

    public static void main(String[] args) {
        test("Linguagem de Especificação Leve Hoare-Separação para Java");
        test("The Spec\\# Programming System: Challenges and Directions");
    }
    
    public static void test(String s) {
    	String un = unescapeLatex(s);
    	String es = escapeLatex(un);
        String un2 = unescapeLatex(es);
    	System.out.println(un);
    	System.out.println(es);
        System.out.println(un2);
    }
}
