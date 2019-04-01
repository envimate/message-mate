package com.envimate.messageMate.useCaseAdapter.severalParameter;


import com.envimate.messageMate.useCaseAdapter.*;
import com.envimate.messageMate.useCaseAdapter.methodInvoking.MissingParameterValueMappingException;
import com.envimate.messageMate.useCaseAdapter.methodInvoking.RedundantParameterValueMappingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SeveralParameterConfigurationResolver.class)
public class SeveralParameterUseCaseAdapterSpecs implements UseCaseAdapterSpecs {

    @Test
    public void testUseCaseAdapter_failsForMissingMapping(final TestUseCase testUseCase) {
        Given.given(UseCaseAdapterSetupBuilder.aUseCaseAdapter(testUseCase)
                .invokingTheUseCaseUsingAMappingMissingAParameter())
                .when(UseCaseAdapterActionBuilder.theAssociatedEventIsSend())
                .then(UseCaseAdapterValidationBuilder.expectAExceptionOfType(MissingParameterValueMappingException.class));
    }

    @Test
    public void testUseCaseAdapter_failsForRedundantMapping(final TestUseCase testUseCase) {
        Given.given(UseCaseAdapterSetupBuilder.aUseCaseAdapter(testUseCase)
                .invokingTheUseCaseUsingARedundantMappingMissingAParameter())
                .when(UseCaseAdapterActionBuilder.theAssociatedEventIsSend())
                .then(UseCaseAdapterValidationBuilder.expectAExceptionOfType(RedundantParameterValueMappingException.class));
    }

}