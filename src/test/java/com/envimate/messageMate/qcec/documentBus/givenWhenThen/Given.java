package com.envimate.messageMate.qcec.documentBus.givenWhenThen;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Given {
    public static When given(final TestDocumentBusBuilder testDocumentBusBuilder) {
        return new When(testDocumentBusBuilder);
    }
}
