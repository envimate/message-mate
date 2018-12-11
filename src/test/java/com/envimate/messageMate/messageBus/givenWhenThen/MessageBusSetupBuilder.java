package com.envimate.messageMate.messageBus.givenWhenThen;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.messageBus.config.MessageBusTestConfig;
import com.envimate.messageMate.shared.context.TestExecutionProperty;
import com.envimate.messageMate.shared.givenWhenThen.Setup;
import com.envimate.messageMate.shared.givenWhenThen.SetupBuilder;
import com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.shared.context.TestExecutionProperty.*;
import static com.envimate.messageMate.shared.givenWhenThen.TestFilter.*;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusSetupBuilder extends SetupBuilder<MessageBus> {

    private final MessageBusBuilder messageBusBuilder = MessageBusBuilder.aMessageBus();

    public static MessageBusSetupBuilder aMessageBus() {
        return new MessageBusSetupBuilder();
    }

    @Override
    public <R> void subscribe(final MessageBus messageBus, final Class<R> rClass, final Subscriber<R> subscriber) {
        messageBus.subscribe(rClass, subscriber);
    }

    @Override
    protected void addFilterThatChangesTheContent(final MessageBus messageBus) {
        final Filter<Object> filter = aContentChangingFilter(Object.class);
        messageBus.add(filter);
    }

    @Override
    protected void addFilterThatDropsMessages(final MessageBus messageBus) {
        final Filter<Object> filter = aMessageDroppingFilter(Object.class);
        messageBus.add(filter);
    }

    @Override
    protected void addFilterThatReplacesWrongMessage(final MessageBus messageBus) {
        final Filter<Object> filter = aMessageReplacingFilter(Object.class);
        messageBus.add(filter);
    }

    @Override
    protected void addFilterThatDoesNotCallAnyFilterMethod(final MessageBus messageBus) {
        final Filter<Object> filter = aMessageThatDoesNotCallAnyMethod(Object.class);
        messageBus.add(filter);
    }

    @Override
    public Setup<MessageBus> build() {
        return Setup.setup(messageBusBuilder.build(), executionContext, setupActions);
    }

    public MessageBusSetupBuilder configuredWith(final MessageBusTestConfig testConfig) {
        messageBusBuilder.withMessageBusConfiguration(testConfig.messageBusConfiguration)
                .withACustomBrokerStrategy(testConfig.brokerStrategy)
                .withACustomDeliveryStrategyFactory(testConfig.deliveryStrategyFactory)
                .withACustomMessageAcceptingStrategyFactory(testConfig.messageAcceptingStrategyFactory)
                .withStatisticsCollector(testConfig.statisticsCollector);
        return this;
    }

    public <T> MessageBusSetupBuilder withASubscriberForTyp(final Class<T> messageClass) {
        setupActions.add((messageBus, executionContext) -> {
            final SimpleTestSubscriber<T> subscriber = SimpleTestSubscriber.testSubscriber();
            messageBus.subscribe(messageClass, subscriber);
            executionContext.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        });
        return this;
    }

}
