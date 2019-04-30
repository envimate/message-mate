package com.envimate.messageMate.shared.polling;

import java.util.function.BooleanSupplier;

public interface Poller {

    void poll(BooleanSupplier condition);

    void poll(BooleanSupplier condition, String exceptionMessage);
}
