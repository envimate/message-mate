package com.envimate.messageMate.soonToBeExternal.neww;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.soonToBeExternal.EventToUseCaseDispatcherBuilder;

public interface UseCaseAdapter {

    void attachTo(MessageBus messageBus);

    static void main(String[] args) {
        // TODO
        EventToUseCaseDispatcherBuilder.anEventToUseCaseDispatcher()
                .invokingUseCase(null).forEvent(null).callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor();
    }
}
