package pt.unl.fct.di.tsantos.util.math;

import java.math.BigInteger;

public class PermutationGenerator extends AbstractGenerator {

    /**
     * Constructs a new generator for the Pn permutation.<br><br>
     * <b>WARNING:</b> Don't make n too large.<br><br>
     * Recall that the number of permutations is n!
     * which can be very large, even when n is as small as 20 --
     * 20! = 2,432,902,008,176,640,000 and
     * 21! is too big to fit into a Java long, which is
     * why we use BigInteger instead.
     * @param n
     */
    public PermutationGenerator(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("Min 1");
        }
        a = new int[n];
        total = getFactorial(n);
        reset();
    }

    /**
     * Generate next permutation (algorithm from Rosen p. 284).
     * @return next permutation.
     */
    public int[] getNext() {
        if (numLeft.equals(total)) {
            numLeft = numLeft.subtract(BigInteger.ONE);
            return a;
        }

        int temp;

        // Find largest index j with a[j] < a[j+1]

        int j = a.length - 2;
        while (a[j] > a[j + 1]) {
            j--;
        }

        // Find index k such that a[k] is smallest integer
        // greater than a[j] to the right of a[j]

        int k = a.length - 1;
        while (a[j] > a[k]) {
            k--;
        }

        // Interchange a[j] and a[k]

        temp = a[k];
        a[k] = a[j];
        a[j] = temp;

        // Put tail end of permutation after jth position in increasing order

        int r = a.length - 1;
        int s = j + 1;

        while (r > s) {
            temp = a[s];
            a[s] = a[r];
            a[r] = temp;
            r--;
            s++;
        }

        numLeft = numLeft.subtract(BigInteger.ONE);
        return a;
    }
}
