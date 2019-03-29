package com.envimate.messageMate.soonToBeExternal.neww;

public interface UseCaseInstantiator {
    <T> T instantiate(Class<T> type);
}
