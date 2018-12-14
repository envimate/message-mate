package com.envimate.messageMate.qcec.documentBus.givenWhenThen;

import com.envimate.messageMate.qcec.domainBus.DocumentBus;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Then {
    private final TestDocumentBusBuilder testDocumentBusBuilder;
    private final DocumentBusActionBuilder documentBusActionBuilder;

    public void then(final DocumentBusValidationBuilder documentBusValidationBuilder) {
        final DocumentBus documentBus = testDocumentBusBuilder.build();
        final TestEnvironment testEnvironment = testDocumentBusBuilder.getTestEnvironment();
        final TestAction<DocumentBus> action = documentBusActionBuilder.build();
        final Object result = action.execute(documentBus, testEnvironment);
        if (result != null) {
            testEnvironment.setProperty(RESULT, result);
        }

        final TestValidation validation = documentBusValidationBuilder.build();
        validation.validate(testEnvironment);
    }
}
