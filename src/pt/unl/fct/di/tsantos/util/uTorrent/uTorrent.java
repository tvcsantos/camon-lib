package pt.unl.fct.di.tsantos.util.uTorrent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class uTorrent {

    protected String host;
    protected int port;
    protected DefaultHttpClient client;
    protected String token;

    public uTorrent(String host, int port) {
        this.host = host;
        this.port = port;
        this.token = null;
        this.client = new DefaultHttpClient();
    }

    public synchronized void login(String username, String password)
            throws HttpException, IOException {
        if (isLoggedIn()) logout();
        Credentials cred = new UsernamePasswordCredentials(username, password);
        client.getCredentialsProvider().setCredentials(AuthScope.ANY, cred);
        //client.getState().setCredentials(AuthScope.ANY, cred);
        HttpGet method = new HttpGet(
                "http://" + host + ":" + port + "/gui/token.html");
        HttpResponse response = client.execute(method);
        HttpEntity entity = response.getEntity();
        /*int executeMethod = client.executeMethod(method);
        if (executeMethod != HttpStatus.SC_OK)
            throw new HttpException(method.getStatusLine().toString());*/
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK)
            throw new HttpException(response.getStatusLine().toString());
        InputStream is = entity.getContent();//.getResponseBodyAsStream();
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
        client = new DefaultHttpClient();
        token = null;
    }

    public Collection<String> listDownloading()
            throws IOException, JSONException {
        HttpGet method = new HttpGet("http://" + host + ":" + port +
                "/gui/?list=1&token=" + token);
        HttpResponse response = client.execute(method);
        HttpEntity entity = response.getEntity();
        String answer = EntityUtils.toString(entity);
        /*InputStream ins = method.getResponseBodyAsStream();
        StringOutputStream os = new StringOutputStream();
        FileUtils.copy(ins, os);
        String answer = os.getString();
        ins.close();
        os.close();*/
        JSONObject jsonObj = new JSONObject(answer);
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
        HttpGet method = new HttpGet("http://" + host + ":" + port +
                "/gui/?token=" + token + "&list=1");
        HttpResponse response = client.execute(method);
        HttpEntity entity = response.getEntity();
        String answer = EntityUtils.toString(entity);
        method.abort();
        /*int executeMethod = client.executeMethod(method);
        InputStream ins = method.getResponseBodyAsStream();
        StringOutputStream os = new StringOutputStream();
        FileUtils.copy(ins, os);
        String answer = os.getString();
        ins.close();
        os.close();*/
        JSONObject jsonObj = new JSONObject(answer);
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
            method = new HttpGet("http://" + host + ":" + port
                    + "/gui/?token=" + token + "&action=getfiles&hash=" + hash);
            /*executeMethod = client.executeMethod(method);
            ins = method.getResponseBodyAsStream();
            os = new StringOutputStream();
            FileUtils.copy(ins, os);
            answer = os.getString();*/
            response = client.execute(method);
            entity = response.getEntity();
            answer = EntityUtils.toString(entity);
            method.abort();
            jsonObj = new JSONObject(answer);
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
