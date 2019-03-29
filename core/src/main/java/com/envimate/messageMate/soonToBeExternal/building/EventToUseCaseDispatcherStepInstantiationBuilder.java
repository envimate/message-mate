package com.envimate.messageMate.soonToBeExternal.building;

import com.envimate.messageMate.soonToBeExternal.neww.UseCaseAdapter;
import com.envimate.messageMate.soonToBeExternal.neww.UseCaseInstantiator;
import com.envimate.messageMate.soonToBeExternal.usecaseCreating.UseCaseFactory;

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
