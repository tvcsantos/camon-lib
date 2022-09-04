package pt.unl.fct.di.tsantos.util.uTorrent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.unl.fct.di.tsantos.util.FileUtils;
import pt.unl.fct.di.tsantos.util.io.StringOutputStream;

public class uTorrent {

    protected String host;
    protected int port;
    protected HttpClient client;
    protected String token;

    public uTorrent(String host, int port) {
        this.host = host;
        this.port = port;
        this.token = null;
        this.client = new HttpClient();
    }

    public synchronized void login(String username, String password)
            throws HttpException, IOException {
        if (isLoggedIn()) logout();
        Credentials cred = new UsernamePasswordCredentials(username, password);
        client.getState().setCredentials(AuthScope.ANY, cred);
        GetMethod method = new GetMethod(
                "http://" + host + ":" + port + "/gui/token.html");
        int executeMethod = client.executeMethod(method);
        if (executeMethod != HttpStatus.SC_OK)
            throw new HttpException(method.getStatusLine().toString());
        InputStream is = method.getResponseBodyAsStream();
        Source source = new Source(is);
        source.setLogger(null);
        Element e = source.getFirstElement("id", "token", false);
        token = e.getContent().toString();
        is.close();
    }

    public boolean isLoggedIn() {
        return token != null;
    }

    public synchronized void logout() {
        client = new HttpClient();
        token = null;
    }

    public Collection<String> listDownloading()
            throws IOException, JSONException {
        GetMethod method = new GetMethod("http://" + host + ":" + port +
                "/gui/?list=1&token=" + token);
        int executeMethod = client.executeMethod(method);
        InputStream ins = method.getResponseBodyAsStream();
        StringOutputStream os = new StringOutputStream();
        FileUtils.copy(ins, os);
        String response = os.getString();
        ins.close();
        os.close();
        JSONObject jsonObj = new JSONObject(response);
        JSONArray jsonArr = jsonObj.getJSONArray("torrents");
        int len = jsonArr.length();
        List<String> result = new LinkedList<String>();
        for (int i = 0; i < len; i++) {
            JSONArray inner = jsonArr.getJSONArray(i);
            int percent = inner.getInt(4);
            if (percent == 1000) continue;
            String name = inner.getString(2);
            result.add(name);
        }
        return result;
    }

    public Collection<String> listDownloadingFiles()
            throws IOException, JSONException {
        GetMethod method = new GetMethod("http://" + host + ":" + port +
                "/gui/?token=" + token + "&list=1");
        int executeMethod = client.executeMethod(method);
        InputStream ins = method.getResponseBodyAsStream();
        StringOutputStream os = new StringOutputStream();
        FileUtils.copy(ins, os);
        String response = os.getString();
        ins.close();
        os.close();
        JSONObject jsonObj = new JSONObject(response);
        JSONArray jsonArr = jsonObj.getJSONArray("torrents");
        int len = jsonArr.length();
        List<String> hashList = new LinkedList<String>();
        for (int i = 0; i < len; i++) {
            JSONArray inner = jsonArr.getJSONArray(i);
            int percent = inner.getInt(4);
            if (percent == 1000) continue;
            String hash = inner.getString(0);
            hashList.add(hash);
        }

        List<String> result = new LinkedList<String>();
        for (String hash : hashList) {
            method = new GetMethod("http://" + host + ":" + port
                    + "/gui/?token=" + token + "&action=getfiles&hash=" + hash);
            executeMethod = client.executeMethod(method);
            ins = method.getResponseBodyAsStream();
            os = new StringOutputStream();
            FileUtils.copy(ins, os);
            response = os.getString();
            jsonObj = new JSONObject(response);
            jsonArr = jsonObj.getJSONArray("files");
            JSONArray inner = jsonArr.getJSONArray(1);
            len = inner.length();
            for (int i = 0; i < len; i++) {
                JSONArray inner2 = inner.getJSONArray(i);
                String fileName = inner2.getString(0);
                long size = inner2.getLong(1);
                long down = inner2.getLong(2);
                if (down >= size) continue;
                result.add(fileName);
            }
        }
        
        return result;
    }
}
