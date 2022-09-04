package pt.unl.fct.di.tsantos.util.math;

import java.math.BigInteger;

public interface Generator {

    /**
     * Reset the generation.
     */
    public void reset();

    /**
     * Return number of generations left.
     * @return number of generations left.
     */
    public BigInteger getNumLeft();

    /**
     * Return true if can generate more, otherwise return false.
     * @return true if can generate more, otherwise return false.
     */
    public boolean hasMore();

    /**
     * Return total number of generations.
     * @return total number of generations.
     */
    public BigInteger getTotal();

    /**
     * Return next generation.
     * @return next generation.
     */
    public int[] getNext();
}
