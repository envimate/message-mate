package com.envimate.messageMate.useCaseConnecting;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SampleRequest {
    private final SampleResponse responseToReturn;
    private final RuntimeException exceptionToBeThrown;

    public static SampleRequest sampleRequestForResponse(final SampleResponse sampleResponse) {
        return new SampleRequest(sampleResponse, null);
    }

    public static SampleRequest sampleRequestThrowingException(final RuntimeException thrownException) {
        return new SampleRequest(null, thrownException);
    }

    public SampleResponse getResponseToReturn() {
        if (exceptionToBeThrown != null) {
            throw exceptionToBeThrown;
        } else {
            return responseToReturn;
        }
    }
}
