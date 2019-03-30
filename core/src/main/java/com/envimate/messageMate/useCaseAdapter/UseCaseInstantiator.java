package com.envimate.messageMate.useCaseAdapter;

public interface UseCaseInstantiator {
    <T> T instantiate(Class<T> type);
}
