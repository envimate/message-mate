package com.envimate.messageMate.channel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelStatusInformation {
    @Getter
    private final ChannelStatistics channelStatistics;

    public static ChannelStatusInformation channelStatusInformation(final ChannelStatistics channelStatistics) {
        return new ChannelStatusInformation(channelStatistics);
    }
}
