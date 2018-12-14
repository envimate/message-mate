package com.envimate.messageMate.qcec.shared.testQueries;


import com.envimate.messageMate.qcec.queryresolving.Query;

public final class TestQuery implements Query<Integer> {
    private final boolean returnsResult;
    private boolean isFinished;
    private int result;

    private TestQuery(final boolean returnsResult) {
        this.returnsResult = returnsResult;
    }

    public static TestQuery aTestQuery() {
        return new TestQuery(true);
    }

    public static TestQuery aTestQueryWithoutResult() {
        return new TestQuery(false);
    }

    @Override
    public Integer result() {
        if (returnsResult) {
            return result;
        } else {
            return null;
        }
    }

    @Override
    public boolean finished() {
        return isFinished;
    }

    public void finishQuery() {
        this.isFinished = true;
    }


    public void setResult(final int result) {
        this.result = result;
    }

    public void addPartialResult(final int result) {
        this.result += result;
    }
}
