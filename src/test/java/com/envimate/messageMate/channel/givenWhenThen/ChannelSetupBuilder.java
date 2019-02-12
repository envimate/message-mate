package com.envimate.messageMate.channel.givenWhenThen;


import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ChannelBuilder;
import com.envimate.messageMate.channel.config.ChannelTestConfig;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusSharedSetupBuilder;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusSutActions;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.Setup;
import com.envimate.messageMate.shared.testMessages.TestMessage;

import static com.envimate.messageMate.channel.ChannelBuilder.aChannel;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestActions.channelTestActions;

public class ChannelSetupBuilder extends ChannelMessageBusSharedSetupBuilder<Channel<TestMessage>> {
    private final ChannelBuilder<TestMessage> channelBuilder = aChannel();

    public static ChannelMessageBusSharedSetupBuilder<Channel<TestMessage>> aConfiguredChannel(final ChannelTestConfig testConfig) {
        return new ChannelSetupBuilder()
                .configuredWith(testConfig);
    }

    private ChannelMessageBusSharedSetupBuilder<Channel<TestMessage>> configuredWith(final ChannelTestConfig testConfig) {
        channelBuilder.withConfiguration(testConfig.channelConfiguration)
                .withACustomDeliveryStrategyFactory(testConfig.deliveryStrategyFactory)
                .withACustomMessageAcceptingStrategyFactory(testConfig.messageAcceptingStrategyFactory)
                .withStatisticsCollector(testConfig.statisticsCollector);
        return this;
    }

    public Setup<Channel<TestMessage>> build() {
        final Channel<TestMessage> channel = channelBuilder.build();
        return Setup.setup(channel, testEnvironment, setupActions);
    }

    @Override
    protected ChannelMessageBusSutActions sutActions(final Channel<TestMessage> channel) {
        return channelTestActions(channel);
    }
}
