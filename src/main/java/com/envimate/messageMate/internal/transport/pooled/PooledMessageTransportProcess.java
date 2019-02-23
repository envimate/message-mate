package com.envimate.messageMate.internal.transport.pooled;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.internal.eventloop.TransportEventLoop;
import com.envimate.messageMate.internal.filtering.FilterApplier;
import com.envimate.messageMate.internal.filtering.PostFilterActions;
import com.envimate.messageMate.internal.transport.MessageTransportProcess;
import com.envimate.messageMate.internal.transport.SubscriberCalculation;
import com.envimate.messageMate.subscribing.Subscriber;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

public class PooledMessageTransportProcess<T> implements MessageTransportProcess<T> {
    private final TransportEventLoop<T> eventLoop;
    private final FilterApplier<T> filterApplier;
    private final List<Filter<T>> filters;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final SubscriberCalculation<T> subscriberCalculation;
    private final PoolControlThread<T> controlThread;

    public PooledMessageTransportProcess(final TransportEventLoop<T> eventLoop, final FilterApplier<T> filterApplier,
                                         final List<Filter<T>> filters, final ThreadPoolExecutor threadPoolExecutor,
                                         final SubscriberCalculation<T> subscriberCalculation,
                                         final PoolControlThread<T> controlThread) {
        this.eventLoop = eventLoop;
        this.filterApplier = filterApplier;
        this.filters = filters;
        this.threadPoolExecutor = threadPoolExecutor;
        this.subscriberCalculation = subscriberCalculation;
        this.controlThread = controlThread;
    }

    @Override
    public void start(final T message) {
        threadPoolExecutor.execute(() -> executeTransport(message));
    }

    private void executeTransport(final T message) {
        eventLoop.messageTransportStarted(message);
        final List<Subscriber<T>> receivers = subscriberCalculation.apply(message);
        eventLoop.messageFilteringStarted(message);
        filterApplier.applyAll(message, filters, new PostFilterActions<T>() {
            @Override
            public void onAllPassed(final T message) {
                controlThread.messagePassedAllFilter(message);
                eventLoop.requestDelivery(message, receivers);
                controlThread.messageHandedOverToDelivery(message);
            }

            @Override
            public void onReplaced(final T message) {
                controlThread.messageReplacedByFilter(message);
            }

            @Override
            public void onBlock(final T message) {
                controlThread.messageBlockedByFilter(message);
            }

            @Override
            public void onForgotten(final T message) {
                controlThread.messageForgottenByFilter(message);
            }
        });
    }
}
