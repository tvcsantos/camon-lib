package pt.unl.fct.di.tsantos.util.swing;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;

/**
 * This class extends JTextArea providing the ability
 * to write in a text area as it was an {@link java.io.OutputStream}.<br>
 * Code copied from
 * <a href="http://www.javafaq.nu/java-example-code-1044.html">javafaq</a>.
 * 
 * <dl><dt><b>Changes:</b></dt>
 * <dt><b>version 1.0 to version 1.2</b></dt>
 * <dd>Override the {@link JTextArea#append(java.lang.String)} method
 * adding auto-scroll</dd>
 * </dl>
 *
 * @see JTextArea
 * @see OutputStream
 *
 * @since JDK1.6
 * @version 1.2
 *
 */
public class JStreamedTextArea extends JTextArea {

    private static final long serialVersionUID = -5832933217572253869L;
    /** The output stream of this text area */
    private OutputStream theOutput = new TextAreaOutputStream();
    boolean autoScroll = true;

    /**
     * Constructs a new JStreamedTextArea
     */
    public JStreamedTextArea() {
        this("", 0, 0);
    }

    /**
     * Constructs a new JStreamedTextArea with the specified text
     * @param text the text of the text area
     */
    public JStreamedTextArea(String text) {
        this(text, 0, 0);
    }

    /**
     * Constructs a new JStreamedTextArea with the specified number
     * of rows and columns.
     * @param rows the number of rows >= 0
     * @param columns the number of columns >= 0
     */
    public JStreamedTextArea(int rows, int columns) {
        this("", rows, columns);
    }

    /**
     * Constructs a new JStreamedTextArea with the specified text and number
     * of rows and columns.
     * @param text the text to be displayed, or null
     * @param rows the number of rows >= 0
     * @param columns the number of columns >= 0
     */
    public JStreamedTextArea(String text, int rows, int columns) {
        super(text, rows, columns);
        setEditable(false);
    }

    /**
     * Returns the output stream of the text area
     * @return the output stream of the text area
     */
    public OutputStream getOutputStream() {
        return theOutput;
    }

    @Override
    public void append(String str) {
        super.append(str);
        setCaretPosition(getDocument().getLength());
    }

    /**
     * Inner class to encapsulate the action of writing
     * in the text area via the output stream
     */
    private class TextAreaOutputStream extends OutputStream {

        /** If the stream is closed or not */
        private boolean closed = false;

        public void write(int b) throws IOException {
            checkOpen();
            // recall that the int should really just be a byte
            b &= 0x000000FF;
            // must convert byte to a char in order to append it
            char c = (char) b;
            append(String.valueOf(c));
        }

        /**
         * Checks if the stream is opened
         * @throws java.io.IOException if the stream is closed
         */
        private void checkOpen() throws IOException {
            if (closed) {
                throw new IOException("Write to closed stream");
            }
        }

        @Override
        public void write(byte[] data, int offset, int length)
                throws IOException {
            checkOpen();
            append(new String(data, offset, length));
        }

        @Override
        public void close() throws IOException {
            this.closed = true;
        }
    }
}
