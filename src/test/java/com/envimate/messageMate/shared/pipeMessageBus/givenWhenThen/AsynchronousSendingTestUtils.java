package com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen;

import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.testMessages.InvalidTestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusTestProperties.*;
import static com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber.blockingTestSubscriber;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class AsynchronousSendingTestUtils {

    public static void sendValidMessagesAsynchronously(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment,
                                                       final int numberOfSender, final int numberOfMessagesPerSender, final boolean expectCleanShutdown) {
        sendXMessagesAsynchronously(numberOfSender, TestMessageFactory.testMessageFactoryForValidMessages(numberOfMessagesPerSender, testEnvironment),
                sutActions::send, testEnvironment, expectCleanShutdown);
    }

    public static void sendValidMessagesAsynchronously(final Consumer<TestMessage> sutSend, final TestEnvironment testEnvironment,
                                                       final int numberOfSender, final int numberOfMessagesPerSender, final boolean expectCleanShutdown) {
        sendXMessagesAsynchronously(numberOfSender, TestMessageFactory.testMessageFactoryForValidMessages(numberOfMessagesPerSender, testEnvironment),
                sutSend, testEnvironment, expectCleanShutdown);
    }

    public static void sendInvalidMessagesAsynchronously(final PipeMessageBusSutActions sutActions,
                                                         final TestEnvironment testEnvironment,
                                                         final int numberOfSender, final int numberOfMessagesPerSender) {
        sendXMessagesAsynchronously(numberOfSender, TestMessageFactory.testMessageFactoryForInvalidMessages(numberOfMessagesPerSender),
                sutActions::send, testEnvironment, true);
    }

    public static void sendMixtureOfValidAndInvalidMessagesAsynchronously(final PipeMessageBusSutActions sutActions,
                                                                          final TestEnvironment testEnvironment,
                                                                          final int numberOfSender, final int numberOfMessagesPerSender) {
        sendXMessagesAsynchronously(numberOfSender, TestMessageFactory.testMessageFactoryForRandomValidOrInvalidTestMessages(numberOfMessagesPerSender, testEnvironment),
                sutActions::send, testEnvironment, true);
    }

    public static void sendMessagesBeforeAndAfterShutdownAsynchronously(final PipeMessageBusSutActions sutActions,
                                                                        final TestEnvironment testEnvironment,
                                                                        final int numberOfMessagesBeforeShutdown,
                                                                        final int numberOfMessagesAfterShutdown,
                                                                        final boolean finishRemainingTask) {
        final Semaphore semaphore = new Semaphore(0);
        final TestSubscriber<TestMessageOfInterest> subscriber = blockingTestSubscriber(semaphore);
        sutActions.subscribe(TestMessageOfInterest.class, subscriber);
        testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);

        final TestMessageFactory messageFactory = TestMessageFactory.testMessageFactoryForValidMessages(1, testEnvironment);
        sendXMessagesAsynchronously(numberOfMessagesBeforeShutdown, messageFactory, sutActions::send, testEnvironment, false);
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

    public static void sendMessagesBeforeShutdownAsynchronously(final PipeMessageBusSutActions sutActions,
                                                                final TestEnvironment testEnvironment,
                                                                final int numberOfSenders, final int numberOfMessages) {
        sendMessagesBeforeShutdownAsynchronously(sutActions::subscribe, sutActions::send, sutActions::close, testEnvironment, numberOfSenders, numberOfMessages);
    }

    public static void sendMessagesBeforeShutdownAsynchronously(final BiConsumer<Class<TestMessageOfInterest>, Subscriber<TestMessageOfInterest>> subscriberConsumer,
                                                                final Consumer<TestMessage> sendConsumer,
                                                                final Consumer<Boolean> closeConsumer,
                                                                final TestEnvironment testEnvironment,
                                                                final int numberOfSenders, final int numberOfMessages) {
        final Semaphore semaphore = new Semaphore(0);
        testEnvironment.setProperty(EXECUTION_END_SEMAPHORE, semaphore);
        final BlockingTestSubscriber<TestMessageOfInterest> subscriber = blockingTestSubscriber(semaphore);
        subscriberConsumer.accept(TestMessageOfInterest.class, subscriber);
        testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);

        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfSenders);
        for (int i = 0; i < numberOfSenders; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < numberOfMessages; j++) {
                    final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
                    testEnvironment.addToListProperty(MESSAGES_SEND, message);
                    sendConsumer.accept(message);
                }
            });
        }
        try {
            MILLISECONDS.sleep(10);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
        closeConsumer.accept(false);
        semaphore.release(1337);
    }

    private static void sendXMessagesAsynchronously(final int numberOfSender, final MessageFactory messageFactory,
                                                    final Consumer<TestMessage> sutSend,
                                                    final TestEnvironment testEnvironment, final boolean expectCleanShutdown) {
        if (numberOfSender <= 0) {
            return;
        }
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
                    sutSend.accept(message);
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
