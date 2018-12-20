package com.envimate.messageMate.qcec.querying.givenWhenThen;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class When {
    private final TestQueryResolver testQueryResolver;

    public Then when(final QueryActionBuilder queryActionBuilder) {
        return new Then(testQueryResolver, queryActionBuilder);
    }
}