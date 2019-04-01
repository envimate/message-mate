package com.envimate.messageMate.messageBus.givenWhenThen;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.TestEventType;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.AsynchronousSendingTestUtils;
import com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber;
import com.envimate.messageMate.shared.subscriber.ExceptionThrowingTestSubscriber;
import com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.envimate.messageMate.identification.CorrelationId.newUniqueCorrelationId;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestProperties.EVENT_TYPE;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.TestEventType.testEventType;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.*;
import static com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber.blockingTestSubscriber;
import static com.envimate.messageMate.shared.subscriber.ExceptionThrowingTestSubscriber.exceptionThrowingTestSubscriber;
import static com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber.testSubscriber;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;

public class MessageBusTestActions {

    public static void sendASingleMessage(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendASingleMessage(messageBus, testEnvironment, eventType);
    }

    public static void sendASingleMessage(final MessageBus messageBus, final TestEnvironment testEnvironment, final EventType eventType) {
        final TestMessageOfInterest message = messageOfInterest();
        testEnvironment.setProperty(SINGLE_SEND_MESSAGE, message);
        final MessageId messageId = messageBus.send(eventType, message);
        testEnvironment.setProperty(SEND_MESSAGE_ID, messageId);
    }

    public static void sendTheMessage(final MessageBus messageBus, final TestEnvironment testEnvironment, final Object message) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        testEnvironment.setProperty(SINGLE_SEND_MESSAGE, message);
        final MessageId messageId = messageBus.send(eventType, message);
        testEnvironment.setProperty(SEND_MESSAGE_ID, messageId);
    }

    public static void sendAMessageWithCorrelationId(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final CorrelationId correlationId = testEnvironment.getPropertyOrSetDefault(EXPECTED_CORRELATION_ID, newUniqueCorrelationId());
        testEnvironment.setProperty(EXPECTED_RESULT, correlationId);

        final TestMessageOfInterest message = messageOfInterest();
        testEnvironment.setProperty(SINGLE_SEND_MESSAGE, message);

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final MessageId messageId = messageBus.send(eventType, message, correlationId);
        testEnvironment.setProperty(SEND_MESSAGE_ID, messageId);
    }


    public static void sendSeveralMessages(final MessageBus messageBus, final TestEnvironment testEnvironment, final int numberOfMessages) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final List<TestMessageOfInterest> messages = new LinkedList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            messageBus.send(eventType, message);
            messages.add(message);
        }
        testEnvironment.setProperty(MESSAGES_SEND_OF_INTEREST, messages);
    }

    public static void sendMessagesAsynchronously(final MessageBus messageBus,
                                                  final TestEnvironment testEnvironment,
                                                  final int numberOfSenders,
                                                  final int numberOfMessages,
                                                  final boolean expectCleanShutdown) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, TestEventType.testEventType());
        final Consumer<TestMessage> sendConsumer = testMessage -> messageBus.send(eventType, testMessage);
        AsynchronousSendingTestUtils.sendValidMessagesAsynchronously(sendConsumer, testEnvironment, numberOfSenders, numberOfMessages, expectCleanShutdown);
    }

    public static void sendInvalidMessagesAsynchronously(final MessageBus messageBus,
                                                         final TestEnvironment testEnvironment,
                                                         final int numberOfSenders,
                                                         final int numberOfMessages) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, TestEventType.testEventType());
        final Consumer<TestMessage> sendConsumer = testMessage -> messageBus.send(eventType, testMessage);
        AsynchronousSendingTestUtils.sendInvalidMessagesAsynchronously(sendConsumer, testEnvironment, numberOfSenders, numberOfMessages);
    }

    public static void sendInvalidAndInvalidMessagesAsynchronously(final MessageBus messageBus,
                                                                   final TestEnvironment testEnvironment,
                                                                   final int numberOfSenders,
                                                                   final int numberOfMessages) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, TestEventType.testEventType());
        final Consumer<TestMessage> sendConsumer = testMessage -> messageBus.send(eventType, testMessage);
        AsynchronousSendingTestUtils.sendMixtureOfValidAndInvalidMessagesAsynchronously(sendConsumer, testEnvironment, numberOfSenders, numberOfMessages);
    }

    public static void addASingleSubscriber(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = TestEventType.testEventType();
        testEnvironment.setPropertyIfNotSet(EVENT_TYPE, eventType);
        addASingleSubscriber(messageBus, testEnvironment, eventType);
    }

    public static void addASingleSubscriber(final MessageBus messageBus, final TestEnvironment testEnvironment, final EventType eventType) {
        final SimpleTestSubscriber<Object> subscriber = testSubscriber();
        addASingleSubscriber(messageBus, testEnvironment, eventType, subscriber);
    }

    public static void addASingleSubscriber(final MessageBus messageBus, final TestEnvironment testEnvironment, final EventType eventType,
                                            final Subscriber<Object> subscriber) {
        messageBus.subscribe(eventType, subscriber);
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        testEnvironment.addToListProperty(INITIAL_SUBSCRIBER, subscriber);
    }

    public static void withSeveralSubscriber(final MessageBus messageBus, final TestEnvironment testEnvironment, final int numberOfSubscribers) {
        final EventType eventType = TestEventType.testEventType();
        for (int i = 0; i < numberOfSubscribers; i++) {
            addASingleSubscriber(messageBus, testEnvironment, eventType);
        }
        testEnvironment.setPropertyIfNotSet(EVENT_TYPE, eventType);
    }


    public static void addASingleRawSubscriber(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = TestEventType.testEventType();
        testEnvironment.setPropertyIfNotSet(EVENT_TYPE, eventType);
        addASingleRawSubscriber(messageBus, testEnvironment, eventType);
    }

    public static void addASingleRawSubscriber(final MessageBus messageBus, final TestEnvironment testEnvironment, final EventType eventType) {
        final SimpleTestSubscriber<ProcessingContext<Object>> subscriber = testSubscriber();
        messageBus.subscribeRaw(eventType, subscriber);
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        testEnvironment.addToListProperty(INITIAL_SUBSCRIBER, subscriber);
    }

    public static void addASubscriberThatBlocksWhenAccepting(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = TestEventType.testEventType();
        testEnvironment.setPropertyIfNotSet(EVENT_TYPE, eventType);
        final Semaphore semaphore = new Semaphore(0);
        final BlockingTestSubscriber<Object> subscriber = blockingTestSubscriber(semaphore);
        addASingleSubscriber(messageBus, testEnvironment, eventType, subscriber);
        testEnvironment.setProperty(EXECUTION_END_SEMAPHORE, semaphore);
    }

    public static void addAErrorThrowingSubscriber(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = TestEventType.testEventType();
        testEnvironment.setPropertyIfNotSet(EVENT_TYPE, eventType);
        final ExceptionThrowingTestSubscriber<Object> subscriber = exceptionThrowingTestSubscriber();
        messageBus.subscribe(eventType, subscriber);
    }

    public static void sendMessagesBeforeShutdownAsynchronously(final MessageBus messageBus, final TestEnvironment testEnvironment,
                                                                final int numberOfSenders, final int numberOfMessages) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, TestEventType.testEventType());
        final BiConsumer<EventType, Subscriber<Object>> subscriberConsumer = (e, subscriber) -> messageBus.subscribe(eventType, subscriber);
        final BiConsumer<EventType, TestMessage> sendConsumer = (e, testMessage) -> messageBus.send(eventType, testMessage);
        final Consumer<Boolean> closeConsumer = finishRemainingTasks -> messageBus.close(false);
        AsynchronousSendingTestUtils.sendMessagesBeforeShutdownAsynchronously(subscriberConsumer, sendConsumer, closeConsumer, testEnvironment, numberOfSenders, numberOfMessages);
    }

    public static void sendMessagesBeforeAndAfterShutdownAsynchronously(final MessageBus messageBus, final TestEnvironment testEnvironment,
                                                                        final int numberOfSender, final boolean finishRemainingTasks) {
        final BiConsumer<EventType, Subscriber<Object>> subscriberConsumer = (e, subscriber) -> messageBus.subscribe(e, subscriber);
        final BiConsumer<EventType, Object> sendConsumer = (e, testMessage) -> messageBus.send(e, testMessage);
        final Consumer<Boolean> closeConsumer = b -> messageBus.close(b);
        AsynchronousSendingTestUtils.sendMessagesBeforeAndAfterShutdownAsynchronously(subscriberConsumer, sendConsumer, closeConsumer,
                testEnvironment, numberOfSender, 0, finishRemainingTasks);
    }

    public static void addDynamicErrorListenerForEventType(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final SubscriptionId subscriptionId = messageBus.onException(eventType, (m, e) -> {
            testEnvironment.setPropertyIfNotSet(RESULT, e);
        });
        testEnvironment.setProperty(USED_SUBSCRIPTION_ID, subscriptionId);
    }

    public static void addTwoDynamicErrorListenerForEventType_whereTheFirstWillBeRemoved(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final SubscriptionId subscriptionId = messageBus.onException(eventType, (m, e) -> {
            throw new RuntimeException("Should not be called");
        });
        testEnvironment.setProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        messageBus.onException(eventType, (m, e) -> {
            testEnvironment.setPropertyIfNotSet(RESULT, e);
        });
    }
}
