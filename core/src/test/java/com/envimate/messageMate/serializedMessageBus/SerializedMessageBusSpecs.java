package com.envimate.messageMate.serializedMessageBus;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.serializedMessageBus.Given.given;
import static com.envimate.messageMate.serializedMessageBus.SerializedMessageBusActionBuilder.*;
import static com.envimate.messageMate.serializedMessageBus.SerializedMessageBusSetupBuilder.aSerializedMessageBus;
import static com.envimate.messageMate.serializedMessageBus.SerializedMessageBusValidationBuilder.*;

public interface SerializedMessageBusSpecs {

    //send and receive maps
    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapData(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withAMapSubscriber())
                .when(aMapDataIsSend())
                .then(expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapDataForACorrelationId(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withAMapSubscriberForACorrelationId())
                .when(aMapDataIsSendForTheGivenCorrelationId())
                .then(expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapDataAndErrorData(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withAMapSubscriber())
                .when(aMapDataWithErrorDataIsSend())
                .then(expectTheDataAndTheErrorToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapDataAndErrorDataForACorrelationId(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withAMapSubscriberForACorrelationId())
                .when(aMapDataWithErrorDataIsSendForTheGivenCorrelationId())
                .then(expectTheDataAndTheErrorToBeReceived());
    }

    //send and receive objects
    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjects(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withADeserializedSubscriber())
                .when(SerializedMessageBusActionBuilder.anObjectIsSend())
                .then(expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjectsForACorrelationId(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withADeserializedSubscriberForACorrelationId())
                .when(anObjectIsSendForACorrelationId())
                .then(expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjectAndErrorObject(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withADeserializedSubscriber())
                .when(anObjectDataWithErrorDataIsSend())
                .then(expectTheDataAndTheErrorToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjectAndErrorObjectForACorrelationId(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withADeserializedSubscriberForACorrelationId())
                .when(anObjectDataWithErrorDataIsSendForAGivenCorrelationId())
                .then(expectTheDataAndTheErrorToBeReceived());
    }

    //invokeAndWait map
    @Test
    default void testSerializedMessageBus_canWaitForMapResult(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(aMapIsSendAndTheResultIsWaited())
                .then(expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForMapResultWithTimeout(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForErrorResponse(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withASubscriberSendingDataBackAsErrorResponse())
                .when(aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(expectTheSendDataToBeReturnedAsErrorData());
    }


    @Test
    default void testSerializedMessageBus_invokeAndWaitIsStoppedByTimeOutWhenNoResultIsReceived(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig))
                .when(aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(expectTheTimeoutToBeOccurred());
    }

    //invokeAndWait objects
    @Test
    default void testSerializedMessageBus_canWaitForObjectResult(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(anObjectIsSendAndTheResultIsWaited())
                .then(expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForObjectResultWithTimeout(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(anObjectIsSendAndTheResultIsWaitedWithTimeout())
                .then(expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForObjectErrorResponse(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withASubscriberSendingDataBackAsErrorResponse())
                .when(anObjectIsSendAndTheResultIsWaited())
                .then(expectTheSendDataToBeReturnedAsErrorData());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitDeserializedIsStoppedByTimeOutWhenNoResultIsReceived(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig))
                .when(anObjectIsSendAndTheResultIsWaitedWithTimeout())
                .then(expectTheTimeoutToBeOccurred());
    }

    //invokeAndWait serializedOnly
    @Test
    default void testSerializedMessageBus_canWaitForSerializedOnlyVersion(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(anObjectIsSendAndTheNotSerializedResultIsWaited())
                .then(expectToHaveWaitedUntilTheNotSerializedResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForSerializedOnlyVersionResultWithTimeout(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(expectToHaveWaitedUntilTheNotSerializedResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForErrorResponseInSerializedOnlyVersion(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withASubscriberSendingDataBackAsErrorResponse())
                .when(anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(expectTheSendDataToBeReturnedAsNotSerializedErrorData());
    }

    @Test
    default void testSerializedMessageBus_serializedOnyVersionIsStoppedByTimeOutWhenNoResultIsReceived(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig))
                .when(anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(expectTheTimeoutToBeOccurred());
    }


    //unsubscribe
    @Test
    default void testSerializedMessageBus_canUnsubscribe(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withAMapSubscriber()
                .withADeserializedSubscriber())
                .when(theSubscriberUnsubscribe())
                .then(expectNoRemainingSubscriber());
    }

    //errors: invokeAndWait
    @Test
    default void testSerializedMessageBus_invokeAndWaitBubblesUnderlyingExceptionUp(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withASubscriberThrowingError())
                .when(aMapIsSendAndTheResultIsWaited())
                .then(expectAnExecutionExceptionWithTheCorrectCause());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitSerializedOnlyBubblesUnderlyingExceptionUp(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withASubscriberThrowingError())
                .when(anObjectIsSendAndTheResultIsWaited())
                .then(expectAnExecutionExceptionWithTheCorrectCause());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitDeserializedBubblesUnderlyingExceptionUp(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withASubscriberThrowingError())
                .when(anObjectIsSendAndTheNotSerializedResultIsWaited())
                .then(expectAnExecutionExceptionWithTheCorrectCause());
    }

    //errors: missing serialization mapping
    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingSerializationMapping_forInvokeAndWaitDeserialized(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig))
                .when(anObjectWithoutKnownSerializationIsSend())
                .then(expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingSerializationMapping_forInvokeAndWaitDeserializedWithTimeout(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig))
                .when(anObjectWithoutKnownSerializationIsSendWithTimeout())
                .then(expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingSerializationMapping_forInvokeAndSerializeOnly(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig))
                .when(anObjectWithoutKnownSerializationIsSendForInvokeAndSerializeOnly())
                .then(expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingSerializationMapping_forInvokeAndSerializeOnlyWithTimeout(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig))
                .when(anObjectWithoutKnownSerializationIsSendForInvokeAndSerializeOnlyWithTimeout())
                .then(expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    //errors: missing deserialization mapping
    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingDeserializationMapping_forInvokeAndWaitDeserialized(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(anObjectWithoutKnownReturnValueDeserializationIsSend())
                .then(expectAnExecutionExceptionFor(TestMissingDeserializationException.class));
    }

    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingDeserializationMapping_forInvokeAndSerializeOnly(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig)
                .withASubscriberSendingCorrelatedResponse())
                .when(anObjectWithoutKnownReturnValueDeserializationIsSendWithTimeout())
                .then(expectAnExecutionExceptionFor(TestMissingDeserializationException.class));
    }

    //errors: timeout when no response
    @Test
    default void testSerializedMessageBus_invokeAndWaitThrowsTimeoutExceptionWhenNoResponseIsReceived(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig))
                .when(aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(expectTheException(TimeoutException.class));
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitSerializedOnlyThrowsTimeoutExceptionWhenNoResponseIsReceived(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig))
                .when(anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(expectTheException(TimeoutException.class));
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitDeserializedOnlyThrowsTimeoutExceptionWhenNoResponseIsReceived(final SerializedMessageBusTestConfig testConfig) {
        given(aSerializedMessageBus(testConfig))
                .when(anObjectIsSendAndTheResultIsWaitedWithTimeout())
                .then(expectTheException(TimeoutException.class));
    }

}
