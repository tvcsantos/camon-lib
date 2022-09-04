package pt.unl.fct.di.tsantos.util.collection;

import java.util.*;

public class RandomList<T> extends ArrayList<T> {

    private static final long serialVersionUID = 8295461203653334208L;
    private Random rg;

    public RandomList() {
        rg = new Random();
    }

    public RandomList(int initialCapacity) {
        super(initialCapacity);
        rg = new Random();
    }

    public RandomList(Collection<? extends T> c) {
        super(c);
        rg = new Random();
    }

    public RandomList(Iterator<? extends T> it) {
        for (; it.hasNext();) {
            add(it.next());
        }
        rg = new Random();
    }

    public T randomElement() {
        return isEmpty() ? null : get((int) (rg.nextDouble() * size()));
    }

    public T removeRandomElement() {
        return isEmpty() ? null : remove((int) (rg.nextDouble() * size()));
    }
}
