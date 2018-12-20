package com.envimate.messageMate.qcec.shared;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestEnvironment {
    private final Map<TestEnvironmentProperty, Object> definedPropertiesMap = new ConcurrentHashMap<>();

    public static TestEnvironment emptyTestEnvironment() {
        return new TestEnvironment();
    }

    public void setProperty(final TestEnvironmentProperty property, final Object o) {
        definedPropertiesMap.put(property, o);
    }

    public synchronized void addToListProperty(final TestEnvironmentProperty property, final Object o) {
        final List<Object> list = (List<Object>) definedPropertiesMap.getOrDefault(property, new LinkedList<>());
        list.add(o);
        definedPropertiesMap.put(property, list);
    }

    public <T> T getPropertyAsType(final TestEnvironmentProperty property, final Class<T> tClass) {
        return (T) getProperty(property);
    }

    public Object getProperty(final TestEnvironmentProperty property) {
        final Object object = definedPropertiesMap.get(property);
        if (object != null) {
            return object;
        } else {
            throw new RuntimeException("Property " + property + " not set.");
        }
    }

    public boolean has(final TestEnvironmentProperty property) {
        return definedPropertiesMap.containsKey(property);
    }
}