package com.envimate.messageMate.useCaseConnecting;

import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.useCaseConnecting.Given.given;
import static com.envimate.messageMate.useCaseConnecting.UseCaseConnectorActionBuilder.*;
import static com.envimate.messageMate.useCaseConnecting.UseCaseConnectorSetupBuilder.aConfiguredUseCaseConnector;
import static com.envimate.messageMate.useCaseConnecting.UseCaseConnectorValidationBuilder.*;

class UseCaseConnectorSpecs {

    @Test
    void testUseCaseConnector_callsCallbackWithResponse() {
        given(aConfiguredUseCaseConnector())
                .when(aSuccessfulResponseIsReceived())
                .expect(theCallbackToBeCalledWithTheResponse());
    }

    @Test
    void testUseCaseConnector_callsCallbackWithThrownException() {
        given(aConfiguredUseCaseConnector())
                .when(anExceptionIsThrown())
                .expect(theCallbackToBeCalledWithTheResponse());
    }

    @Test
    void testUseCaseConnector_doesNotAcceptNewMessagesWhenClosed() {
        given(aConfiguredUseCaseConnector())
                .when(aMessagesIsSendAfterClose())
                .expect(anExceptionOfType(IllegalStateException.class));
    }

    @Test
    void testUseCaseConnector_closeIsIdempotent() {
        given(aConfiguredUseCaseConnector())
                .when(closeIsCalledSeveralTimes())
                .expect(noExceptionToBeOccurred());
    }

    @Test
    void testUseCaseConnector_closeRemovesSubscriberFromMessageBus() {
        given(aConfiguredUseCaseConnector())
                .when(closeIsCalled())
                .expect(expectTheSubscriberToBeRemovedFromTheMessageBus());
    }
}