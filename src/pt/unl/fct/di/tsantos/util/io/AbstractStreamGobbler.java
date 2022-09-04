/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.unl.fct.di.tsantos.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import pt.unl.fct.di.tsantos.util.ProgressListener;

/**
 * Asynchronously read the output of a given input stream. <br />
 * Any exception during execution of the command in managed in this thread.
 * @author <a href="http://stackoverflow.com/users/6309/vonc">VonC</a>
 */
public abstract class AbstractStreamGobbler extends Thread {

    private InputStream is;
    protected String type;
    private StringBuffer output = new StringBuffer();
    protected ProgressListener listener;

    public AbstractStreamGobbler(final InputStream anIs, final String aType,
            ProgressListener listener) {
        this.is = anIs;
        this.type = aType;
        this.listener = listener;
    }

    public AbstractStreamGobbler(final InputStream anIs, final String aType) {
        this(anIs, aType, null);
    }

    public abstract void parseLine(String line);

    /**
     * Asynchronous read of the input stream. <br />
     * Will report output as its its displayed.
     * @see java.lang.Thread#run()
     */
    @Override
    public final void run() {
        try {
            final InputStreamReader isr = new InputStreamReader(this.is);
            final BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                //System.out.println(this.type + ">" + line);
                parseLine(line);
                this.output.append(line).append(
                        System.getProperty("line.separator"));
            }
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Get output filled asynchronously. <br />
     * Should be called after execution
     * @return final output
     */
    public final String getOutput() {
        return this.output.toString();
    }
}
