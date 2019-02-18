package com.envimate.messageMate.pipe.config;

import com.envimate.messageMate.configuration.PipeConfiguration;
import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyFactory;
import com.envimate.messageMate.internal.delivering.DeliveryStrategyFactory;
import com.envimate.messageMate.internal.delivering.DeliveryType;
import com.envimate.messageMate.internal.statistics.StatisticsCollector;
import com.envimate.messageMate.internal.transport.MessageTransportConfiguration;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyAbstractFactory.aMessageAcceptingStrategyFactory;
import static com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyType.ATOMIC;
import static com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyType.QUEUED;
import static com.envimate.messageMate.internal.delivering.AbstractDeliveryStrategyFactory.deliveryStrategyForType;
import static com.envimate.messageMate.internal.statistics.StatisticsCollectorFactory.aStatisticsCollector;
import static com.envimate.messageMate.internal.transport.MessageTransportConfiguration.messageTransportConfiguration;
import static com.envimate.messageMate.internal.transport.MessageTransportType.POOLED;
import static com.envimate.messageMate.internal.transport.MessageTransportType.SYNCHRONOUS;


@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChannelTestConfig {

    public static final int ASYNCHRONOUS_DELIVERY_POOL_SIZE = 3;
    public final String description;
    public final PipeConfiguration pipeConfiguration;
    public final MessageAcceptingStrategyFactory<TestMessage> messageAcceptingStrategyFactory;
    public final DeliveryStrategyFactory<TestMessage> deliveryStrategyFactory;
    public final StatisticsCollector statisticsCollector;

    static ChannelTestConfig aSynchronousChannel() {
        final PipeConfiguration configuration = PipeConfiguration.defaultConfiguration();
        configuration.setDeliveryType(DeliveryType.SYNCHRONOUS);
        final MessageTransportConfiguration messageTransportConfiguration = messageTransportConfiguration(SYNCHRONOUS, 0);
        configuration.setMessageTransportConfiguration(messageTransportConfiguration);
        final StatisticsCollector statisticsCollector = aStatisticsCollector();
        final DeliveryStrategyFactory<TestMessage> deliveryStrategyFactory = deliveryStrategyForType(configuration);
        final MessageAcceptingStrategyFactory<TestMessage> acceptingStrategyFactory = aMessageAcceptingStrategyFactory(configuration.getMessageAcceptingStrategyType());
        return new ChannelTestConfig("aSynchronousChannel", configuration, acceptingStrategyFactory, deliveryStrategyFactory, statisticsCollector);
    }

    static ChannelTestConfig aSynchronousChannelWithAsyncDelivery() {
        final PipeConfiguration configuration = PipeConfiguration.defaultConfiguration();
        configuration.setDeliveryType(DeliveryType.ASYNCHRONOUS);
        configuration.setThreadPoolWorkingQueue(new LinkedBlockingQueue<>());
        configuration.setTimeoutTimeUnit(TimeUnit.MILLISECONDS);
        configuration.setMaximumTimeout(10);
        configuration.setCorePoolSize(ASYNCHRONOUS_DELIVERY_POOL_SIZE);
        configuration.setMaximumPoolSize(ASYNCHRONOUS_DELIVERY_POOL_SIZE);
        final MessageTransportConfiguration messageTransportConfiguration = messageTransportConfiguration(SYNCHRONOUS, 0);
        configuration.setMessageTransportConfiguration(messageTransportConfiguration);
        final StatisticsCollector statisticsCollector = aStatisticsCollector();
        final DeliveryStrategyFactory<TestMessage> deliveryStrategyFactory = deliveryStrategyForType(configuration);
        final MessageAcceptingStrategyFactory<TestMessage> acceptingStrategyFactory = aMessageAcceptingStrategyFactory(configuration.getMessageAcceptingStrategyType());
        return new ChannelTestConfig("aSynchronousChannelWithAsyncDelivery", configuration, acceptingStrategyFactory, deliveryStrategyFactory, statisticsCollector);
    }

    static ChannelTestConfig aSynchronousChannelWithQueuedAcceptingStrategy() {
        final PipeConfiguration configuration = PipeConfiguration.defaultConfiguration();
        configuration.setDeliveryType(DeliveryType.SYNCHRONOUS);
        final MessageTransportConfiguration messageTransportConfiguration = messageTransportConfiguration(SYNCHRONOUS, 0);
        configuration.setMessageTransportConfiguration(messageTransportConfiguration);
        final StatisticsCollector statisticsCollector = aStatisticsCollector();
        final DeliveryStrategyFactory<TestMessage> deliveryStrategyFactory = deliveryStrategyForType(configuration);
        final MessageAcceptingStrategyFactory<TestMessage> acceptingStrategyFactory = aMessageAcceptingStrategyFactory(QUEUED);
        return new ChannelTestConfig("aSynchronousChannel", configuration, acceptingStrategyFactory, deliveryStrategyFactory, statisticsCollector);
    }

    static ChannelTestConfig aConfigurationForAtomicAcceptingStrategyPooledTransportAndSynchronousDelivery() {
        final PipeConfiguration configuration = PipeConfiguration.defaultConfiguration();
        configuration.setDeliveryType(DeliveryType.SYNCHRONOUS);
        final MessageTransportConfiguration messageTransportConfiguration = messageTransportConfiguration(POOLED, 8);
        configuration.setMessageTransportConfiguration(messageTransportConfiguration);
        final StatisticsCollector statisticsCollector = aStatisticsCollector();
        final DeliveryStrategyFactory<TestMessage> deliveryStrategyFactory = deliveryStrategyForType(configuration);
        final MessageAcceptingStrategyFactory<TestMessage> acceptingStrategyFactory = aMessageAcceptingStrategyFactory(ATOMIC);
        return new ChannelTestConfig("aSynchronousChannel", configuration, acceptingStrategyFactory, deliveryStrategyFactory, statisticsCollector);
    }

    @Override
    public String toString() {
        return description;
    }
}
