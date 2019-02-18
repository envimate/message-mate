package com.envimate.messageMate.internal.transport;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.internal.eventloop.TransportEventLoop;
import com.envimate.messageMate.internal.transport.pooled.PooledMessageTransportProcessFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class MessageTransportProcessFactoryFactory {

    public static <T> MessageTransportProcessFactory<T> messageTransportProcessFactory(final MessageTransportConfiguration configuration,
                                                                                       final List<Filter<T>> filters,
                                                                                       final TransportEventLoop<T> eventLoop,
                                                                                       final SubscriberCalculation<T> subscriberCalculation) {
        final MessageTransportType type = configuration.getMessageTransportType();
        switch (type) {
            case SYNCHRONOUS:
                return new SynchronousTransportProcessFactory<>(filters, eventLoop, subscriberCalculation);
            case POOLED:
                final int numberOfThreads = configuration.getNumberOfThreads();
                return new PooledMessageTransportProcessFactory<>(eventLoop, filters, subscriberCalculation, numberOfThreads);
            default:
                final String messageTransportTypename = MessageTransportType.class.getSimpleName();
                final String message = format("Unknown %s %s.", messageTransportTypename, type);
                throw new IllegalArgumentException(message);
        }
    }
}
