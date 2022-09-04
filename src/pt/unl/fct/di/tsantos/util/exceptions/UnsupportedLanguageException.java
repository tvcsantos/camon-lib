package pt.unl.fct.di.tsantos.util.exceptions;

public class UnsupportedLanguageException extends Exception {

    public UnsupportedLanguageException(Throwable cause) {
        super(cause);
    }

    public UnsupportedLanguageException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedLanguageException(String message) {
        super(message);
    }

    public UnsupportedLanguageException() {
    }

}
