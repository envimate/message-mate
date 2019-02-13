package com.envimate.messageMate.shared.channelMessageBus.givenWhenThen;

import com.envimate.messageMate.internal.statistics.MessageStatistics;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.AsynchronousSendingTestUtils.*;
import static com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusTestProperties.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelMessageBusTestActions {

    public static void sendASingleMessage(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
        testEnvironment.setProperty(SINGLE_SEND_MESSAGE, message);
        sutActions.send(message);
    }

    public static void sendSeveralMessages(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment, final int numberOfMessages) {
        final List<TestMessageOfInterest> messages = new LinkedList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            sutActions.send(message);
            messages.add(message);
        }
        testEnvironment.setProperty(MESSAGES_SEND_OF_INTEREST, messages);
    }

    public static void sendSeveralMessagesInTheirOwnThread(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment,
                                                           final int numberOfSender, final int numberOfMessagesPerSender, final boolean expectCleanShutdown) {
        sendValidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, expectCleanShutdown);
    }

    public static void sendBothValidAndInvalidMessagesAsynchronously(final ChannelMessageBusSutActions sutActions,
                                                                     final TestEnvironment testEnvironment,
                                                                     final int numberOfSender, final int numberOfMessagesPerSender) {
        sendMixtureOfValidAndInvalidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender);
    }

    public static void sendSeveralInvalidMessagesAsynchronously(final ChannelMessageBusSutActions sutActions,
                                                                final TestEnvironment testEnvironment,
                                                                final int numberOfSender, final int numberOfMessagesPerSender) {
        sendInvalidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender);
    }

    public static void sendXMessagesAShutdownsIsCalledThenSendsYMessage(final ChannelMessageBusSutActions sutActions,
                                                                        final TestEnvironment testEnvironment,
                                                                        final int numberOfMessagesBeforeShutdown,
                                                                        final int numberOfMessagesAfterShutdown,
                                                                        final boolean finishRemainingTask) {
        sendMessagesBeforeAndAfterShutdownAsynchronously(sutActions, testEnvironment, numberOfMessagesBeforeShutdown,
                numberOfMessagesAfterShutdown, finishRemainingTask);
    }

    public static void sendSeveralMessagesAsynchronouslyBeforeTheObjectIsShutdown(final ChannelMessageBusSutActions sutActions,
                                                                                  final TestEnvironment testEnvironment,
                                                                                  final int numberOfSenders, final int numberOfMessages) {
        sendMessagesBeforeShutdownAsynchronously(sutActions, testEnvironment, numberOfSenders, numberOfMessages);
    }

    public static void shutdownTheSut(final ChannelMessageBusSutActions sutActions) {
        sutActions.close(true);
    }


    public static void shutdownTheObjectAsynchronouslyXTimes(final ChannelMessageBusSutActions sutActions,
                                                             final int numberOfThreads) {
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> sutActions.close(false));
        }
    }

    public static void awaitTheShutdownTimeoutInSeconds(final ChannelMessageBusSutActions sutActions,
                                                        final TestEnvironment testEnvironment,
                                                        final int timeoutInSeconds) {
        try {
            final boolean terminatedSuccessful = sutActions.awaitTermination(timeoutInSeconds, SECONDS);
            testEnvironment.setProperty(RESULT, terminatedSuccessful);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void callAwaitWithoutACloseIsCalled(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        try {
            final boolean terminatedSuccessful = sutActions.awaitTermination(0, SECONDS);
            testEnvironment.setProperty(RESULT, terminatedSuccessful);
            testEnvironment.setProperty(EXPECTED_RESULT, false);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void unsubscribeASubscriberXTimes(final ChannelMessageBusSutActions sutActions,
                                                    final TestEnvironment testEnvironment,
                                                    final int numberOfUnsubscriptions) {
        final List<Subscriber<?>> currentSubscriber;
        if (testEnvironment.has(EXPECTED_SUBSCRIBER)) {
            currentSubscriber = (List<Subscriber<?>>) testEnvironment.getProperty(EXPECTED_SUBSCRIBER);
        } else {
            currentSubscriber = (List<Subscriber<?>>) testEnvironment.getProperty(INITIAL_SUBSCRIBER);
        }
        final Subscriber<?> firstSubscriber = currentSubscriber.get(0);
        final SubscriptionId subscriptionId = firstSubscriber.getSubscriptionId();
        for (int i = 0; i < numberOfUnsubscriptions; i++) {
            sutActions.unsubscribe(subscriptionId);
        }
        final List<Subscriber<?>> remainingSubscriber = currentSubscriber.subList(1, currentSubscriber.size());
        testEnvironment.setProperty(EXPECTED_SUBSCRIBER, remainingSubscriber);
    }

    public static void performAShortWait(final long timeout, final TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void queryTheNumberOfAcceptedMessages(final ChannelMessageBusSutActions sutActions,
                                                        final TestEnvironment testEnvironment) {
        queryMessageStatistics(sutActions, testEnvironment, MessageStatistics::getAcceptedMessages);
    }

    public static void queryTheNumberOfAcceptedMessagesAsynchronously(final ChannelMessageBusSutActions sutActions,
                                                                      final TestEnvironment testEnvironment) {
        final Semaphore semaphore = new Semaphore(0);
        new Thread(() -> {
            queryMessageStatistics(sutActions, testEnvironment, MessageStatistics::getAcceptedMessages);
            semaphore.release();
        }).start();
        try {
            semaphore.acquire();
        } catch (final InterruptedException e) {
            //not necessary to do anything here
        }
    }

    public static void queryTheNumberOfWaitingMessages(final ChannelMessageBusSutActions sutActions,
                                                       final TestEnvironment testEnvironment) {
        queryMessageStatistics(sutActions, testEnvironment, MessageStatistics::getWaitingMessages);
    }

    public static void queryTheNumberOfSuccessfulDeliveredMessages(final ChannelMessageBusSutActions sutActions,
                                                                   final TestEnvironment testEnvironment) {
        queryMessageStatistics(sutActions, testEnvironment, MessageStatistics::getSuccessfulMessages);
    }

    public static void queryTheNumberOfFailedDeliveredMessages(final ChannelMessageBusSutActions sutActions,
                                                               final TestEnvironment testEnvironment) {
        queryMessageStatistics(sutActions, testEnvironment, MessageStatistics::getFailedMessages);
    }

    public static void queryTheNumberOfDroppedMessages(final ChannelMessageBusSutActions sutActions,
                                                       final TestEnvironment testEnvironment) {
        queryMessageStatistics(sutActions, testEnvironment, MessageStatistics::getDroppedMessages);
    }

    public static void queryTheNumberOfReplacedMessages(final ChannelMessageBusSutActions sutActions,
                                                        final TestEnvironment testEnvironment) {
        queryMessageStatistics(sutActions, testEnvironment, MessageStatistics::getReplacedMessages);
    }

    public static void queryTheNumberOfForgottenMessages(final ChannelMessageBusSutActions sutActions,
                                                         final TestEnvironment testEnvironment) {
        queryMessageStatistics(sutActions, testEnvironment, MessageStatistics::getForgottenMessages);
    }

    public static void queryTheNumberOfCurrentlyDeliveredMessages(final ChannelMessageBusSutActions sutActions,
                                                                  final TestEnvironment testEnvironment) {
        queryMessageStatistics(sutActions, testEnvironment, MessageStatistics::getCurrentlyDeliveredMessages);
    }

    public static void queryTheNumberOfCurrentlyTransportedMessages(final ChannelMessageBusSutActions sutActions,
                                                                    final TestEnvironment testEnvironment) {
        queryMessageStatistics(sutActions, testEnvironment, MessageStatistics::getCurrentlyTransportedMessages);
    }

    public static void queryTheTimestampOfTheMessageStatistics(final ChannelMessageBusSutActions sutActions,
                                                               final TestEnvironment testEnvironment) {
        final MessageStatistics messageStatistics = sutActions.getMessageStatistics();
        final Date timestamp = messageStatistics.getTimestamp();
        testEnvironment.setProperty(RESULT, timestamp);
    }

    private static void queryMessageStatistics(final ChannelMessageBusSutActions sutActions,
                                               final TestEnvironment testEnvironment,
                                               final MessageStatisticsQuery query) {
        final MessageStatistics messageStatistics = sutActions.getMessageStatistics();
        final BigInteger statistic = query.query(messageStatistics);
        final long longValueExact = statistic.longValueExact();
        testEnvironment.setProperty(RESULT, longValueExact);
    }

    public static void queryTheListOfFilters(final ChannelMessageBusSutActions sutActions,
                                             final TestEnvironment testEnvironment) {
        final List<?> filter = sutActions.getFilter();
        testEnvironment.setProperty(RESULT, filter);
    }

    public static void removeAFilter(final ChannelMessageBusSutActions sutActions,
                                     final TestEnvironment testEnvironment) {
        final Object removedFilter = sutActions.removeAFilter();
        final List<?> expectedFilter = testEnvironment.getPropertyAsType(EXPECTED_FILTER, List.class);
        expectedFilter.remove(removedFilter);
    }

    private interface MessageStatisticsQuery {
        BigInteger query(MessageStatistics messageStatistics);
    }
}
