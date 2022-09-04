package pt.unl.fct.di.tsantos.util.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArraysExtended {
    private ArraysExtended() {
        throw new UnsupportedOperationException();
    }

    public static <T> T[] intersection(T[] a1, T[] a2) {
        if (a1 == null)
            throw new NullPointerException();
        if (a2 == null)
            throw new NullPointerException();
        if(!a1.getClass().equals(a2.getClass()))
            throw new IllegalArgumentException("the two arrays must be" +
                    " from the same class");
        List<T> list = new ArrayList<T>();
        for (T elem : a1) if (contains(a2, elem)) list.add(elem);
        return (T[]) list.toArray();
    }

    public static <T> T[] union(T[] a1, T[] a2) {
        if (a1 == null)
            throw new NullPointerException();
        if (a2 == null)
            throw new NullPointerException();
        if(!a1.getClass().equals(a2.getClass()))
            throw new IllegalArgumentException("the two arrays must be" +
                    " from the same class");
        List<T> list = new ArrayList<T>();
        for (T elem : a1) if (!contains(a2, elem)) list.add(elem);
        list.addAll(Arrays.asList(a2));
        return (T[]) list.toArray();
    }

    public static <T> boolean contains(T[] array, T element) {
        if (array == null)
            throw new NullPointerException();
        String name = array.getClass().toString();
        int index = name.indexOf("[L");
        String elemsClass = name.substring(index + 2, name.length() - 1);
        try {
            Class<?> theClass = Class.forName(elemsClass);
            if (!theClass.equals(element.getClass()))
            throw new IllegalArgumentException("element is not from the same" +
                    " class as the elements in the array");
            //System.out.println(array.getClass());
            //Arrays.equals(array, array);
            for (T elem : array) if(elem.equals(element)) return true;
            return false;
        } catch (ClassNotFoundException ex) {
            RuntimeException e = new RuntimeException(ex);
            //e.initCause(ex);
            throw e;
        }
    }
}
