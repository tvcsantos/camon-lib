package pt.unl.fct.di.tsantos.util.mkvtoolnix;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.exec.ExecuteException;
import pt.unl.fct.di.tsantos.util.exceptions.NotExecutableException;
import pt.unl.fct.di.tsantos.util.exceptions.UnsupportedOSException;
import pt.unl.fct.di.tsantos.util.io.ExecutableFile;

/**
 *
 * @author tvcsantos
 */
public class Eac3to extends ProgramDirectory {
    protected static Eac3to instance;
    
    private Eac3to() {
        super("eac3to", new String[]{"eac3to"});
    }    
    
    public static Eac3to getInstance() {
        if (instance == null) instance = new Eac3to();
        return instance;
    }

    public ExecutableFile getProgram() throws NotExecutableException,
            FileNotFoundException, UnsupportedOSException {
        return getProgram("eac3to");
    }
    
    public static void main(String[] args) throws NotExecutableException,
            FileNotFoundException, UnsupportedOSException, 
            ExecuteException, IOException {
        Eac3to eac3to = Eac3to.getInstance();
        ExecutableFile program = eac3to.getProgram("eac3t");
        program.execute(new String[]{});
    }
}
