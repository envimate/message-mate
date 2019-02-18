package com.envimate.messageMate.pipe.givenWhenThen;


import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.pipe.PipeBuilder;
import com.envimate.messageMate.pipe.config.ChannelTestConfig;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusSharedSetupBuilder;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusSutActions;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.Setup;
import com.envimate.messageMate.shared.testMessages.TestMessage;

import static com.envimate.messageMate.pipe.PipeBuilder.aPipe;
import static com.envimate.messageMate.pipe.givenWhenThen.ChannelTestActions.channelTestActions;

public class ChannelSetupBuilder extends ChannelMessageBusSharedSetupBuilder<Pipe<TestMessage>> {
    private final PipeBuilder<TestMessage> pipeBuilder = aPipe();

    public static ChannelMessageBusSharedSetupBuilder<Pipe<TestMessage>> aConfiguredChannel(final ChannelTestConfig testConfig) {
        return new ChannelSetupBuilder()
                .configuredWith(testConfig);
    }

    private ChannelMessageBusSharedSetupBuilder<Pipe<TestMessage>> configuredWith(final ChannelTestConfig testConfig) {
        pipeBuilder.withConfiguration(testConfig.pipeConfiguration)
                .withACustomDeliveryStrategyFactory(testConfig.deliveryStrategyFactory)
                .withACustomMessageAcceptingStrategyFactory(testConfig.messageAcceptingStrategyFactory)
                .withStatisticsCollector(testConfig.statisticsCollector);
        return this;
    }

    public Setup<Pipe<TestMessage>> build() {
        final Pipe<TestMessage> pipe = pipeBuilder.build();
        return Setup.setup(pipe, testEnvironment, setupActions);
    }

    @Override
    protected ChannelMessageBusSutActions sutActions(final Pipe<TestMessage> pipe) {
        return channelTestActions(pipe);
    }
}
