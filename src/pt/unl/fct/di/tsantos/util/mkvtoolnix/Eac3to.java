/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.mkvtoolnix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import pt.unl.fct.di.tsantos.util.io.AbstractStreamGobbler;
import pt.unl.fct.di.tsantos.util.ProgressListener;
import pt.unl.fct.di.tsantos.util.io.StreamGobbler;
import pt.unl.fct.di.tsantos.util.app.AppUtils;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedOSException;

/**
 *
 * @author tvcsantos
 */
public class Eac3to {
    private static Eac3to instance;

    private File path;

    private Eac3to() throws FileNotFoundException, UnsupportedOSException {
        if (path == null) {
            /** location overrided by some application **/
            String s = System.getProperty("eac3to.path");
            if (s == null) {
                /** location not overrided try system location **/
                if (AppUtils.osIsWindows()) { /** windows **/
                    String pp = System.getenv("ProgramFiles(x86)");
                    if (pp != null) /** 64 bits **/ {
                        s = System.getenv("ProgramW6432") +
                                AppUtils.FILE_SEPARATOR + "eac3to";
                        /** check 64bit dir **/
                        if (!new File(s).exists()) s = null;
                        if (s == null) /** if not found at 64bit dir try 32bit **/
                            s = pp + AppUtils.FILE_SEPARATOR + "eac3to";
                    } else { /** must be 32bits os **/
                        s = System.getProperty("ProgramFiles") +
                                AppUtils.FILE_SEPARATOR + "eac3to";
                    }
                } else throw new UnsupportedOSException();
            }

            if (s != null) {
                path = new File(s);
                if (!path.exists() || !path.isDirectory()) {
                    path = null;
                    throw new FileNotFoundException();
                }
            } else throw new FileNotFoundException();
        }
    }

    public static Eac3to getInstance()
            throws FileNotFoundException, UnsupportedOSException {
        if (instance == null) {
            instance = new Eac3to();
        }
        return instance;
    }

    public File getPath() {
        return path;
    }

    public String[] exec(String[] args)
            throws IOException, InterruptedException {
        return exec(args, null);
    }

    public String[] exec(String[] args, ProgressListener listener)
            throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String[] rargs = new String[args.length + 1];
        rargs[0] = path.getAbsolutePath().toString();
        System.arraycopy(args, 0, rargs, 1, args.length);
        Process p = rt.exec(rargs);

        String output = null;
        String error = null;

        final StreamGobbler errorGobbler =
                new StreamGobbler(p.getErrorStream(), "ERROR");

        final AbstractStreamGobbler outputGobbler =
                new AbstractStreamGobbler(p.getInputStream(), "OUTPUT",
                listener) {

                    @Override
                    public void parseLine(String line) {
                        if (listener == null) {
                            return;
                        }
                        if (line == null) {
                            return;
                        }
                        line = line.trim();
                        if (line.length() <= 0) {
                            return;
                        }
                        if (line.contains("Progress")) {
                            listener.reportProgress(Double.valueOf(
                                    line.replace("Progress", "").replace(":", "").
                                    replace("%", "").trim()).intValue());
                            //listener.reportCurrentTask(getOutput());
                        }
                    }
                };

        // kick them off
        errorGobbler.start();
        outputGobbler.start();

        // any error???
        final int exitVal = p.waitFor();
        System.out.println("ExitValue: " + exitVal);

        output = outputGobbler.getOutput();
        error = errorGobbler.getOutput();

        return new String[]{exitVal + "", output, error};
    }

    public static void main(String[] args) 
            throws FileNotFoundException, UnsupportedOSException {
        Eac3to eac3to = Eac3to.getInstance();
    }
}
