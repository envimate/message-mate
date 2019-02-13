package com.envimate.messageMate.shared.channelMessageBus.givenWhenThen;

import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.testMessages.InvalidTestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusTestProperties.*;
import static com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber.blockingTestSubscriber;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class AsynchronousSendingTestUtils {

    public static void sendValidMessagesAsynchronously(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment,
                                                       final int numberOfSender, final int numberOfMessagesPerSender, final boolean expectCleanShutdown) {
        sendXMessagesAsynchronously(numberOfSender, TestMessageFactory.testMessageFactoryForValidMessages(numberOfMessagesPerSender, testEnvironment),
                sutActions, testEnvironment, expectCleanShutdown);
    }

    public static void sendInvalidMessagesAsynchronously(final ChannelMessageBusSutActions sutActions,
                                                         final TestEnvironment testEnvironment,
                                                         final int numberOfSender, final int numberOfMessagesPerSender) {
        sendXMessagesAsynchronously(numberOfSender, TestMessageFactory.testMessageFactoryForInvalidMessages(numberOfMessagesPerSender),
                sutActions, testEnvironment, true);
    }

    public static void sendMixtureOfValidAndInvalidMessagesAsynchronously(final ChannelMessageBusSutActions sutActions,
                                                                          final TestEnvironment testEnvironment,
                                                                          final int numberOfSender, final int numberOfMessagesPerSender) {
        sendXMessagesAsynchronously(numberOfSender, TestMessageFactory.testMessageFactoryForRandomValidOrInvalidTestMessages(numberOfMessagesPerSender, testEnvironment),
                sutActions, testEnvironment, true);
    }

    public static void sendMessagesBeforeAndAfterShutdownAsynchronously(final ChannelMessageBusSutActions sutActions,
                                                                        final TestEnvironment testEnvironment,
                                                                        final int numberOfMessagesBeforeShutdown,
                                                                        final int numberOfMessagesAfterShutdown,
                                                                        final boolean finishRemainingTask) {
        final Semaphore semaphore = new Semaphore(0);
        final TestSubscriber<TestMessageOfInterest> subscriber = blockingTestSubscriber(semaphore);
        sutActions.subscribe(TestMessageOfInterest.class, subscriber);
        testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);

        final TestMessageFactory messageFactory = TestMessageFactory.testMessageFactoryForValidMessages(1, testEnvironment);
        sendXMessagesAsynchronously(numberOfMessagesBeforeShutdown, messageFactory, sutActions, testEnvironment, false);
        try {
            MILLISECONDS.sleep(100);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
        sutActions.close(finishRemainingTask);
        semaphore.release(1000);
        for (int i = 0; i < numberOfMessagesAfterShutdown; i++) {
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            testEnvironment.addToListProperty(MESSAGES_SEND, message);
            sutActions.send(message);
        }
    }

    public static void sendMessagesBeforeShutdownAsynchronously(final ChannelMessageBusSutActions sutActions,
                                                                final TestEnvironment testEnvironment,
                                                                final int numberOfSenders, final int numberOfMessages) {
        final Semaphore semaphore = new Semaphore(0);
        testEnvironment.setProperty(EXECUTION_END_SEMAPHORE, semaphore);
        final BlockingTestSubscriber<TestMessageOfInterest> subscriber = blockingTestSubscriber(semaphore);
        sutActions.subscribe(TestMessageOfInterest.class, subscriber);
        testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);

        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfSenders);
        for (int i = 0; i < numberOfSenders; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < numberOfMessages; j++) {
                    final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
                    testEnvironment.addToListProperty(MESSAGES_SEND, message);
                    sutActions.send(message);
                }
            });
        }
        try {
            MILLISECONDS.sleep(10);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
        sutActions.close(false);
    }

    private static void sendXMessagesAsynchronously(final int numberOfSender, final MessageFactory messageFactory,
                                                    final ChannelMessageBusSutActions sutActions,
                                                    final TestEnvironment testEnvironment, final boolean expectCleanShutdown) {
        final CyclicBarrier sendingStartBarrier = new CyclicBarrier(numberOfSender);
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfSender);
        for (int i = 0; i < numberOfSender; i++) {
            executorService.execute(() -> {
                final List<TestMessage> messagesToSend = new ArrayList<>();

                for (int j = 0; j < messageFactory.numberOfMessages(); j++) {
                    final TestMessage message = messageFactory.createMessage();
                    messagesToSend.add(message);
                    testEnvironment.addToListProperty(MESSAGES_SEND, message);
                }
                try {
                    sendingStartBarrier.await(3, SECONDS);
                } catch (final InterruptedException | BrokenBarrierException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
                for (final TestMessage message : messagesToSend) {
                    sutActions.send(message);
                }
            });
        }
        executorService.shutdown();
        if (expectCleanShutdown) {
            try {
                final boolean isTerminated = executorService.awaitTermination(3, SECONDS);
                if (!isTerminated) {
                    throw new RuntimeException("ExecutorService did not shutdown within timeout.");
                }
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private interface MessageFactory {
        TestMessage createMessage();

        int numberOfMessages();
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static final class TestMessageFactory implements MessageFactory {
        private final Supplier<TestMessage> messageSupplier;
        private final int numberOfMessages;

        public static TestMessageFactory testMessageFactoryForValidMessages(final int numberOfMessages, final TestEnvironment testEnvironment) {
            return new TestMessageFactory(() -> {
                final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
                testEnvironment.addToListProperty(MESSAGES_SEND_OF_INTEREST, message);
                return message;
            }, numberOfMessages);
        }

        public static TestMessageFactory testMessageFactoryForInvalidMessages(final int numberOfMessages) {
            return new TestMessageFactory(InvalidTestMessage::invalidTestMessage, numberOfMessages);
        }

        public static TestMessageFactory testMessageFactoryForRandomValidOrInvalidTestMessages(final int numberOfMessages, final TestEnvironment testEnvironment) {
            return new TestMessageFactory(() -> {
                if (Math.random() < 0.5) {
                    final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
                    testEnvironment.addToListProperty(MESSAGES_SEND_OF_INTEREST, message);
                    return message;
                } else {
                    return InvalidTestMessage.invalidTestMessage();
                }
            }, numberOfMessages);
        }


        @Override
        public TestMessage createMessage() {
            return messageSupplier.get();
        }

        @Override
        public int numberOfMessages() {
            return numberOfMessages;
        }
    }
}
