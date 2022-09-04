package pt.unl.fct.di.tsantos.util.exceptions;

public class IncorrectSizeException extends Exception {

    private int low;
    private int high;
    private int size;

    public IncorrectSizeException(int low, int high,
            int size, Throwable cause) {
        super(cause);
        this.low = low;
        this.high = high;
        this.size = size;
    }

    public IncorrectSizeException(int low, int high, int size,
            String message, Throwable cause) {
        super(message, cause);
        this.low = low;
        this.high = high;
        this.size = size;
    }

    public IncorrectSizeException(int low, int high, int size,
            String message) {
        super(message);
        this.low = low;
        this.high = high;
        this.size = size;
    }

    public IncorrectSizeException(int low, int high, int size) {
        this.low = low;
        this.high = high;
        this.size = size;
    }

    public boolean isToSmall() {
        return size < low;
    }

    public boolean isToBig() {
        return size > high;
    }

}
