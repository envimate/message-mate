package com.envimate.messageMate.qcec.shared;

public interface TestAction<T> {

    Object execute(T t, TestEnvironment testEnvironment);
}
