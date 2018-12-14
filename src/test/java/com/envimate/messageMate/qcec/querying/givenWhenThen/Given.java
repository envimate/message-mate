package com.envimate.messageMate.qcec.querying.givenWhenThen;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Given {
    public static When given(final TestQueryResolver aQueryResolver) {
        return new When(aQueryResolver);
    }
}
