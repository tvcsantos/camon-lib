package pt.unl.fct.di.tsantos.util.awt;

import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class TrayIconStreamed extends TrayIcon {

    protected PrintStream output = new TrayOutputStream(System.out);
    protected final String caption;

    public TrayIconStreamed(String caption,
            Image image, String tooltip, PopupMenu popup) {
        super(image, tooltip, popup);
        this.caption = caption;
    }

    public TrayIconStreamed(Image image, String tooltip, PopupMenu popup) {
        this(null, image, tooltip, popup);
    }

    public TrayIconStreamed(String caption, Image image, String tooltip) {
        super(image, tooltip);
        this.caption = caption;
    }

    public TrayIconStreamed(Image image, String tooltip) {
        this(null, image, tooltip);
    }

    public TrayIconStreamed(String caption, Image image) {
        super(image);
        this.caption = caption;
    }

    public TrayIconStreamed(Image image) {
        this(null, image);
    }

    class TrayOutputStream extends PrintStream {

        public TrayOutputStream(File file, String csn)
                throws FileNotFoundException, UnsupportedEncodingException {
            super(file, csn);
        }

        public TrayOutputStream(File file)
                throws FileNotFoundException {
            super(file);
        }

        public TrayOutputStream(String fileName, String csn)
                throws FileNotFoundException, UnsupportedEncodingException {
            super(fileName, csn);
        }

        public TrayOutputStream(String fileName)
                throws FileNotFoundException {
            super(fileName);
        }

        public TrayOutputStream(OutputStream out, boolean autoFlush,
                String encoding) throws UnsupportedEncodingException {
            super(out, autoFlush, encoding);
        }

        public TrayOutputStream(OutputStream out, boolean autoFlush) {
            super(out, autoFlush);
        }

        public TrayOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void print(String s) {
            displayMessage(caption, s, MessageType.INFO);
        }

        @Override
        public void println(String s) {
            print(s);
        }

        @Override
        public void print(boolean b) {
            print(b + "");
        }

        @Override
        public void print(char c) {
            print(c + "");
        }

        @Override
        public void print(int i) {
            print(i + "");
        }

        @Override
        public void print(long l) {
            print(l + "");
        }

        @Override
        public void print(float f) {
            print(f + "");
        }

        @Override
        public void print(double d) {
            print(d + "");
        }

        @Override
        public void print(Object obj) {
            print(obj.toString() + "");
        }

        @Override
        public void println() {
            //super.println();
        }

        @Override
        public void println(boolean x) {
            print(x);
        }

        @Override
        public void println(char x) {
            print(x);
        }

        @Override
        public void println(int x) {
            print(x);
        }

        @Override
        public void println(long x) {
            print(x);
        }

        @Override
        public void println(float x) {
            print(x);
        }

        @Override
        public void println(double x) {
            print(x);
        }

        @Override
        public void println(Object x) {
            print(x);
        }

        
    }

    public PrintStream getOutputPrintStream() {
        return output;
    }
    
}
