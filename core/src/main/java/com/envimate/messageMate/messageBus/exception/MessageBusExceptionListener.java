package com.envimate.messageMate.messageBus.exception;

import java.util.function.BiConsumer;

/**
 * An exception listener, that can be added dynamically for class to the {@code MessageBus}.
 *
 * @param <T> the type of messages, for which the listener accepts exceptions
 * @see <a href="https://github.com/envimate/message-mate#dynamically-adding-exception-listener">Message Mate Documentation</a>
 */
public interface MessageBusExceptionListener<T> extends BiConsumer<T, Exception> {
}
