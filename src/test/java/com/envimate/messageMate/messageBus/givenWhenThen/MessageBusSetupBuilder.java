package com.envimate.messageMate.messageBus.givenWhenThen;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.messageBus.config.MessageBusTestConfig;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSharedSetupBuilder;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.Setup;
import com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestActions.messageBusTestActions;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusSetupBuilder extends PipeMessageBusSharedSetupBuilder<MessageBus> {

    private final MessageBusBuilder messageBusBuilder = MessageBusBuilder.aMessageBus();


    public static MessageBusSetupBuilder aConfiguredMessageBus(final MessageBusTestConfig testConfig) {
        return new MessageBusSetupBuilder()
                .configuredWith(testConfig);
    }

    private MessageBusSetupBuilder configuredWith(final MessageBusTestConfig testConfig) {
        messageBusBuilder.withConfiguration(testConfig.messageBusConfiguration)
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

    @Override
    public Setup<MessageBus> build() {
        final MessageBus messageBus = messageBusBuilder.build();
        return Setup.setup(messageBus, testEnvironment, setupActions);
    }

    @Override
    protected PipeMessageBusSutActions sutActions(final MessageBus messageBus) {
        return messageBusTestActions(messageBus);
    }
}
