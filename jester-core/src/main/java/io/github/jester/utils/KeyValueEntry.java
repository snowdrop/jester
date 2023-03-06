package io.github.jester.utils;

import java.util.Map;

public class KeyValueEntry<K, V> implements Map.Entry<K, V> {

    private final K key;
    private final V value;

    public KeyValueEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }
}
