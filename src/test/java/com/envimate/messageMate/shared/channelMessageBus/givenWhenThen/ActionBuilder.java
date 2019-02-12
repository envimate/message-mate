package com.envimate.messageMate.shared.channelMessageBus.givenWhenThen;


import com.envimate.messageMate.qcec.shared.TestAction;

import java.util.List;

public interface ActionBuilder<T> {

    ActionBuilder<T> andThen(final ActionBuilder<T> followUpBuilder);

    List<TestAction<T>> build();

}
