package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.messageBus.MessageBus;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static com.envimate.messageMate.useCaseAdapter.UseCaseRequestExecutingSubscriber.useCaseRequestExecutingSubscriber;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseAdapterImpl implements UseCaseAdapter {
    private final List<UseCaseInvocationInformation> useCaseInvocationInformations;
    private final UseCaseInstantiator useCaseInstantiator;

    public static UseCaseAdapter useCaseAdapterImpl(final List<UseCaseInvocationInformation> useCaseInvocationInformations,
                                                    final UseCaseInstantiator useCaseInstantiator) {
        ensureNotNull(useCaseInvocationInformations, "useCaseInvocationInformations");
        ensureNotNull(useCaseInstantiator, "useCaseInstantiator");
        return new UseCaseAdapterImpl(useCaseInvocationInformations, useCaseInstantiator);
    }

    @Override
    public void attachTo(final MessageBus messageBus) {
        useCaseInvocationInformations.forEach(mapping -> {
            final UseCaseRequestExecutingSubscriber useCaseRequestSubscriber = (UseCaseRequestExecutingSubscriber) useCaseRequestExecutingSubscriber(messageBus, mapping, useCaseInstantiator);
            messageBus.subscribeRaw(mapping.eventClass, useCaseRequestSubscriber);
        });
    }
}
