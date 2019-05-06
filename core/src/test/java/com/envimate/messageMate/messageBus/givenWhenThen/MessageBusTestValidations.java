package com.envimate.messageMate.messageBus.givenWhenThen;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.validations.SharedTestValidations;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestProperties.MESSAGE_RECEIVED_BY_ERROR_LISTENER;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.properties.SharedTestProperties.*;
import static com.envimate.messageMate.shared.polling.PollingUtils.pollUntilListHasSize;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.assertEquals;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.assertResultOfClass;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
final class MessageBusTestValidations {
    static void assertAmountOfSubscriberForType(final int expectedNumberOfSubscribers,
                                                final EventType eventType,
                                                final TestEnvironment testEnvironment) {
        @SuppressWarnings("unchecked")
        final Map<EventType, List<Subscriber<?>>> resultMap =
                (Map<EventType, List<Subscriber<?>>>) testEnvironment.getProperty(RESULT);
        final List<Subscriber<?>> subscribersForType = resultMap.get(eventType);
        SharedTestValidations.assertListOfSize(subscribersForType, expectedNumberOfSubscribers);
    }

    static void assertTheExceptionHandled(final Class<?> expectedExceptionClass,
                                          final TestEnvironment testEnvironment) {
        assertResultOfClass(testEnvironment, expectedExceptionClass);
        final ProcessingContext<?> processingContext = getReceivedErrorMessage(testEnvironment);
        final Object message = processingContext.getPayload();
        final Object expectedPayload = testEnvironment.getProperty(SINGLE_SEND_MESSAGE);
        assertEquals(message, expectedPayload);
        final MessageId messageId = processingContext.getMessageId();
        final Object expectedMessageId = testEnvironment.getProperty(SEND_MESSAGE_ID);
        assertEquals(messageId, expectedMessageId);
    }

    private static ProcessingContext<?> getReceivedErrorMessage(final TestEnvironment testEnvironment) {
        return testEnvironment.getPropertyAsType(MESSAGE_RECEIVED_BY_ERROR_LISTENER, ProcessingContext.class);
    }

    static void assertAllReceiverReceivedProcessingContextWithCorrectCorrelationId(final TestEnvironment testEnvironment) {
        final List<TestSubscriber<ProcessingContext<Object>>> receivers =
                getExpectedReceiverAsCorrelationBasedSubscriberList(testEnvironment);

        for (final TestSubscriber<ProcessingContext<Object>> receiver : receivers) {
            pollUntilListHasSize(receiver::getReceivedMessages, 1);
            final List<ProcessingContext<Object>> receivedMessages = receiver.getReceivedMessages();
            final ProcessingContext<Object> processingContext = receivedMessages.get(0);
            final CorrelationId expectedCorrelationId = getExpectedCorrelationId(testEnvironment);
            assertEquals(processingContext.getCorrelationId(), expectedCorrelationId);
            final Object expectedResult = testEnvironment.getProperty(SINGLE_SEND_MESSAGE);
            assertEquals(processingContext.getPayload(), expectedResult);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<TestSubscriber<ProcessingContext<Object>>> getExpectedReceiverAsCorrelationBasedSubscriberList(
            final TestEnvironment testEnvironment) {
        return (List<TestSubscriber<ProcessingContext<Object>>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
    }

    private static CorrelationId getExpectedCorrelationId(final TestEnvironment testEnvironment) {
        return testEnvironment.getPropertyAsType(EXPECTED_CORRELATION_ID, CorrelationId.class);
    }
}
