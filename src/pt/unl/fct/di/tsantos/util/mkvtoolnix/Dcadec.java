/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.mkvtoolnix;

import java.io.FileNotFoundException;
import pt.unl.fct.di.tsantos.util.exceptions.NotExecutableException;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedOSException;
import pt.unl.fct.di.tsantos.util.io.ExecutableFile;

/**
 *
 * @author tvcsantos
 */
public class Dcadec extends ProgramDirectory {
    protected static Dcadec instance;

    private Dcadec() {
        super("dcadec", new String[]{"dcadec"});
    }

    public static Dcadec getInstance() {
        if (instance == null) instance = new Dcadec();
        return instance;
    }

    public ExecutableFile getProgram() throws NotExecutableException,
            FileNotFoundException, UnsupportedOSException {
        return getProgram("dcadec");
    }
}
