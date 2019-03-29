package com.envimate.messageMate.soonToBeExternal.building;

import com.envimate.messageMate.soonToBeExternal.usecaseCreating.UseCaseFactory;

import java.util.function.Function;

import static com.envimate.messageMate.soonToBeExternal.usecaseCreating.ZeroArgumentsConstructorUseCaseFactory.zeroArgumentsConstructorUseCaseFactory;

// TODO mit guice
public interface EventToUseCaseDispatcherStepInstantiationBuilder<T> {

    default T obtainingUseCaseInstancesUsingTheZeroArgumentConstructor() {
        return obtainingUseCaseInstancesUsing((useCaseClass) -> {
            // TODO no not use reflection on every call
            final UseCaseFactory factory = zeroArgumentsConstructorUseCaseFactory(useCaseClass);
            return factory.createInstance();
        });
    }

    T obtainingUseCaseInstancesUsing(Function<Class, Object> instantiator);
}
