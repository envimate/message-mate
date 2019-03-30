package com.envimate.messageMate.soonToBeExternal.building;

import com.envimate.messageMate.soonToBeExternal.usecaseCreating.UseCaseFactory;
import com.envimate.messageMate.useCaseAdapter.UseCaseAdapter;
import com.envimate.messageMate.useCaseAdapter.UseCaseInstantiator;

import static com.envimate.messageMate.soonToBeExternal.usecaseCreating.ZeroArgumentsConstructorUseCaseFactory.zeroArgumentsConstructorUseCaseFactory;

// TODO mit guice
public interface EventToUseCaseDispatcherStepInstantiationBuilder {

    default UseCaseAdapter obtainingUseCaseInstancesUsingTheZeroArgumentConstructor() {
        return obtainingUseCaseInstancesUsing(new UseCaseInstantiator() {
            @Override
            public <T> T instantiate(Class<T> useCaseClass) {
                // TODO no not use reflection on every call
                final UseCaseFactory factory = zeroArgumentsConstructorUseCaseFactory(useCaseClass);
                return (T) factory.createInstance();
            }
        });
    }

    UseCaseAdapter obtainingUseCaseInstancesUsing(UseCaseInstantiator useCaseInstantiator);
}
