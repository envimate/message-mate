package com.envimate.messageMate.qcec.shared;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestEnvironment {
    private final Map<String, Object> definedPropertiesMap = new ConcurrentHashMap<>();

    public static TestEnvironment emptyTestEnvironment() {
        return new TestEnvironment();
    }

    public void setProperty(final TestEnvironmentProperty property, final Object o) {
        setProperty(property.name(), o);
    }

    public void setProperty(final String property, final Object o) {
        definedPropertiesMap.put(property, o);
    }

    public synchronized void addToListProperty(final String property, final Object... os) {
        for (Object o : os) {
            addToListProperty(property, o);
        }
    }

    public synchronized void addToListProperty(final TestEnvironmentProperty property, final Object... os) {
        for (Object o : os) {
            addToListProperty(property, o);
        }
    }

    public synchronized void addToListProperty(final TestEnvironmentProperty property, final Object o) {
        addToListProperty(property.name(), o);
    }

    public synchronized void addToListProperty(final String property, final Object o) {
        @SuppressWarnings("unchecked")
        final List<Object> list = (List<Object>) definedPropertiesMap.getOrDefault(property, new LinkedList<>());
        list.add(o);
        definedPropertiesMap.put(property, list);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPropertyAsType(final TestEnvironmentProperty property, final Class<T> tClass) {
        return (T) getProperty(property);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPropertyAsType(final String property, final Class<T> tClass) {
        return (T) getProperty(property);
    }

    public Object getProperty(final TestEnvironmentProperty property) {
        return getProperty(property.name());
    }

    public Object getProperty(final String property) {
        final Object object = definedPropertiesMap.get(property);
        if (object != null) {
            return object;
        } else {
            throw new RuntimeException("Property " + property + " not set.");
        }
    }

    public boolean has(final TestEnvironmentProperty property) {
        return has(property.name());
    }

    public boolean has(final String property) {
        return definedPropertiesMap.containsKey(property);
    }
}
