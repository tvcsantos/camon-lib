/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import pt.unl.fct.di.tsantos.util.exceptions.NotExecutableException;
import pt.unl.fct.di.tsantos.util.swing.EventProducerUtilities;
import pt.unl.fct.di.tsantos.util.swing.ProcessEvent;
import pt.unl.fct.di.tsantos.util.swing.ProcessListener;

/**
 *
 * @author tvcsantos
 */
public class ExecutableFile extends File {
    protected static final ShutdownHookProcessDestroyer destroyer;
    protected static final Executor executor;

    static {
        destroyer = new ShutdownHookProcessDestroyer();
        executor = new DefaultExecutor();
        executor.setProcessDestroyer(destroyer);
    }
    
    protected List<ProcessListener> listeners =
            new LinkedList<ProcessListener>();

    private void checkExecutable() throws NotExecutableException {
        try {
            Executor dummy = new DefaultExecutor();
            dummy.setStreamHandler(
                    new PumpStreamHandler(new StringOutputStream()));
            dummy.execute(new CommandLine(this));
            //Runtime.getRuntime().exec(getAbsolutePath());
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

    private void notifyListenersProcessStart(ProcessEvent evt) {
        EventProducerUtilities.notifyListeners(
                listeners, "processStart", evt);
    }

    private void notifyListenersProcessFinish(ProcessEvent evt) {
        EventProducerUtilities.notifyListeners(
                listeners, "processFinish", evt);
    }

    private void notifyListenersProcessInterrupt(ProcessEvent evt) {
        EventProducerUtilities.notifyListeners(
                listeners, "processInterrupt", evt);
    }

    private void notifyListenersProcessUpdate(ProcessEvent evt) {
        EventProducerUtilities.notifyListeners(
                listeners, "processUpdate", evt);
    }

    public ExecutionResult execute(String[] args)
            throws ExecuteException, IOException {
        try {
            CommandLine cmdLine = new CommandLine(this);
            cmdLine.addArguments(args);
            MemLogOutputStream outsos = new MemLogOutputStream() {
                @Override
                protected void processLine(String line) {
                    notifyListenersProcessUpdate(
                            new ProcessEvent(ExecutableFile.this, line));
                }
            };
            MemLogOutputStream errsos = new MemLogOutputStream() {
                @Override
                protected void processLine(String line) {
                    notifyListenersProcessUpdate(
                            new ProcessEvent(ExecutableFile.this, line));
                }
            };
            executor.setStreamHandler(new PumpStreamHandler(outsos, errsos));
            notifyListenersProcessStart(new ProcessEvent(this,
                    "Executing " + this.getName() +
                    " with the following command line: " + cmdLine));
            int execute = executor.execute(cmdLine);
            notifyListenersProcessFinish(new ProcessEvent(this,
                    "Execution of " + cmdLine + " ended"));
            return new ExecutionResult(execute, outsos.getString(),
                    errsos.getString());
        } catch(ExecuteException ex) {
            notifyListenersProcessInterrupt(new ProcessEvent(this, ex));
            throw ex;
        } catch(IOException ex) {
            notifyListenersProcessInterrupt(new ProcessEvent(this, ex));
            throw ex;
        }
    }

    public void addProcessListener(ProcessListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ProcessListener listener) {
        listeners.remove(listener);
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
