package com.envimate.messageMate.useCases.building;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.serializedMessageBus.SerializedMessageBus;
import com.envimate.messageMate.useCases.useCaseAdapter.UseCaseAdapter;
import com.envimate.messageMate.useCases.useCaseBus.UseCaseBus;

public interface BuilderStepBuilder {

    UseCaseBus build(SerializedMessageBus serializedMessageBus);

    UseCaseBus build(MessageBus messageBus);

    UseCaseAdapter buildAsStandaloneAdapter();

}
