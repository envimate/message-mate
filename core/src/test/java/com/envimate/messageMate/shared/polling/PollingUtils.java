package com.envimate.messageMate.shared.polling;

import com.envimate.messageMate.shared.validations.SharedTestValidations;
import lombok.RequiredArgsConstructor;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PollingUtils {
    public static void pollUntil(final BooleanSupplier condition) {
        System.out.println("Polling started");
        final TimeoutPoller poller = TimeoutPoller.timeoutPoller();
        poller.poll(condition);
    }


    public static void pollUntilEquals(final Supplier<Object> actualSupplier, final Object expected) {
        System.out.println("Polling started");
        final TimeoutPoller poller = TimeoutPoller.timeoutPoller();
        final String exceptionMessage = "Actual: " + actualSupplier.get() + ", Expected: " + expected;
        poller.poll(() -> {
            final Object actual = actualSupplier.get();
            return SharedTestValidations.testEquals(actual, expected);
        }, exceptionMessage);
    }

}
