package com.envimate.messageMate.soonToBeExternal.neww;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.soonToBeExternal.EventToUseCaseMapping;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static com.envimate.messageMate.soonToBeExternal.neww.UseCaseRequestExecutingSubscriber.useCaseRequestExecutingSubscriber;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseAdapterImpl implements UseCaseAdapter {
    private final List<EventToUseCaseMapping> eventToUseCaseMappings;
    private final UseCaseInstantiator useCaseInstantiator;

    public static UseCaseAdapter useCaseAdapterImpl(final List<EventToUseCaseMapping> eventToUseCaseMappings,
                                                    final UseCaseInstantiator useCaseInstantiator) {
        ensureNotNull(eventToUseCaseMappings, "eventToUseCaseMappings");
        ensureNotNull(useCaseInstantiator, "useCaseInstantiator");
        return new UseCaseAdapterImpl(eventToUseCaseMappings, useCaseInstantiator);
    }

    @Override
    public void attachTo(final MessageBus messageBus) {
        eventToUseCaseMappings.forEach(mapping -> {
            final UseCaseRequestExecutingSubscriber useCaseRequestSubscriber = (UseCaseRequestExecutingSubscriber) useCaseRequestExecutingSubscriber(messageBus, mapping, useCaseInstantiator);
            messageBus.subscribeRaw(mapping.eventClass, useCaseRequestSubscriber);
        });
    }
}
