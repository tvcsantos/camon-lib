package pt.unl.fct.di.tsantos.util.collection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DoubleHashMap<K, V> extends HashMap<K, V>
        implements DoubleMap<K, V> {

    public static final int DHM_DEFAULT_INITIAL_CAPACITY = 16;

    public static final int DHM_MAXIMUM_CAPACITY = 1 << 30;

    public static final float DHM_DEFAULT_LOAD_FACTOR = 0.75f;

    Map<V, List<K>> v2kmap;

    public DoubleHashMap(Map<? extends K, ? extends V> m) {
        super(m);
        v2kmap = new HashMap<V, List<K>>(
                Math.max((int) (m.size() / DHM_DEFAULT_LOAD_FACTOR) + 1,
                DHM_DEFAULT_INITIAL_CAPACITY), DHM_DEFAULT_LOAD_FACTOR);
    }

    public DoubleHashMap() {
        super();
        v2kmap = new HashMap<V, List<K>>();
    }

    public DoubleHashMap(int initialCapacity) {
        super(initialCapacity);
        v2kmap = new HashMap<V, List<K>>(initialCapacity);
    }

    public DoubleHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        v2kmap = new HashMap<V, List<K>>(initialCapacity, loadFactor);
    }

    @Override
    public V put(K key, V value) {
        V oldVal = super.put(key, value);

        List<K> list = v2kmap.get(oldVal);
        if (list != null && !list.isEmpty()) {
            list.remove(key);
            if (list.isEmpty()) v2kmap.remove(oldVal);
        }

        list = v2kmap.get(value);
        if (list == null) {
            list = new LinkedList<K>();
            list.add(key);
            v2kmap.put(value, list);
        } else {
            list.add(key);
        }

        return oldVal;
    }

    public V getValue(Object key) {
        return get(key);
    }

    @Override
    public boolean containsValue(Object value) {
        /*List<K> keys = v2kmap.get(value);
        if (keys == null || keys.isEmpty()) {
            return false;
        } else {
            return true;
        }*/
        return v2kmap.containsKey(value);
    }

    @Override
    public V remove(Object key) {
        V val = super.remove(key);

        List<K> list = v2kmap.get(val);
        if (list != null && !list.isEmpty()) {
            list.remove(key);
            if (list.isEmpty()) v2kmap.remove(val);
        }

        return val;
    }

    public List<K> getKeys(Object value) {
        List<K> keys = v2kmap.get(value);
        if (keys == null) return new LinkedList<K>();
        else return keys;
    }

    public List<K> removeKeys(Object value) {
        List<K> keys = v2kmap.get(value);
        if (keys == null) keys = new LinkedList<K>();
        else keys = new LinkedList<K>(keys);
        for (K key : keys) {
            remove(key);
        }
        return keys;
    }
}
