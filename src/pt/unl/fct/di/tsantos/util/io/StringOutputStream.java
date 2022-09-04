package pt.unl.fct.di.tsantos.util.io;

import java.io.IOException;
import java.io.OutputStream;

public class StringOutputStream extends OutputStream {

    protected StringBuilder mBuf = new StringBuilder();

    @Override
    public void write(int b) throws IOException {
        mBuf.append((char) b);
    }

    public String getString() {
        return mBuf.toString();
    }
}
