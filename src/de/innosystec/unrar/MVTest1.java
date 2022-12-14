package de.innosystec.unrar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

public class MVTest1 {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String filename = "D:\\Vuze Downloads\\Subtitles\\pb\\"
                + "Subsfreakab58ff0878221a703f06d2b5f053c992DIR.rar";
        File f = new File(filename);
        Archive a = null;
        try {
            a = new Archive(f);
        } catch (RarException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (a != null) {
            //a.getMainHeader().print();
            FileHeader fh = a.nextFileHeader();
            while (fh != null) {
                try {
                    File out = new File("D:\\Vuze Downloads\\Subtitles\\pb\\" +
                            fh.getFileNameString().trim());
                    if (!fh.isDirectory()) {
                        System.out.println(out.getParentFile().mkdirs());
                        System.out.println(out.getAbsolutePath());
                        FileOutputStream os = new FileOutputStream(out);
                        a.extractFile(fh, os);
                        os.close();
                    }
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (RarException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                fh = a.nextFileHeader();
            }
        }
    }
}
