package pt.unl.fct.di.tsantos.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Properties {
    protected Properties parent;
    protected String key;
    protected String value;
    protected Map<String, List<Properties>> contents;

    public Properties(String key, String value) {
        if (key == null)
            throw new NullPointerException("key must be non null");
        this.parent = null;
        this.key = key;
        this.value = value;
        this.contents = new HashMap<String, List<Properties>>();
    }

    public Properties(String key, String value, Properties parent) {
        this(key, value);
        this.parent = parent;
    }

    public void add(Properties n) {
        n.setParent(this);
        List<Properties> list = contents.get(n.key);
        if (list == null) list = new LinkedList<Properties>();
        list.add(n);
        contents.put(n.key, list);
    }

    public List<Properties> get(String key) {
        return contents.get(key);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setParent(Properties parent) {
        this.parent = parent;
    }

    public Properties getParent() {
        return parent;
    }

    public String toFormatedString() {
        return toFormatedString(0);
    }

    private String toFormatedString(int level) {
        String res = "";
        for (int i = 0 ; i < level ; i++) res += "  ";
        String result = res + key + ": " + value ;
        if (contents.isEmpty()) return result;
        for (List<Properties> list : contents.values()) {
            for (Properties n : list) {
                result += "\n" + n.toFormatedString(level + 1);
            }
        }
        return result;
    }
    
    public void storeToXML(OutputStream os, String comment, String encoding) 
            throws IOException {
        if (os == null) throw new NullPointerException();
        save(os, comment, encoding);
    }

    public void storeToXML(OutputStream os, String comment)
            throws IOException {
        if (os == null) throw new NullPointerException();
        save(os, comment, "UTF-8");
    }

    private void save(OutputStream os, String comment,
                     String encoding)
        throws IOException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            assert(false);
        }

        Document doc = db.newDocument();
        Element properties =  (Element)
            doc.appendChild(doc.createElement("properties"));

        if (comment != null) {
            Element comments = (Element)properties.appendChild(
                doc.createElement("comment"));
            comments.appendChild(doc.createTextNode(comment));
        }

        processProps(doc, properties, this);
        emitDocument(doc, os, encoding);
    }

    private void processProps(Document doc, Element properties, Properties p) {
        Element entry = (Element)properties.appendChild(
            doc.createElement("property"));
        entry.setAttribute("key", p.key);
        if (p.value != null) entry.setAttribute("value", p.value);
        Collection keys = p.contents.values();
        Iterator i = keys.iterator();
        while(i.hasNext()) {
            List list = (List)i.next();
            Iterator it = list.iterator();
            while(it.hasNext()) {
                Properties prop = (Properties)it.next();
                processProps(doc, entry, prop);
            }
        }
    }

    private void emitDocument(Document doc, OutputStream os, String encoding)
        throws IOException
    {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = null;
        try {
            t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "properties.dtd");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.METHOD, "xml");
            t.setOutputProperty(OutputKeys.ENCODING, encoding);
        } catch (TransformerConfigurationException tce) {
            assert(false);
        }
        DOMSource doms = new DOMSource(doc);
        StreamResult sr = new StreamResult(os);
        try {
            t.transform(doms, sr);
        } catch (TransformerException te) {
            IOException ioe = new IOException();
            ioe.initCause(te);
            throw ioe;
        }
    }

}
