/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.mkvtoolnix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import pt.unl.fct.di.tsantos.util.app.AppUtils;
import pt.unl.fct.di.tsantos.util.collection.ArraysExtended;
import pt.unl.fct.di.tsantos.util.exceptions.NotExecutableException;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedOSException;
import pt.unl.fct.di.tsantos.util.io.ExecutableFile;

/**
 *
 * @author tvcsantos
 */
public class ProgramDirectory {
    protected String dirName;
    protected String propPathName;
    protected File path;
    protected String[] programs;
    
    public ProgramDirectory(String dirName) {
        this(dirName, dirName.replaceAll(" ", "")
                .toLowerCase().concat(".path"), null);
    }

    public ProgramDirectory(String dirName, String propPathName) {
        this(dirName, propPathName, null);
    }

    public ProgramDirectory(String dirName, String propPathName,
            String[] programs) {
        this.dirName = dirName;
        this.propPathName = propPathName;
        this.programs = programs;
    }

    public ProgramDirectory(String dirName, String[] programs) {
        this(dirName, dirName.replaceAll(" ", "")
                .toLowerCase().concat(".path"), programs);
    }
   
    public File getDirectory()
            throws FileNotFoundException, UnsupportedOSException {
        if (path == null) {
            /** location overrided by some application **/
            String s = System.getProperty(propPathName);
            if (s == null) {
                /** location not overrided try system location **/
                if (AppUtils.osIsWindows()) s = getWindows();
                else if (AppUtils.osIsLinux()) s = getLinux();
                else if (AppUtils.osIsMac()) s = getMac();
                else s = getOtherOS();
            }

            if (s != null) {
                path = new File(s);
                if (!path.exists() || !path.isDirectory()) {
                    path = null;
                    throw new FileNotFoundException();
                }
            } else throw new FileNotFoundException();
        }
        return path;
    }

    protected String getWindows() {
        String s = null;
        String pp = System.getenv("ProgramFiles(x86)");
        if (pp != null) /** 64 bits **/
        {
            s = System.getenv("ProgramW6432")
                    + AppUtils.FILE_SEPARATOR + dirName;
            /** check 64bit dir **/
            if (!new File(s).exists()) {
                s = null;
            }
            if (s == null) /** if not found at 64bit dir try 32bit **/
            {
                s = pp + AppUtils.FILE_SEPARATOR + dirName;
            }
        } else {
            /** must be 32bits os **/
            s = System.getProperty("ProgramFiles")
                    + AppUtils.FILE_SEPARATOR + dirName;
        }
        return s;
    }

    protected String getLinux() throws UnsupportedOSException {
        if (programs == null || programs.length <= 0)
            throw new UnsupportedOSException();
        String s = null;
        try {
            String where = AppUtils.whereIs(programs[0]);
            if (where != null) {
                File f = new File(where);
                if (f.exists()) {
                    s = f.getParentFile().getAbsolutePath();
                }
            }
        } catch (IOException ex) {
        }
        return s;
    }

    protected String getMac() throws UnsupportedOSException {
        return getLinux();
    }

    protected String getOtherOS() throws UnsupportedOSException {
        throw new UnsupportedOSException();
    }
    
    public ExecutableFile getProgram(String name)
        throws NotExecutableException, FileNotFoundException,
            UnsupportedOSException {
        if (!ArraysExtended.contains(programs, name))
            throw new FileNotFoundException(name + 
                    " not found in this program directory");
        File dir = getDirectory();
        ExecutableFile res = null;
        if (AppUtils.osIsWindows())
            res = new ExecutableFile(dir, name + ".exe");
        else if (AppUtils.osIsLinux() || AppUtils.osIsMac())
            res = new ExecutableFile(dir, name);
        else throw new UnsupportedOSException();
        if (res == null || !res.exists() || !res.isFile())
            throw new FileNotFoundException();
        return res;
    }
}
