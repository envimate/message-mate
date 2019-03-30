package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.messageBus.MessageBus;

public interface UseCaseAdapter {

    void attachTo(MessageBus messageBus);

}
