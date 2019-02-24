package com.envimate.messageMate.messageBus;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.channel.action.Consume;
import com.envimate.messageMate.messageBus.brokering.MessageBusBrokerStrategy;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusConsumeAction {

    public static Consume<Object> messageBusConsumeAction(final MessageBusBrokerStrategy brokerStrategy) {
        return Consume.consume(objectProcessingContext -> {
            final Object message = objectProcessingContext.getPayload();
            final Class<?> messageClass = message.getClass();
            System.out.println("Querying BS: " + messageClass);
            final Set<Channel<?>> channels = brokerStrategy.getDeliveringChannelsFor(messageClass);
            for (Channel<?> channel : channels) {
                final ProcessingContext tProcessingContext = ProcessingContext.processingContext(message);
                channel.accept(tProcessingContext);
            }
        });
    }
}
