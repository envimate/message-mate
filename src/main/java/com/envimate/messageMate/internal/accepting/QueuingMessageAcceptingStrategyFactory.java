package com.envimate.messageMate.internal.accepting;

import com.envimate.messageMate.internal.eventloop.AcceptingEventLoop;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class QueuingMessageAcceptingStrategyFactory<T> implements MessageAcceptingStrategyFactory<T> {

    public static <T> QueuingMessageAcceptingStrategyFactory<T> queingMEssageAcceptingStrategyFactory() {
        return new QueuingMessageAcceptingStrategyFactory<>();
    }

    @Override
    public MessageAcceptingStrategy<T> createNew(final AcceptingEventLoop<T> eventLoop) {
        return new QueuingMessageAcceptingStrategy<>(eventLoop);
    }
}
