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
public class Aften extends ProgramDirectory {
    protected static Aften instance;

    private Aften() {
        super("aften", new String[]{"aften"});
    }

    public static Aften getInstance() {
        if (instance == null) instance = new Aften();
        return instance;
    }

    public ExecutableFile getProgram() throws NotExecutableException,
            FileNotFoundException, UnsupportedOSException {
        return getProgram("aften");
    }
}
