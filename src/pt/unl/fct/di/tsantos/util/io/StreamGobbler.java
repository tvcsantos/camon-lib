/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.unl.fct.di.tsantos.util.io;

import pt.unl.fct.di.tsantos.util.io.AbstractStreamGobbler;
import java.io.InputStream;

/**
 * Asynchronously read the output of a given input stream. <br />
 * Any exception during execution of the command in managed in this thread.
 * @author <a href="http://stackoverflow.com/users/6309/vonc">VonC</a>
 */
public class StreamGobbler extends AbstractStreamGobbler {

    public StreamGobbler(final InputStream anIs, final String aType) {
        super(anIs, aType);
    }

    @Override
    public void parseLine(String line) {
        System.out.println(this.type + ">" + line);
    }
    
}
