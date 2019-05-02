package com.envimate.messageMate.shared.pipeChannelMessageBus.testActions;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusSutActions implements SendingAndReceivingActions, SubscriberQueryActions {
    private final MessageBus messageBus;

    public static MessageBusSutActions messageBusSutActions(final MessageBus messageBus) {
        return new MessageBusSutActions(messageBus);
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        messageBus.close(finishRemainingTasks);
    }

    @Override
    public boolean await(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return messageBus.awaitTermination(timeout, timeUnit);
    }

    @Override
    public void send(final EventType eventType, final TestMessage message) {
        messageBus.send(eventType, message);
    }

    @Override
    public void subscribe(final EventType eventType, final Subscriber<TestMessage> subscriber) {
        final Subscriber degenerifiedSubscriber = subscriber;
        messageBus.subscribe(eventType, degenerifiedSubscriber);
    }

    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        return messageBus.getStatusInformation().getAllSubscribers();
    }
}
