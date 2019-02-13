package com.envimate.messageMate.channel.config;

import com.envimate.messageMate.configuration.ChannelConfiguration;
import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyFactory;
import com.envimate.messageMate.internal.delivering.DeliveryStrategyFactory;
import com.envimate.messageMate.internal.delivering.DeliveryType;
import com.envimate.messageMate.internal.statistics.StatisticsCollector;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyAbstractFactory.aMessageAcceptingStrategyFactory;
import static com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyType.QUEUED;
import static com.envimate.messageMate.internal.delivering.AbstractDeliveryStrategyFactory.deliveryStrategyForType;
import static com.envimate.messageMate.internal.statistics.StatisticsCollectorFactory.aStatisticsCollector;


@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChannelTestConfig {

    public static final int ASYNCHRONOUS_DELIVERY_POOL_SIZE = 3;
    public final String description;
    public final ChannelConfiguration channelConfiguration;
    public final DeliveryStrategyFactory<TestMessage> deliveryStrategyFactory;
    public final MessageAcceptingStrategyFactory<TestMessage> messageAcceptingStrategyFactory;
    public final StatisticsCollector statisticsCollector;

    static ChannelTestConfig aSynchronousChannel() {
        final ChannelConfiguration configuration = ChannelConfiguration.defaultConfiguration();
        configuration.setDeliveryType(DeliveryType.SYNCHRONOUS);
        final StatisticsCollector statisticsCollector = aStatisticsCollector();
        final DeliveryStrategyFactory<TestMessage> deliveryStrategyFactory = deliveryStrategyForType(configuration);
        final MessageAcceptingStrategyFactory<TestMessage> acceptingStrategyFactory = aMessageAcceptingStrategyFactory(configuration.getMessageAcceptingStrategyType());
        return new ChannelTestConfig("aSynchronousChannel", configuration, deliveryStrategyFactory, acceptingStrategyFactory, statisticsCollector);
    }

    static ChannelTestConfig aSynchronousChannelWithAsyncDelivery() {
        final ChannelConfiguration configuration = ChannelConfiguration.defaultConfiguration();
        configuration.setDeliveryType(DeliveryType.ASYNCHRONOUS);
        configuration.setThreadPoolWorkingQueue(new LinkedBlockingQueue<>());
        configuration.setTimeoutTimeUnit(TimeUnit.MILLISECONDS);
        configuration.setMaximumTimeout(10);
        configuration.setCorePoolSize(ASYNCHRONOUS_DELIVERY_POOL_SIZE);
        configuration.setMaximumPoolSize(ASYNCHRONOUS_DELIVERY_POOL_SIZE);
        final StatisticsCollector statisticsCollector = aStatisticsCollector();
        final DeliveryStrategyFactory<TestMessage> deliveryStrategyFactory = deliveryStrategyForType(configuration);
        final MessageAcceptingStrategyFactory<TestMessage> acceptingStrategyFactory = aMessageAcceptingStrategyFactory(configuration.getMessageAcceptingStrategyType());
        return new ChannelTestConfig("aSynchronousChannelWithAsyncDelivery", configuration, deliveryStrategyFactory, acceptingStrategyFactory, statisticsCollector);
    }

    static ChannelTestConfig aSynchronousChannelWithQueuedAcceptingStrategy() {
        final ChannelConfiguration configuration = ChannelConfiguration.defaultConfiguration();
        configuration.setDeliveryType(DeliveryType.SYNCHRONOUS);
        final StatisticsCollector statisticsCollector = aStatisticsCollector();
        final DeliveryStrategyFactory<TestMessage> deliveryStrategyFactory = deliveryStrategyForType(configuration);
        final MessageAcceptingStrategyFactory<TestMessage> acceptingStrategyFactory = aMessageAcceptingStrategyFactory(QUEUED);
        return new ChannelTestConfig("aSynchronousChannel", configuration, deliveryStrategyFactory, acceptingStrategyFactory, statisticsCollector);
    }

    @Override
    public String toString() {
        return description;
    }
}
