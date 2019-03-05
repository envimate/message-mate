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
        MessageFunctionBuilder.aMessageFunction()
                .forRequestType(BuyAppleRequest.class)
                .forResponseType(OfferReply.class)
                .with(BuyAppleRequest.class).answeredBy(AcceptOfferReply.class).or(DeclineOfferReply.class).orByError(DeclineOfferReply.class).orByError(DeclineOfferReply.class)
                .with(BuyAppleRequest.class).answeredBy(DeclineOfferReply.class)
                .with(BuyAppleRequest.class).answeredBy(AcceptOfferReply.class).orByError(DeclineOfferReply.class)
                .withGeneralErrorResponse(OfferReply.class)
                .obtainingCorrelationIdsOfRequestsWith(buyAppleRequest -> buyAppleRequest.correlationId)
                .obtainingCorrelationIdsOfResponsesWith(AcceptOfferReply::getCorrelationId)
                .usingMessageBus(null)
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
