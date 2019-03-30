package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.soonToBeExternal.building.EventToUseCaseDispatcherStep2Builder;
import com.envimate.messageMate.soonToBeExternal.building.EventToUseCaseDispatcherStep3Builder;
import com.envimate.messageMate.soonToBeExternal.building.EventToUseCaseDispatcherStepCallingBuilder;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.messageMate.useCaseAdapter.EventToUseCaseMapping.eventToUseCaseMapping;
import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterImpl.useCaseAdapterImpl;

public class UseCaseAdapterBuilder implements EventToUseCaseDispatcherStep3Builder {
    private final List<EventToUseCaseMapping> eventToUseCaseMappings = new LinkedList<>();

    public static EventToUseCaseDispatcherStep3Builder anUseCaseAdapter() {
        return new UseCaseAdapterBuilder();
    }

    @Override
    public <USECASE> EventToUseCaseDispatcherStep2Builder<USECASE> invokingUseCase(final Class<USECASE> useCaseClass) {
        return new EventToUseCaseDispatcherStep2Builder<USECASE>() {
            @Override
            public <EVENT> EventToUseCaseDispatcherStepCallingBuilder<USECASE, EVENT> forEvent(Class<EVENT> eventClass) {
                return caller -> {
                    eventToUseCaseMappings.add(eventToUseCaseMapping(useCaseClass, eventClass, caller));
                    return UseCaseAdapterBuilder.this;
                };
            }
        };
    }


    @Override
    public UseCaseAdapter obtainingUseCaseInstancesUsing(final UseCaseInstantiator useCaseInstantiator) {
        return useCaseAdapterImpl(eventToUseCaseMappings, useCaseInstantiator);
    }
}
