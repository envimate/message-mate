package com.envimate.messageMate.channel.givenWhenThen;


import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ChannelBuilder;
import com.envimate.messageMate.channel.config.ChannelTestConfig;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.shared.givenWhenThen.Setup;
import com.envimate.messageMate.shared.givenWhenThen.SetupBuilder;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;

import static com.envimate.messageMate.shared.givenWhenThen.TestFilter.*;

public class ChannelSetupBuilder extends SetupBuilder<Channel<TestMessage>> {

    private final ChannelBuilder<TestMessage> channelBuilder = ChannelBuilder.aChannel();

    public static ChannelSetupBuilder aChannel() {
        return new ChannelSetupBuilder();
    }

    public Setup<Channel<TestMessage>> build() {
        final Channel<TestMessage> channel = channelBuilder.build();
        return Setup.setup(channel, executionContext, setupActions);
    }

    public ChannelSetupBuilder configuredWith(final ChannelTestConfig testConfig) {
        channelBuilder.withConfiguration(testConfig.channelConfiguration)
                .withACustomDeliveryStrategyFactory(testConfig.deliveryStrategyFactory)
                .withACustomMessageAcceptingStrategyFactory(testConfig.messageAcceptingStrategyFactory)
                .withStatisticsCollector(testConfig.statisticsCollector);
        return this;
    }

    @Override
    protected void addFilterThatChangesTheContent(final Channel<TestMessage> channel) {
        final Filter<TestMessage> filter = aContentChangingFilter();
        channel.add(filter);
    }

    @Override
    protected void addFilterThatDropsMessages(final Channel<TestMessage> channel) {
        final Filter<TestMessage> filter = aMessageDroppingFilter();
        channel.add(filter);
    }

    @Override
    protected void addFilterThatReplacesWrongMessage(final Channel<TestMessage> channel) {
        final Filter<TestMessage> filter = aMessageReplacingFilter();
        channel.add(filter);
    }

    @Override
    protected void addFilterThatDoesNotCallAnyFilterMethod(final Channel<TestMessage> channel) {
        final Filter<TestMessage> filter = aMessageFilterThatDoesNotCallAnyMethod();
        channel.add(filter);
    }

    @Override
    protected Filter<?> addFilterAtPositionThatAppendsTheContent(final String contentToAppend, final int position, final Channel<TestMessage> channel) {
        final Filter<TestMessage> filter = aContentAppendingFilter(contentToAppend);
        channel.add(filter, position);
        return filter;
    }

    @Override
    public <R> void subscribe(final Channel<TestMessage> channel, final Class<R> rClass, final Subscriber<R> subscriber) {
        @SuppressWarnings("unchecked")
        final Subscriber<TestMessage> castedSubscriber = (Subscriber<TestMessage>) subscriber;
        channel.subscribe(castedSubscriber);
    }
}
