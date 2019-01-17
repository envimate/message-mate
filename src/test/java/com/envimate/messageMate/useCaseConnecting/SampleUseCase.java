package com.envimate.messageMate.useCaseConnecting;

public class SampleUseCase {
    public SampleResponse useCaseMethod(final SampleRequest sampleRequest) {
        final SampleResponse sampleResponse = sampleRequest.getResponseToReturn();
        return sampleResponse;
    }
}
