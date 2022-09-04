package pt.unl.fct.di.tsantos.util.download.subtitile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.codec.binary.Base64;
import pt.unl.fct.di.tsantos.util.string.StringUtils;

public abstract class AbstractSubtitleDescriptor
        implements SubtitleDescriptor {

    public final void saveData(File file) throws IOException {
        byte[] bytes = Base64.decodeBase64(getData());
        saveDataBytes(file, bytes);
    }

    protected void saveDataBytes(File file, byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        int b = -1;
        FileOutputStream fos = new FileOutputStream(file);
        while ((b = bis.read()) != -1) {
            fos.write(b);
        }
        fos.flush();
        fos.close();
        bis.close();
    }

    public static String getSubtitleFileContent(SubtitleDescriptor sd)
            throws IOException {
        String data = sd.getData();
        if (data == null) return null;
        byte[] bytes = Base64.decodeBase64(data);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        return StringUtils.getString(bis);
    }

}
