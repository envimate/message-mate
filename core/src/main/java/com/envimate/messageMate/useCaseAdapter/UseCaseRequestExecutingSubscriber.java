package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import com.envimate.messageMate.useCaseAdapter.methodInvoking.ParameterValueMappings;
import com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.Caller;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.UseCaseCallingInformation;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static com.envimate.messageMate.subscribing.AcceptingBehavior.MESSAGE_ACCEPTED;
import static com.envimate.messageMate.subscribing.SubscriptionId.newUniqueId;
import static com.envimate.messageMate.useCaseAdapter.UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
final class UseCaseRequestExecutingSubscriber implements Subscriber<ProcessingContext<Object>> {
    private final MessageBus messageBus;
    private final UseCaseCallingInformation useCaseCallingInformation;
    private final UseCaseInstantiator useCaseInstantiator;
    private final SubscriptionId subscriptionId = newUniqueId();

    public static UseCaseRequestExecutingSubscriber useCaseRequestExecutingSubscriber(final MessageBus messageBus,
                                                                                      final UseCaseCallingInformation useCaseCallingInformation,
                                                                                      final UseCaseInstantiator useCaseInstantiator) {
        ensureNotNull(messageBus, "messageBus");
        ensureNotNull(useCaseCallingInformation, "useCaseCallingInformation");
        ensureNotNull(useCaseInstantiator, "useCaseInstantiator");
        return new UseCaseRequestExecutingSubscriber(messageBus, useCaseCallingInformation, useCaseInstantiator);
    }

    @Override
    public AcceptingBehavior accept(final ProcessingContext<Object> processingContext) {
        final Caller caller = useCaseCallingInformation.getCaller();
        final Class<?> useCaseClass = useCaseCallingInformation.getUseCaseClass();
        final Object useCase = useCaseInstantiator.instantiate(useCaseClass);
        final Object event = processingContext.getPayload();
        final ParameterValueMappings parameterValueMappings = useCaseCallingInformation.getParameterValueMappings();
        @SuppressWarnings("unchecked")
        final Object returnValue = caller.call(useCase, event, parameterValueMappings);
        final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
        messageBus.send(USE_CASE_RESPONSE_EVENT_TYPE, returnValue, correlationId);
        return MESSAGE_ACCEPTED;
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }
}
