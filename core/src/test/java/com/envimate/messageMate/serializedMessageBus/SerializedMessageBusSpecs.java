package com.envimate.messageMate.serializedMessageBus;

import org.junit.jupiter.api.Test;

public interface SerializedMessageBusSpecs {

    //send and receive maps
    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapData(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withAMapSubscriber())
                .when(SerializedMessageBusActionBuilder.aMapDataIsSend())
                .then(SerializedMessageBusValidationBuilder.expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapDataForACorrelationId(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withAMapSubscriberForACorrelationId())
                .when(SerializedMessageBusActionBuilder.aMapDataIsSendForTheGivenCorrelationId())
                .then(SerializedMessageBusValidationBuilder.expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapDataAndErrorData(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withAMapSubscriber())
                .when(SerializedMessageBusActionBuilder.aMapDataWithErrorDataIsSend())
                .then(SerializedMessageBusValidationBuilder.expectTheDataAndTheErrorToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapDataAndErrorDataForACorrelationId(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withAMapSubscriberForACorrelationId())
                .when(SerializedMessageBusActionBuilder.aMapDataWithErrorDataIsSendForTheGivenCorrelationId())
                .then(SerializedMessageBusValidationBuilder.expectTheDataAndTheErrorToBeReceived());
    }

    //send and receive objects
    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjects(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withADeserializedSubscriber())
                .when(SerializedMessageBusActionBuilder.anObjectIsSend())
                .then(SerializedMessageBusValidationBuilder.expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjectsForACorrelationId(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withADeserializedSubscriberForACorrelationId())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendForACorrelationId())
                .then(SerializedMessageBusValidationBuilder.expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjectAndErrorObject(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withADeserializedSubscriber())
                .when(SerializedMessageBusActionBuilder.anObjectDataWithErrorDataIsSend())
                .then(SerializedMessageBusValidationBuilder.expectTheDataAndTheErrorToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjectAndErrorObjectForACorrelationId(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withADeserializedSubscriberForACorrelationId())
                .when(SerializedMessageBusActionBuilder.anObjectDataWithErrorDataIsSendForAGivenCorrelationId())
                .then(SerializedMessageBusValidationBuilder.expectTheDataAndTheErrorToBeReceived());
    }

    //invokeAndWait map
    @Test
    default void testSerializedMessageBus_canWaitForMapResult(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.aMapIsSendAndTheResultIsWaited())
                .then(SerializedMessageBusValidationBuilder.expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForMapResultWithTimeout(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitIsStoppedByTimeOutWhenNoResultIsReceived(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig))
                .when(SerializedMessageBusActionBuilder.aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectTheTimeoutToBeOccurred());
    }

    //invokeAndWait objects
    @Test
    default void testSerializedMessageBus_canWaitForObjectResult(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheResultIsWaited())
                .then(SerializedMessageBusValidationBuilder.expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForObjectResultWithTimeout(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheResultIsWaitedWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitDeserializedIsStoppedByTimeOutWhenNoResultIsReceived(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig))
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheResultIsWaitedWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectTheTimeoutToBeOccurred());
    }

    //TODO: unsubscribe
    //TODO: errors
}
