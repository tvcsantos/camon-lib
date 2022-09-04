/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.unl.fct.di.tsantos.util.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author tvcsantos
 */
public abstract class MemLogOutputStream extends OutputStream {
    
    protected StringBuilder mBuf = new StringBuilder();
    protected StringBuilder fullBuf = new StringBuilder();

    protected boolean skip = false;

    public MemLogOutputStream() {
        super();
    }

    @Override
    public void write(int b) throws IOException {
        char c = (char) b;
        if ((c == '\n') || (c == '\r')) {
            if (!skip) {
                processBuffer();
            }
        } else {
            mBuf.append(c);
        }
        skip = (c == '\r');
        fullBuf.append(c);
    }

    public String getString() {
        return fullBuf.toString();
    }

    protected void processBuffer() {
        processLine(mBuf.toString());
        mBuf.setLength(0);
    }

    protected abstract void processLine(final String line);
}
