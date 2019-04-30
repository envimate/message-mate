package com.envimate.messageMate.shared.polling;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.function.BooleanSupplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TimeoutPoller implements Poller {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(1);

    public static TimeoutPoller timeoutPoller() {
        return new TimeoutPoller();
    }

    @Override
    public void poll(final BooleanSupplier condition) {
        final TestStatePollingTimeoutException exception = new TestStatePollingTimeoutException();
        poll(condition, exception);
    }

    @Override
    public void poll(final BooleanSupplier condition, final String exceptionMessage) {
        final TestStatePollingTimeoutException exception = new TestStatePollingTimeoutException(exceptionMessage);
        poll(condition, exception);
    }

    private void poll(final BooleanSupplier condition, final TestStatePollingTimeoutException exception) {
        final long timeout = calculateTimeoutOffsetInMilliseconds();
        try {
            while (!condition.getAsBoolean()) {
                System.out.println("polling");
                MILLISECONDS.sleep(10);
                if (timeout < System.currentTimeMillis()) {
                    throw exception;
                }
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private long calculateTimeoutOffsetInMilliseconds() {
        final long offset = DEFAULT_TIMEOUT.toMillis();
        return System.currentTimeMillis() + offset;
    }
}
