package com.envimate.messageMate.useCases.exceptionThrowing;


import com.envimate.messageMate.useCases.TestUseCase;
import com.envimate.messageMate.useCases.UseCaseInvocationSpecs;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ExceptionThrowingConfigurationResolver.class)
public class ExceptionThrowingUseCaseInvocationSpecs implements UseCaseInvocationSpecs {

    @Override
    public void testUseCaseAdapter_failsForMissingSerializationMapping(final TestUseCase testUseCase) {
        // serialization is not invoked since the exception is always thrown
    }
}
