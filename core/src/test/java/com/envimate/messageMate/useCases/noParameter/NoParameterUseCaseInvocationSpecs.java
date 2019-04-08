package com.envimate.messageMate.useCases.noParameter;


import com.envimate.messageMate.useCases.TestUseCase;
import com.envimate.messageMate.useCases.UseCaseInvocationSpecs;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(NoParameterConfigurationResolver.class)
public class NoParameterUseCaseInvocationSpecs implements UseCaseInvocationSpecs {

    @Override
    public void testUseCaseAdapter_failsForMissingDeserializationMapping(final TestUseCase testUseCase) {
        // cannot be tested, when there are no parameter
    }
}
