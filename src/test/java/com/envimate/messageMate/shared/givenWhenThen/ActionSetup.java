package com.envimate.messageMate.shared.givenWhenThen;


import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActionSetup<T> {
    public final List<TestAction<T>> channelActions;

    public static <T> ActionSetup<T> actionSetup(final List<TestAction<T>> actions) {
        return new ActionSetup<>(actions);
    }

}
