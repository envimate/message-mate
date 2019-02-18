package com.envimate.messageMate.channel;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class When {
    private final ChannelSetupBuilder channelSetupBuilder;

    public Then when(final ChannelActionBuilder channelActionBuilder) {
        return new Then(channelSetupBuilder, channelActionBuilder);
    }
}
