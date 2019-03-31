package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static com.envimate.messageMate.subscribing.AcceptingBehavior.MESSAGE_ACCEPTED;
import static com.envimate.messageMate.subscribing.SubscriptionId.newUniqueId;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
final class UseCaseRequestExecutingSubscriber<T> implements Subscriber<ProcessingContext<T>> {
    private final MessageBus messageBus;
    private final UseCaseInvocationInformation mapping;
    private final UseCaseInstantiator useCaseInstantiator;
    private final SubscriptionId subscriptionId = newUniqueId();

    public static <T> Subscriber<ProcessingContext<T>> useCaseRequestExecutingSubscriber(final MessageBus messageBus,
                                                                                         final UseCaseInvocationInformation mapping,
                                                                                         final UseCaseInstantiator useCaseInstantiator) {
        ensureNotNull(messageBus, "messageBus");
        ensureNotNull(mapping, "mapping");
        ensureNotNull(useCaseInstantiator, "useCaseInstantiator");
        return new UseCaseRequestExecutingSubscriber<>(messageBus, mapping, useCaseInstantiator);
    }

    //TODO: think about Error when CorrelationId == null by messageBus
    @Override
    public AcceptingBehavior accept(final ProcessingContext<T> processingContext) {
        final Caller caller = mapping.caller;
        final Class<?> useCaseClass = mapping.useCaseClass;
        final Object useCase = useCaseInstantiator.instantiate(useCaseClass);
        final T event = processingContext.getPayload();
        final Object returnValue = caller.call(useCase, event).orElse(null);
        final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
        messageBus.send(returnValue, correlationId);
        return MESSAGE_ACCEPTED;
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }
}
