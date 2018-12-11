package com.envimate.messageMate.shared.context;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestExecutionContext {
    private final Map<TestExecutionProperty, Object> definedPropertiesMap = new ConcurrentHashMap<>();

    public static TestExecutionContext emptyExecutionContext() {
        return new TestExecutionContext();
    }

    public void setProperty(final TestExecutionProperty property, final Object o) {
        definedPropertiesMap.put(property, o);
    }

    public synchronized void addToListProperty(final TestExecutionProperty property, final Object o) {
        final List<Object> list = (List<Object>) definedPropertiesMap.getOrDefault(property, new LinkedList<>());
        list.add(o);
        definedPropertiesMap.put(property, list);
    }

    public <T> T getPropertyAsType(final TestExecutionProperty property, final Class<T> tClass) {
        return (T) getProperty(property);
    }

    public Object getProperty(final TestExecutionProperty property) {
        final Object object = definedPropertiesMap.get(property);
        if (object != null) {
            return object;
        } else {
            throw new RuntimeException("Property " + property + " not set.");
        }
    }

    public boolean has(final TestExecutionProperty property) {
        return definedPropertiesMap.containsKey(property);
    }
}
