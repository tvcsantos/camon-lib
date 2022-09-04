package pt.unl.fct.di.tsantos.util.download.torrent;

import pt.unl.fct.di.tsantos.util.exceptions.IncorrectSizeException;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.torrentsniffer.torrent.Torrent;
import net.sf.torrentsniffer.torrent.TorrentInfo;
import net.sf.torrentsniffer.torrent.TorrentState;
import pt.unl.fct.di.tsantos.util.download.Downloader;

public class TorrentDownloader implements Downloader {

    private URL url;
    private String fileName;
    private File saveDir;

    private TorrentDownloader(URL url, String fileName, File saveDir) {
        this.url = url;
        this.fileName = fileName;
        this.saveDir = saveDir;
    }

    public List<File> download() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int length = conn.getContentLength();
        InputStream in = conn.getInputStream();
        String s = conn.getHeaderField("Content-Disposition");
        Matcher m1 = Pattern.compile(".*filename=\"(.*)\"").matcher(s);
        if (m1.matches()) {
            String f = m1.group(1);
            f = f != null ? f.trim() : f;
            int index = -1;
            if (f != null && (index = f.lastIndexOf(".torrent")) != -1)
                f = f.substring(0, index);
            if (fileName == null) fileName = f;
        }
        
        // incredible ugly hack to retrieve torrents from isohunt
        if (length == -1) {
            length = 250000;
        }

        BufferedInputStream bis = new BufferedInputStream(in);
        FileOutputStream bos = new FileOutputStream(
                new File(saveDir,fileName + ".torrent"));

        byte[] buff = new byte[length];
        int bytesRead;

        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
        }

        in.close();
        bis.close();
        bos.close();

        return Arrays.asList(new File[]{
            new File(saveDir, fileName + ".torrent")});
    }

    public static List<File> download(URL url, String fileName, File saveDir)
            throws IOException {
        return new TorrentDownloader(url, fileName, saveDir).download();
    }

    public static boolean hasCorrectSize(Torrent torrent, SyndEntry rssItem,
            int minSize, int maxSize) throws IncorrectSizeException {
        int sizeMB = 0;
        sizeMB = getRssDescriptionSize(rssItem);

        // Size not found in description, try the torrent file itself.
        if (sizeMB == 0 || sizeMB == -1) {
            // get torrent info (for size)
            TorrentInfo torrentInfo = torrent.getInfo();
            // first check if size is between min and max
            // convert bytes to MB
            double byteSize = 9.5367431 * Math.pow(10, -7);
            sizeMB = (int) Math.round(torrentInfo.getLength() * byteSize);
        }

        // the size of the file(s) is zero. Not useful and probably not even a
        // torrent
        if (sizeMB == 0 || sizeMB == -1) {
            return false;
        }

        // the size is smaller than the minimum size
        // or larger than the maximum size
        if ((minSize != 0 && minSize >= sizeMB)
                || (maxSize != 0 && maxSize <= sizeMB)) {
            // print error
            if (sizeMB > maxSize) {
                throw new IncorrectSizeException(minSize, maxSize, sizeMB);
            } else if (sizeMB < minSize) {
                throw new IncorrectSizeException(minSize, maxSize, sizeMB);
            }

            return false;
        } else {
            return true;
        }
    }

    public static int getRssDescriptionSize(SyndEntry rssItem) {
        // See if there is a description.
        SyndContent rssContent = rssItem.getDescription();
        if (rssContent == null) {
            // No description available.
            return -1;
        }

        // Retrieve the description.
        String description = rssContent.getValue().toLowerCase();

        Pattern sizePattern = Pattern.compile(
                "(size: )(\\d+(.\\d+)*)(.*)((megabyte|mb)|(gigabyte|gb))");
        Matcher sizeMatcher = sizePattern.matcher(description);

        // Find the file size in the description.
        int fileSize = 0;
        if (sizeMatcher.find()) {
            // Retrieve the file size.
            String fileSizeMatch = sizeMatcher.group(2);

            // If the file size is in GB we've to multiply it by 1024
            // to get it in MB.
            String kindOfBytes = sizeMatcher.group(5);

            // Use this temporary double to store sizes of 1.5gb
            // Cast it to an int before returning the size
            // (after muliplying it by 1024).
            double fileSizeDouble = 0;
            try {
                // Retrieve the number from the string.
                fileSizeDouble = Double.parseDouble(fileSizeMatch);
            } catch (Exception e) {
                // Return if it has failed.
                return -1;
            }

            if (kindOfBytes.contains("gigabyte") || kindOfBytes.equals("gb")) {
                fileSizeDouble *= 1024;
            }

            fileSize = (int) fileSizeDouble;
        } else {
            // Size not found
            return -1;
        }

        return fileSize;
    }

    public static int getSeedersFromTorrent(Torrent torrent) {
        // First try to get the number of the torrent.
        int numberOfSeeders = 0;
        TorrentState torrentState = torrent.getState();
        numberOfSeeders = torrentState.getComplete();
        return numberOfSeeders;
    }
}
