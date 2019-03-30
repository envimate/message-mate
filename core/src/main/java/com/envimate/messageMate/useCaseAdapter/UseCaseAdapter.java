package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.soonToBeExternal.EventToUseCaseDispatcherBuilder;

public interface UseCaseAdapter {

    void attachTo(MessageBus messageBus);

}
