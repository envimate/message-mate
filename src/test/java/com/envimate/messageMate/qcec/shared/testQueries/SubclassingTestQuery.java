package com.envimate.messageMate.qcec.shared.testQueries;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SubclassingTestQuery implements SuperclassTestQuery {
    private int result;

    public static SubclassingTestQuery aSubclassingQuery() {
        return new SubclassingTestQuery();
    }

    @Override
    public Integer result() {
        return result;
    }

    @Override
    public boolean finished() {
        return true;
    }

    @Override
    public void reportMatch(final int result) {
        this.result = result;
    }
}
