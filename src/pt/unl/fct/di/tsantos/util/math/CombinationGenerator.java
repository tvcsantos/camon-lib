package pt.unl.fct.di.tsantos.util.math;

import java.math.BigInteger;

/**
 * Systematically generate combinations.
 */
public class CombinationGenerator extends AbstractGenerator {

    protected int n;
    protected int r;

    /**
     * Constructs a new generator for the nCr combination.
     * @param n
     * @param r
     */
    public CombinationGenerator(int n, int r) {
        if (r > n) {
            throw new IllegalArgumentException();
        }
        if (n < 1) {
            throw new IllegalArgumentException();
        }
        this.n = n;
        this.r = r;
        a = new int[r];
        BigInteger nFact = getFactorial(n);
        BigInteger rFact = getFactorial(r);
        BigInteger nminusrFact = getFactorial(n - r);
        total = nFact.divide(rFact.multiply(nminusrFact));
        reset();
    }

    /**
     * Generate next combination (algorithm from Rosen p. 286).
     * @return next combination.
     */
    public int[] getNext() {
        if (numLeft.equals(total)) {
            numLeft = numLeft.subtract(BigInteger.ONE);
            return a;
        }

        int i = r - 1;
        while (a[i] == n - r + i) {
            i--;
        }
        a[i] = a[i] + 1;
        for (int j = i + 1; j < r; j++) {
            a[j] = a[i] + j - i;
        }

        numLeft = numLeft.subtract(BigInteger.ONE);
        return a;
    }
}
