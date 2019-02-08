package com.envimate.messageMate.sooToBeExternal;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.soonToBeExternal.*;

import java.util.ArrayList;
import java.util.List;

public class EventDispatcherMAIN {

    public static void main(String[] args) {
        final MessageBus messageBus = MessageBusBuilder.aMessageBus().build();
        final EventToUseCaseDispatcher useCaseDispatcher = EventToUseCaseDispatcherBuilder.anEventToUseCaseDispatcher()
                .invokingUseCase(TestUseCase.class)
                //.invokingUseCase(new TestUseCase())
                .usingMessageBus(messageBus)
                .build();

        messageBus.add((message, receivers, filterActions) -> {
            if (message instanceof UseCaseCallRequest) {
                final List<Object> parameter = ((UseCaseCallRequest) message).getParameter();
                parameter.add("hello");
            }
            filterActions.pass(message);
        });

        final EventFactory eventFactory = useCaseDispatcher.eventFactoryFor(TestEvent.class);
        final Object event = eventFactory.createEvent(new ArrayList<>());
        final UseCaseResponseFuture useCaseResponseFuture = useCaseDispatcher.dispatch(event);
        useCaseResponseFuture.then((response, wasSuccessful, exception) -> {
            if (exception != null) {
                throw new RuntimeException(exception);
            } else {
                System.out.println(response);
            }
        });
    }

}
