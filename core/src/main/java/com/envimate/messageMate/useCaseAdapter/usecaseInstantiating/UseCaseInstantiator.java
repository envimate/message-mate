package com.envimate.messageMate.useCaseAdapter.usecaseInstantiating;

public interface UseCaseInstantiator {
    <T> T instantiate(Class<T> type);
}
