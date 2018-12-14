package com.envimate.messageMate.qcec.documentBus.givenWhenThen;


import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class When {
    private final TestDocumentBusBuilder testDocumentBusBuilder;

    public Then when(final DocumentBusActionBuilder documentBusActionBuilder) {
        return new Then(testDocumentBusBuilder, documentBusActionBuilder);
    }
}
