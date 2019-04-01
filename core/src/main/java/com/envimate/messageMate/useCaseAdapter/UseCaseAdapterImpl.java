package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.UseCaseCallingInformation;
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
    private final List<UseCaseCallingInformation> useCaseCallingInformations;
    private final UseCaseInstantiator useCaseInstantiator;

    public static UseCaseAdapter useCaseAdapterImpl(final List<UseCaseCallingInformation> useCaseCallingInformations,
                                                    final UseCaseInstantiator useCaseInstantiator) {
        ensureNotNull(useCaseCallingInformations, "useCaseCallingInformations");
        ensureNotNull(useCaseInstantiator, "useCaseInstantiator");
        return new UseCaseAdapterImpl(useCaseCallingInformations, useCaseInstantiator);
    }

    @Override
    public void attachTo(final MessageBus messageBus) {
        useCaseCallingInformations.forEach(callingInformation -> {
            final Subscriber<ProcessingContext<Object>> useCaseRequestSubscriber = useCaseRequestExecutingSubscriber(messageBus, callingInformation, useCaseInstantiator);
            messageBus.subscribeRaw(callingInformation.getEventType(), useCaseRequestSubscriber);
        });
    }
}
