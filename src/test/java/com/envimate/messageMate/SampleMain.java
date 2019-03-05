package com.envimate.messageMate;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ChannelBuilder;
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.channel.action.Action;
import com.envimate.messageMate.channel.action.actionHandling.ActionHandler;
import com.envimate.messageMate.channel.action.actionHandling.ActionHandlerSet;
import com.envimate.messageMate.channel.action.actionHandling.DefaultActionHandlerSet;
import com.envimate.messageMate.correlation.CorrelationId;

import java.io.PrintStream;

public class SampleMain {
    public static void main(String[] args) {
        ActionHandlerSet<Object> actionHandlerSet = DefaultActionHandlerSet.defaultActionHandlerSet();
        actionHandlerSet.registerActionHandler(Log.class, new LogActionHandler<>());
        Channel<Object> channel = ChannelBuilder.aChannel(Object.class)
                .withDefaultAction(new Log<>())
                .withActionHandlerSet(actionHandlerSet)
                .build();
    }


    interface OfferReply {
        CorrelationId getCorrelationId();
    }

    static class Log<T> implements Action<T> {
        private final PrintStream stream = System.out;

        public PrintStream getStream() {
            return stream;
        }
    }

    static class LogActionHandler<T> implements ActionHandler<Log<T>, T> {
        @Override
        public void handle(Log<T> action, ProcessingContext<T> processingContext) {
            final PrintStream stream = action.getStream();
            stream.println(processingContext);
        }
    }

}
