package com.envimate.messageMate.channel.givenWhenThen;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Given {
    public static When given(final ChannelSetupBuilder channelSetupBuilder) {
        return new When(channelSetupBuilder);
    }
}
