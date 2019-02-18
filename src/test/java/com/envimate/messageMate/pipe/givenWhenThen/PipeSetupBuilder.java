package com.envimate.messageMate.pipe.givenWhenThen;


import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.pipe.PipeBuilder;
import com.envimate.messageMate.pipe.config.PipeTestConfig;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSharedSetupBuilder;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.Setup;
import com.envimate.messageMate.shared.testMessages.TestMessage;

import static com.envimate.messageMate.pipe.PipeBuilder.aPipe;
import static com.envimate.messageMate.pipe.givenWhenThen.PipeTestActions.pipeTestActions;

public class PipeSetupBuilder extends PipeMessageBusSharedSetupBuilder<Pipe<TestMessage>> {
    private final PipeBuilder<TestMessage> pipeBuilder = aPipe();

    public static PipeMessageBusSharedSetupBuilder<Pipe<TestMessage>> aConfiguredPipe(final PipeTestConfig testConfig) {
        return new PipeSetupBuilder()
                .configuredWith(testConfig);
    }

    private PipeMessageBusSharedSetupBuilder<Pipe<TestMessage>> configuredWith(final PipeTestConfig testConfig) {
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
    protected PipeMessageBusSutActions sutActions(final Pipe<TestMessage> pipe) {
        return pipeTestActions(pipe);
    }
}
