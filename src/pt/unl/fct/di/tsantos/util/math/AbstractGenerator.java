package pt.unl.fct.di.tsantos.util.math;

import java.math.BigInteger;

public abstract class AbstractGenerator implements Generator {

    protected int[] a;
    protected BigInteger numLeft;
    protected BigInteger total;

    public void reset() {
        for (int i = 0; i < a.length; i++) {
            a[i] = i;
        }
        numLeft = new BigInteger(total.toString());
    }

    public BigInteger getNumLeft() {
        return numLeft;
    }

    public boolean hasMore() {
        return numLeft.compareTo(BigInteger.ZERO) == 1;
    }

    public BigInteger getTotal() {
        return total;
    }

    /**
     * Compute factorial of n
     * @param n the factorial to compute
     * @return the factorial of n
     */
    public static BigInteger getFactorial(int n) {
        BigInteger fact = BigInteger.ONE;
        for (int i = n; i > 1; i--) {
            fact = fact.multiply(new BigInteger(Integer.toString(i)));
        }
        return fact;
    }
}
