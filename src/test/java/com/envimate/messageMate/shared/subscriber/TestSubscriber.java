package com.envimate.messageMate.shared.subscriber;


import com.envimate.messageMate.subscribing.Subscriber;

import java.util.List;

public interface TestSubscriber<T> extends Subscriber<T> {

    List<T> getReceivedMessages();

}
