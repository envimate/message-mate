package com.envimate.messageMate.useCaseConnecting;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SampleResponse {

    public static SampleResponse sampleResponse() {
        return new SampleResponse();
    }
}
