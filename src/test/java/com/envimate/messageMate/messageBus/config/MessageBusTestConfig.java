package com.envimate.messageMate.messageBus.config;

import com.envimate.messageMate.configuration.MessageBusConfiguration;
import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyFactory;
import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyType;
import com.envimate.messageMate.internal.brokering.BrokerStrategy;
import com.envimate.messageMate.internal.brokering.BrokerStrategyFactory;
import com.envimate.messageMate.internal.delivering.DeliveryStrategyFactory;
import com.envimate.messageMate.internal.delivering.DeliveryType;
import com.envimate.messageMate.internal.statistics.StatisticsCollector;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyAbstractFactory.aMessageAcceptingStrategyFactory;
import static com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyType.QUEUED;
import static com.envimate.messageMate.internal.brokering.BrokerStrategyType.DELIVERY_TO_SAME_CLASS_AS_MESSAGE;
import static com.envimate.messageMate.internal.delivering.AbstractDeliveryStrategyFactory.deliveryStrategyForType;
import static com.envimate.messageMate.internal.delivering.DeliveryType.SYNCHRONOUS;
import static com.envimate.messageMate.internal.statistics.StatisticsCollectorFactory.aStatisticsCollector;


@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageBusTestConfig {

    public static final int ASYNCHRONOUS_DELIVERY_POOL_SIZE = 3;
    public final String description;
    public final MessageBusConfiguration messageBusConfiguration;
    public final DeliveryStrategyFactory<Object> deliveryStrategyFactory;
    public final BrokerStrategy brokerStrategy;
    public final MessageAcceptingStrategyFactory<Object> messageAcceptingStrategyFactory;
    public final StatisticsCollector statisticsCollector;

    static MessageBusTestConfig aSynchronousMessageBus() {
        final MessageBusConfiguration configuration = MessageBusConfiguration.defaultConfiguration();
        configuration.setDeliveryType(SYNCHRONOUS);
        final StatisticsCollector statisticsCollector = aStatisticsCollector();
        final DeliveryStrategyFactory<Object> deliveryStrategyFactory = deliveryStrategyForType(configuration);
        final BrokerStrategy brokerStrategy = BrokerStrategyFactory.aBrokerStrategyForSpecificType(DELIVERY_TO_SAME_CLASS_AS_MESSAGE);
        final MessageAcceptingStrategyType messageAcceptingStrategyType = configuration.getMessageAcceptingStrategyType();
        final MessageAcceptingStrategyFactory<Object> acceptingStrategyFactory = aMessageAcceptingStrategyFactory(messageAcceptingStrategyType);
        return new MessageBusTestConfig("aSynchronousMessageBus", configuration, deliveryStrategyFactory, brokerStrategy, acceptingStrategyFactory, statisticsCollector);
    }

    static MessageBusTestConfig aSynchronousMessageBusWithAsyncDelivery() {
        final MessageBusConfiguration configuration = MessageBusConfiguration.defaultConfiguration();
        configuration.setDeliveryType(DeliveryType.ASYNCHRONOUS);
        configuration.setThreadPoolWorkingQueue(new LinkedBlockingQueue<>());
        configuration.setTimeoutTimeUnit(TimeUnit.MILLISECONDS);
        configuration.setMaximumTimeout(10);
        configuration.setCorePoolSize(ASYNCHRONOUS_DELIVERY_POOL_SIZE);
        configuration.setMaximumPoolSize(ASYNCHRONOUS_DELIVERY_POOL_SIZE);
        final StatisticsCollector statisticsCollector = aStatisticsCollector();
        final DeliveryStrategyFactory<Object> deliveryStrategyFactory = deliveryStrategyForType(configuration);
        final BrokerStrategy brokerStrategy = BrokerStrategyFactory.aBrokerStrategyForSpecificType(DELIVERY_TO_SAME_CLASS_AS_MESSAGE);
        final MessageAcceptingStrategyType messageAcceptingStrategyType = configuration.getMessageAcceptingStrategyType();
        final MessageAcceptingStrategyFactory<Object> acceptingStrategyFactory = aMessageAcceptingStrategyFactory(messageAcceptingStrategyType);
        return new MessageBusTestConfig("aSynchronousMessageBusWithAsyncDelivery", configuration, deliveryStrategyFactory, brokerStrategy, acceptingStrategyFactory, statisticsCollector);
    }

    static MessageBusTestConfig aQueuingAcceptingSynchronousMessageBus() {
        final MessageBusConfiguration configuration = MessageBusConfiguration.defaultConfiguration();
        configuration.setDeliveryType(SYNCHRONOUS);
        configuration.setMessageAcceptingStrategyType(QUEUED);
        final StatisticsCollector statisticsCollector = aStatisticsCollector();
        final DeliveryStrategyFactory<Object> deliveryStrategyFactory = deliveryStrategyForType(configuration);
        final BrokerStrategy brokerStrategy = BrokerStrategyFactory.aBrokerStrategyForSpecificType(DELIVERY_TO_SAME_CLASS_AS_MESSAGE);
        final MessageAcceptingStrategyType messageAcceptingStrategyType = configuration.getMessageAcceptingStrategyType();
        final MessageAcceptingStrategyFactory<Object> acceptingStrategyFactory = aMessageAcceptingStrategyFactory(messageAcceptingStrategyType);
        return new MessageBusTestConfig("aSynchronousMessageBus", configuration, deliveryStrategyFactory, brokerStrategy, acceptingStrategyFactory, statisticsCollector);
    }

    @Override
    public String toString() {
        return description;
    }
}
