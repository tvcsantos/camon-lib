package pt.unl.fct.di.tsantos.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * This class provides utilities for obtaining the MD5 checksum
 * from files converting byte[] to hexadecimal string representation.
 *
 * @author Tiago Santos
 *
 * @since JDK1.6
 * @version 1.0
 */
public class MD5Checksum {

    /** Can't instantiate this class */
    private MD5Checksum() {
    }

    /**
     * Creates an MD5 checksum from a stream
     * @param is the input stream
     * @return the byte[] of the checksum
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.IOException
     */
    public static byte[] createChecksum(InputStream is)
            throws NoSuchAlgorithmException, IOException {

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = is.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        is.close();
        return complete.digest();
    }

    /**
     * Creates an MD5 checksum of a file
     * @param file the file
     * @return the byte[] of the checksum
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.IOException
     */
    public static byte[] createChecksum(File file)
            throws NoSuchAlgorithmException, IOException {
        InputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return complete.digest();
    }

    /**
     * Creates an MD5 checksum of a file specified in argument
     * and returns its hexadecimal string representation
     * @param file the file
     * @return the hexadecimal string representing the MD5 checksum of the file
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.IOException
     */
    public static String getMD5Checksum(File file)
            throws NoSuchAlgorithmException, IOException {
        return getHexString(createChecksum(file));
    }

    /**
     * Creates an MD5 checksum from a stream specified in argument
     * and returns its hexadecimal string representation
     * @param is the input stream
     * @return the hexadecimal string representing the MD5 checksum of the file
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.IOException
     */
    public static String getMD5Checksum(InputStream is)
            throws NoSuchAlgorithmException, IOException {
        return getHexString(createChecksum(is));
    }
    
    /**
     * The hexadecimal byte char table
     */
    static final byte[] HEX_CHAR_TABLE = {
        (byte) '0', (byte) '1', (byte) '2', (byte) '3',
        (byte) '4', (byte) '5', (byte) '6', (byte) '7',
        (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
        (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
    };

    /**
     * Returns the hexadecimal string of the specified byte[]
     * @param raw the byte[] to convert to hexadecimal string
     * @return the hexadecimal string of the specified byte[]
     */
    public static String getHexString(byte[] raw) {
        byte[] hex = new byte[2 * raw.length];
        int index = 0;
        for (byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        return new String(hex).toUpperCase();
    }

    /**
     * Returns a random hexadecimal string with the length
     * specified in argument
     * @param characters the length of the random hexadecimal string
     * @return a random hexadecimal string
     */
    public static String randomHexString(int characters) {
        String r = "";
        Random rand = new Random();
        while (r.length() < characters) {
            r += HEX_CHAR_TABLE[rand.nextInt(HEX_CHAR_TABLE.length)];
        }
        return r;
    }

    public static boolean containsCheckSum(File directory, String checkSum) {
        return getFileWithCheckSum(directory, checkSum) != null;
    }

    public static File getFileWithCheckSum(File directory, String checkSum) {
        try {
            if (!directory.exists()) return null;
            File[] files = directory.listFiles();
            if (files == null) return null;
            for (File file : files) {
                if (file == null || file.isDirectory())
                    continue;
                String fileHash = getMD5Checksum(file);
                if (checkSum.equalsIgnoreCase(fileHash)) {
                    return file;
                }
            }
        } catch(Exception e) {}
        return null;
    }
    
}
