package pt.unl.fct.di.tsantos.util.collection;

import java.util.List;
import java.util.Map;

public interface DoubleMap<K, V> extends Map<K, V> {
    V getValue(Object key);

    List<K> getKeys(Object value);

    List<K> removeKeys(Object value);
}
