package pt.unl.fct.di.tsantos.util.mediainfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import pt.unl.fct.di.tsantos.util.app.AppUtils;
import pt.unl.fct.di.tsantos.util.Properties;

/**
 *
 * @author tvcsantos
 */
public class MediaInfo {
    private MediaInfo() { }

    public static Properties load(File progDir, File file) throws
            FileNotFoundException, IOException {
        if (progDir == null)
            throw new NullPointerException("progDir must be non null");
        if (file == null)
            throw new NullPointerException("file must be non null");
        if (!progDir.exists())
            throw new FileNotFoundException("progDir doesn't exist");
        if (!progDir.isDirectory())
            throw new IllegalArgumentException("progDir must be a directory");
        if (!file.exists())
            throw new FileNotFoundException("file doesn't exist");
        if (!file.isFile())
            throw new IllegalArgumentException("file must be a file");

        boolean unix = !AppUtils.USER_OS.toLowerCase().contains("windows");

        if (!unix) {
            File mediaInfo =
                    new File((progDir.getAbsolutePath() + AppUtils.FILE_SEPARATOR
                + "MediaInfo.exe").replace("%20", " "));
            if (!mediaInfo.exists())
                throw new NullPointerException(
                        "MediaInfo.exe not found at " + progDir);
        }
        
        String path = (progDir.getAbsolutePath() + AppUtils.FILE_SEPARATOR
                + "MediaInfo.exe").replace("%20", " ");
        
        if (unix) path = "mediainfo";
        
        Process proc = AppUtils.RUNTIME.exec(
                new String[]{path, file.toString()});
        InputStream inputstream = proc.getInputStream();
        InputStreamReader inputstreamreader = new InputStreamReader(
                inputstream);
        BufferedReader bufferedreader = new BufferedReader(
                inputstreamreader);

        // read the ls output
        Properties root = new Properties(file.toString(), null);
        Properties curr = root;

        String line;
        while ((line = bufferedreader.readLine()) != null) {
            line = line.trim();
            if (line.length() <= 0) continue;
            int index = line.indexOf(":");
            boolean isParent = index == -1;
            if (isParent) {
                int index2 = line.indexOf("#");
                if (index2 >= 0)
                    line = line.substring(0, index2).trim();
                Properties node = new Properties(line, null);
                root.add(node);
                curr = node;
            } else {
                String left = line.substring(0, index).trim();
                String right = line.substring(index + 1).trim();
                int index2 = right.indexOf(" / ");
                if (index2 >= 0) {
                    StringTokenizer st = new StringTokenizer(right,"/");
                    Properties p = new Properties(left, null);
                    curr.add(p);
                    curr = p;
                    while(st.hasMoreTokens()) {
                        String token = st.nextToken().trim();
                        String[] arr = token.split("=");
                        if (arr.length < 2) continue;
                        Properties node = new Properties(arr[0],arr[1]);
                        curr.add(node);
                    }
                    curr = curr.getParent();
                } else {
                    Properties node = new Properties(left, right);
                    curr.add(node);
                }
            }
        }
        return root;
    }
}
