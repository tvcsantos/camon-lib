package pt.unl.fct.di.tsantos.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CollectionUtilities {

    private CollectionUtilities() {
        throw new UnsupportedOperationException();
    }

    public static <K, V> Map<K, List<V>> merge(Map<K, List<V>> m1,
            Map<K, List<V>> m2) {
        Map<K, List<V>> res = new HashMap<K, List<V>>();
        Set<Entry<K, List<V>>> s2 = m2.entrySet();
        res.putAll(m1);
        for (Entry<K, List<V>> e : s2) {
            K key = e.getKey();
            List<V> value = e.getValue();
            List<V> list = res.get(key);
            if (list == null) {
                res.put(key, value);
            } else {
                list.addAll(value);
            }
        }
        return res;
    }

    public static <K, V> void mergeD(Map<K, List<V>> m1,
            Map<K, List<V>> m2) {
        Set<Entry<K, List<V>>> s2 = m2.entrySet();
        for (Entry<K, List<V>> e : s2) {
            K key = e.getKey();
            List<V> value = e.getValue();
            List<V> list = m1.get(key);
            if (list == null) {
                m1.put(key, value);
            } else {
                list.addAll(value);
            }
        }
    }

    public static <K, V> List<V> put(K key, V value, Map<K, List<V>> m) {
        List<V> l = m.get(key);
        if (l != null) {
            l.add(value);
        } else {
            l = new LinkedList<V>();
            l.add(value);
            m.put(key, l);
        }
        return l;
    }

    public static <T> List<T> flatten(List<?> list) {
        List<T> retVal = new LinkedList<T>();
        flatten(list, retVal);
        return retVal;
    }

    @SuppressWarnings("unchecked")
    public static <T> void flatten(List<?> fromTreeList, List<T> toFlatList) {
        for (Object item : fromTreeList) {
            if (item instanceof List<?>) {
                flatten((List<?>) item, toFlatList);
            } else {
                toFlatList.add((T) item);
            }
        }
    }

    public static <T> Collection<T> flattenCollection(Collection<?> list) {
        List<T> retVal = new LinkedList<T>();
        flattenCollection(list, retVal);
        return retVal;
    }

    @SuppressWarnings("unchecked")
    public static <T> void flattenCollection(Collection<?> fromTreeList,
            List<T> toFlatList) {
        for (Object item : fromTreeList) {
            if (item instanceof Collection<?>) {
                flattenCollection((Collection<?>) item, toFlatList);
            } else {
                toFlatList.add((T) item);
            }
        }
    }

    public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
        Set<Set<T>> sets = new HashSet<Set<T>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new LinkedList<T>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
        for (Set<T> set : powerSet(rest)) {
            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    public static <T> void copyInto(Collection<T> collection, T[] array) {
        int i = 0; for(T elem : collection) array[i++] = elem;
    }
}
