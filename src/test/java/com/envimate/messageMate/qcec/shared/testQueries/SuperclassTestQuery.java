package com.envimate.messageMate.qcec.shared.testQueries;


import com.envimate.messageMate.qcec.queryresolving.Query;

public interface SuperclassTestQuery extends Query<Integer> {

    void reportMatch(final int result);
}
