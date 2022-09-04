package pt.unl.fct.di.tsantos.util.app;

import java.awt.SystemTray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedOSException;

public class AppUtils {

    private AppUtils() {
        throw new UnsupportedOperationException();
    }
    
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String USER_OS = System.getProperty("os.name");
    public static final String USER_OS_VERSION =
            System.getProperty("os.version");
    public static final String USER_JAVA_ARCH = System.getProperty("os.arch");
    public static final boolean OS_SUPPORT_TRAY = SystemTray.isSupported();
    public static final String JAVA_VERSION =
            System.getProperty("java.version");

    public static final String FILE_SEPARATOR = java.io.File.separator;
    public static final Runtime RUNTIME = Runtime.getRuntime();
    public static final javax.swing.Icon DEFAULT_HOME_FOLDER_ICON =
            javax.swing.UIManager.getIcon("FileChooser.homeFolderIcon");
    public static final javax.swing.Icon DEFAULT_UP_FOLDER_ICON =
            javax.swing.UIManager.getIcon("FileChooser.upFolderIcon");
    public static final javax.swing.Icon DEFAULT_NEW_FOLDER_ICON =
            javax.swing.UIManager.getIcon("FileChooser.newFolderIcon");
    public static final javax.swing.Icon DEFAULT_LIST_VIEW_ICON =
            javax.swing.UIManager.getIcon("FileChooser.listViewIcon");
    public static final javax.swing.Icon DEFAULT_DETAILS_VIEW_ICON =
            javax.swing.UIManager.getIcon("FileChooser.detailsViewIcon");

    public static javax.swing.filechooser.FileNameExtensionFilter
            getExtensionFilter(String description, String... extensions) {
        return new javax.swing.filechooser.FileNameExtensionFilter(
                description, extensions);
    }

    public static void useWindows7FileChooserIcons(
            javax.swing.Icon homeFolderIcon, javax.swing.Icon upFolderIcon,
            javax.swing.Icon newFolderIcon, javax.swing.Icon listViewIcon,
            javax.swing.Icon detailsViewIcon) {
        javax.swing.UIManager.put("FileChooser.homeFolderIcon", homeFolderIcon);
        javax.swing.UIManager.put("FileChooser.upFolderIcon", upFolderIcon);
        javax.swing.UIManager.put("FileChooser.newFolderIcon", newFolderIcon);
        javax.swing.UIManager.put("FileChooser.listViewIcon", listViewIcon);
        javax.swing.UIManager.put(
                "FileChooser.detailsViewIcon", detailsViewIcon);
        //javax.swing.SwingUtilities.updateComponentTreeUI(fileChooser);
    }

    public static void useDefaultFileChooserIcons() {
        javax.swing.UIManager.put("FileChooser.homeFolderIcon",
                DEFAULT_HOME_FOLDER_ICON);
        javax.swing.UIManager.put("FileChooser.upFolderIcon",
                DEFAULT_UP_FOLDER_ICON);
        javax.swing.UIManager.put("FileChooser.newFolderIcon",
                DEFAULT_NEW_FOLDER_ICON);
        javax.swing.UIManager.put("FileChooser.listViewIcon",
                DEFAULT_LIST_VIEW_ICON);
        javax.swing.UIManager.put("FileChooser.detailsViewIcon",
                DEFAULT_DETAILS_VIEW_ICON);
        //javax.swing.SwingUtilities.updateComponentTreeUI(fileChooser);
    }

    public static java.net.URL getLocation(Class<?> theClass) {
        if (theClass == null) {
            throw new java.lang.NullPointerException();
        }
        java.security.ProtectionDomain pd = theClass.getProtectionDomain();
        if (pd == null) {
            return null;
        }
        java.security.CodeSource cs = pd.getCodeSource();
        if (cs == null) {
            return null;
        }
        return cs.getLocation();
    }
    
    private static final boolean osIsWindows =
            USER_OS.toLowerCase().contains("windows");
    private static final boolean osIsLinux =
            USER_OS.toLowerCase().contains("linux");
    private static final boolean osIsSolaris =
            USER_OS.toLowerCase().contains("solaris");
    private static final boolean osIsMac =
            USER_OS.toLowerCase().contains("mac");
    private static final boolean osIs64bits =
            checkOS64bits();

    private static boolean checkOS64bits() {
        boolean is64 = false;
        if (AppUtils.osIsWindows()) { /** windows **/
            String pp = System.getenv("ProgramFiles(x86)");
            if (pp != null) /** 64 bits **/ is64 = true;
        } else if (AppUtils.osIsLinux() || AppUtils.osIsMac()) {
            try {
                Runtime rt = Runtime.getRuntime();
                Process p = rt.exec(new String[]{"uname", "-m"});
                InputStream inputstream = p.getInputStream();
                InputStreamReader inputstreamreader =
                        new InputStreamReader(inputstream);
                BufferedReader bufferedreader =
                        new BufferedReader(inputstreamreader);
                String line = null;
                while ((line = bufferedreader.readLine()) != null) {
                    if (line.contains("x86_64")) {
                        is64 = true;
                    }
                }
                try {
                    p.waitFor();
                    /*if (p.waitFor() != 0) {
                        System.err.println("exit value = " + p.exitValue());
                    }*/
                } catch (InterruptedException e) {
                    //System.err.println(e);
                }
            } catch (IOException ioe) {
            }
        }
        return is64;
    }


    public static boolean osIsWindows() {
        return osIsWindows;
    }

    public static boolean osIsLinux() {
        return osIsLinux;
    }

    public static boolean osIsSolaris() {
        return osIsSolaris;
    }

    public static boolean osIsMac() {
        return osIsMac;
    }

    public static boolean osIsMacLeopardOrBetter() {
        int minMainVersion = 10;
        int minSubVersion = 5;
        boolean result = false;

        if (osIsMac()) {
            String version = USER_OS_VERSION.toLowerCase();
            StringTokenizer tokenizer = new StringTokenizer(version, ".");
            // check first and second token
            if (tokenizer.countTokens() >= 2) {
                int mainVersion = Integer.parseInt(tokenizer.nextToken());
                int subVersion = Integer.parseInt(tokenizer.nextToken());
                if (mainVersion >= minMainVersion
                        && subVersion >= minSubVersion) {
                    result = true;
                }
            }
        }

        return result;
    }

    public static boolean osIs64bits() {
        return osIs64bits;
    }

    public static String whereIs(String prog) 
            throws IOException, UnsupportedOSException {
        if (!osIsLinux() && !osIsMac())
            throw new UnsupportedOSException();
        //if (AppUtils.osIsWindows()) return null;
        Process proc = null;
        String cmd = osIsLinux() ? "whereis -b" : "whereis";
        proc = RUNTIME.exec(new String[]{"sh", "-c",
                    cmd + " " + prog});
        InputStream inputstream = proc.getInputStream();
        InputStreamReader inputstreamreader =
                new InputStreamReader(inputstream);
        BufferedReader bufferedreader =
                new BufferedReader(inputstreamreader);

        // read the ls output

        String line = null;
        String[] results = null;
        while ((line = bufferedreader.readLine()) != null) {
            line = line.trim();
            results = line.split(" ");
        }

        try {
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
        } catch (InterruptedException e) {
            System.err.println(e);
        }

        if (results == null || results.length <= 1) return null;
        else return results[1].trim();
    }
}
