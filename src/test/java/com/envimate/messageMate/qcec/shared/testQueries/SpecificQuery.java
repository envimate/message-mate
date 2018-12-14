package com.envimate.messageMate.qcec.shared.testQueries;

import com.envimate.messageMate.qcec.queryresolving.Query;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class SpecificQuery implements Query<Integer> {
    public final int id;

    public static SpecificQuery specificQueryWithId(final int id) {
        return new SpecificQuery(id);
    }

    @Override
    public Integer result() {
        return null;
    }

    @Override
    public boolean finished() {
        return false;
    }
}
