/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util;

import java.util.Arrays;

/**
 *
 * @author tvcsantos
 */
public class Tuple {
    Object[] tuple;

    /*public Tuple(int size) {
        tuple = new Object[size];
    }*/

    public Tuple(Object ... elements) {
        tuple = elements;
    }

    public Object get(int pos) {
        if (pos < 0 || pos >= tuple.length)
            throw new ArrayIndexOutOfBoundsException(pos);
        return tuple[pos];
    }

    public <T> T get(int pos, Class<T> aClass) {
        return aClass.cast(get(pos));
    }

    public void set(int pos, Object o) {
        if (pos < 0 || pos >= tuple.length)
            throw new ArrayIndexOutOfBoundsException(pos);
        tuple[pos] = o;
    }

    @Override
    public String toString() {
        String result = "(";
        if (tuple.length > 0) result +=
                (tuple[0] == null ? "null" : tuple[0].toString());
        for (int i = 1 ; i < tuple.length ; i++) {
            result += ", " + (tuple[i] == null ? "null" : tuple[i].toString());
        }
        result += ")";
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tuple other = (Tuple) obj;
        if (!Arrays.deepEquals(this.tuple, other.tuple)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Arrays.deepHashCode(this.tuple);
        return hash;
    }
}
