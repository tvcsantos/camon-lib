/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import pt.unl.fct.di.tsantos.util.ProgressListener;
import pt.unl.fct.di.tsantos.util.exceptions.NotExecutableException;

/**
 *
 * @author tvcsantos
 */
public class ExecutableFile extends File {

    private void checkExecutable() throws NotExecutableException {
        try {
            Runtime.getRuntime().exec(getAbsolutePath());
        } catch (IOException ex) {
            String msg = ex.getMessage();
            NotExecutableException nex = 
                    new NotExecutableException("This file can't be executed");
            nex.initCause(ex);
            //nex.initCause(ex.getCause());
            if (msg == null) throw nex;
            else if (msg.toLowerCase().contains("cannot run program"))
                throw nex;
        }
    }

    public ExecutableFile(URI uri) throws NotExecutableException {
        super(uri);
        checkExecutable();
    }

    public ExecutableFile(File parent, String child) 
            throws NotExecutableException {
        super(parent, child);
        checkExecutable();
    }

    public ExecutableFile(String parent, String child) 
            throws NotExecutableException {
        super(parent, child);
        checkExecutable();
    }

    public ExecutableFile(String pathname)
            throws NotExecutableException {
        super(pathname);
        checkExecutable();
    }

    public ExecutionResult execute(String[] args, 
            final ProgressListener listener)
            throws InterruptedException, IOException {
        Runtime rt = Runtime.getRuntime();
        String[] rargs = new String[args.length + 1];
        rargs[0] = getAbsolutePath().toString();
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

        return new ExecutionResult(exitVal, output, error);
    }

    public ExecutionResult execute(String[] args) 
            throws InterruptedException, IOException {
        return execute(args, null);
    }

    public class ExecutionResult {
        protected int exitCode;
        protected String output;
        protected String error;

        public ExecutionResult(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }
    }
}
