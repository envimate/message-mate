package com.envimate.messageMate;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.correlation.CorrelationId;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.messageBus.MessageBusType;
import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.messageFunction.MessageFunctionBuilder;
import com.envimate.messageMate.messageFunction.ResponseFuture;

import java.util.List;

public class SampleMain {
    public static void main(String[] args) {

        MessageBus messageBus = MessageBusBuilder.aMessageBus()
                .forType(MessageBusType.ASYNCHRONOUS)
                .withAsynchronousConfiguration(asyncConfig)
                .build();

        MessageFunction<BuyAppleRequest, OfferReply> messageFunction = MessageFunctionBuilder.aMessageFunction()
                .forRequestType(BuyAppleRequest.class)
                .forResponseType(OfferReply.class)
                .with(BuyAppleRequest.class)
                .answeredBy(AcceptOfferReply.class)
                .orByError(DeclineOfferReply.class)
                .obtainingCorrelationIdsOfRequestsWith(buyAppleRequest -> buyAppleRequest.correlationId)
                .obtainingCorrelationIdsOfResponsesWith(OfferReply::getCorrelationId)
                .usingMessageBus(messageBus)
                .build();

        new Farmer(messageBus, 11);

        ResponseFuture<OfferReply> responseFuture = messageFunction.request(new BuyAppleRequest(5));
        responseFuture.then((response, wasSuccessful, exception) -> {
            if (exception != null) {
                System.out.println("Exception occured: " + exception);
            } else {
                if (wasSuccessful) {
                    System.out.println("AcceptOfferReply received: " + response);
                } else {
                    System.out.println("DeclineOfferReply received: " + response);
                }
            }
        });

        Channel<T> channel;

        List<Filter<ProcessingContext<T>>> preFilter = channel.getPreFilter();
        List<Filter<ProcessingContext<T>>> processFilter = channel.getProcessFilter();
        List<Filter<ProcessingContext<T>>> postFilter = channel.getPostFilter();

        channel.removePreFilter(filter);
        channel.removeProcessFilter(filter);
        channel.removePostFilter(filter);

        MessageFunctionBuilder.aMessageFunction()
                .forRequestType(BuyAppleRequest.class)
                .forResponseType(OfferReply.class)
                .with(BuyAppleRequest.class)
                .answeredBy(AcceptOfferReply.class).or(AcceptOfferReply.class)
                .orByError(DeclineOfferReply.class).orByError(DeclineOfferReply.class)
                .with(BuyAppleRequest.class)
                .answeredBy(AcceptOfferReply.class).or(AcceptOfferReply.class)
                .orByError(DeclineOfferReply.class).orByError(DeclineOfferReply.class)
                .withGeneralErrorResponse(DeclineOfferReply.class)
                .obtainingCorrelationIdsOfRequestsWith(buyAppleRequest -> buyAppleRequest.correlationId)
                .obtainingCorrelationIdsOfResponsesWith(OfferReply::getCorrelationId)
                .usingMessageBus(messageBus)
                .build();
    }


    interface OfferReply {
        CorrelationId getCorrelationId();
    }

    static class Farmer {
        private int stock;

        public Farmer(MessageBus messageBus, int stock) {
            this.stock = stock;
            messageBus.subscribe(BuyAppleRequest.class, buyAppleRequest -> {
                CorrelationId correlationId = buyAppleRequest.correlationId;
                if (stock >= buyAppleRequest.numberOfApples) {
                    AcceptOfferReply reply = new AcceptOfferReply(correlationId);
                    messageBus.send(reply);
                } else {
                    DeclineOfferReply reply = new DeclineOfferReply(correlationId);
                    messageBus.send(reply);
                }
            });
        }
    }

    static class BuyAppleRequest {
        public int numberOfApples;
        public CorrelationId correlationId = CorrelationId.newUniqueId();

        public BuyAppleRequest(int numberOfApples) {
            this.numberOfApples = numberOfApples;
        }
    }

    static class AcceptOfferReply implements OfferReply {
        public CorrelationId correlationId;

        public AcceptOfferReply(CorrelationId correlationId) {
            this.correlationId = correlationId;
        }

        @Override
        public CorrelationId getCorrelationId() {
            return correlationId;
        }
    }


    static class DeclineOfferReply implements OfferReply {
        public CorrelationId correlationId;

        public DeclineOfferReply(CorrelationId correlationId) {
            this.correlationId = correlationId;
        }

        @Override
        public CorrelationId getCorrelationId() {
            return correlationId;
        }
    }

}
