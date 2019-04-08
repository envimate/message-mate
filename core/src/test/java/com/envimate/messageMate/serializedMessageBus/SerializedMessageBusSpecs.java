package com.envimate.messageMate.serializedMessageBus;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

//TODO: test invokeAndWaitWithError / without Error
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

    //invokeAndWait serializedOnly
    @Test
    default void testSerializedMessageBus_canWaitForSerializedOnlyVersion(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheNotSerializedResultIsWaited())
                .then(SerializedMessageBusValidationBuilder.expectToHaveWaitedUntilTheNotSerializedResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForSerializedOnyVersionResultWithTimeout(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(SerializedMessageBusValidationBuilder.expectToHaveWaitedUntilTheNotSerializedResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_serializedOnyVersionIsStoppedByTimeOutWhenNoResultIsReceived(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig))
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(SerializedMessageBusValidationBuilder.expectTheTimeoutToBeOccurred());
    }


    //unsubscribe
    @Test
    default void testSerializedMessageBus_canUnsubscribe(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withAMapSubscriber()
                .withADeserializedSubscriber())
                .when(SerializedMessageBusActionBuilder.theSubscriberUnsubscribe())
                .then(SerializedMessageBusValidationBuilder.expectNoRemainingSubscriber());
    }

    //errors: invokeAndWait
    @Test
    default void testSerializedMessageBus_invokeAndWaitBubblesUnderlyingExceptionUp(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withASubscriberThrowingError())
                .when(SerializedMessageBusActionBuilder.aMapIsSendAndTheResultIsWaited())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionWithTheCorrectCause());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitSerializedOnlyBubblesUnderlyingExceptionUp(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withASubscriberThrowingError())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheResultIsWaited())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionWithTheCorrectCause());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitDeserializedBubblesUnderlyingExceptionUp(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withASubscriberThrowingError())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheNotSerializedResultIsWaited())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionWithTheCorrectCause());
    }

    //errors: missing serialization mapping
    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingSerializationMapping_forInvokeAndWaitDeserialized(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig))
                .when(SerializedMessageBusActionBuilder.anObjectWithoutKnownSerializationIsSend())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingSerializationMapping_forInvokeAndWaitDeserializedWithTimeout(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig))
                .when(SerializedMessageBusActionBuilder.anObjectWithoutKnownSerializationIsSendWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingSerializationMapping_forInvokeAndSerializeOnly(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig))
                .when(SerializedMessageBusActionBuilder.anObjectWithoutKnownSerializationIsSendForInvokeAndSerializeOnly())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingSerializationMapping_forInvokeAndSerializeOnlyWithTimeout(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig))
                .when(SerializedMessageBusActionBuilder.anObjectWithoutKnownSerializationIsSendForInvokeAndSerializeOnlyWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    //errors: missing deserialization mapping
    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingDeserializationMapping_forInvokeAndWaitDeserialized(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.anObjectWithoutKnownReturnValueDeserializationIsSend())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionFor(TestMissingDeserializationException.class));
    }

    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingDeserializationMapping_forInvokeAndSerializeOnly(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.anObjectWithoutKnownReturnValueDeserializationIsSendWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionFor(TestMissingDeserializationException.class));
    }

    //errors: timeout when no response
    @Test
    default void testSerializedMessageBus_invokeAndWaitThrowsTimeoutExceptionWhenNoResponseIsReceived(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig))
                .when(SerializedMessageBusActionBuilder.aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectTheException(TimeoutException.class));
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitSerializedOnlyThrowsTimeoutExceptionWhenNoResponseIsReceived(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig))
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(SerializedMessageBusValidationBuilder.expectTheException(TimeoutException.class));
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitDeserializedOnlyThrowsTimeoutExceptionWhenNoResponseIsReceived(final SerializedMessageBusTestConfig testConfig) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(testConfig))
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheResultIsWaitedWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectTheException(TimeoutException.class));
    }

}
