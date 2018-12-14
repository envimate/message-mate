package com.envimate.messageMate.qcec.shared;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestReceiver<T> implements Consumer<T> {
    private final List<Object> receivedObjects = new ArrayList<>();

    public static <T> TestReceiver<T> aTestReceiver() {
        return new TestReceiver<>();
    }

    @Override
    public void accept(final T t) {
        receivedObjects.add(t);
    }

    public boolean hasReceived(final T t) {
        return receivedObjects.contains(t);
    }

    public List<Object> getReceivedObjects() {
        return receivedObjects;
    }
}
