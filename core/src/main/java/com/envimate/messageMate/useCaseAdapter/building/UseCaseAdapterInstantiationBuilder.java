package com.envimate.messageMate.useCaseAdapter.building;

import com.envimate.messageMate.useCaseAdapter.UseCaseAdapter;
import com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.UseCaseFactory;
import com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;

import static com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.ZeroArgumentsConstructorUseCaseFactory.zeroArgumentsConstructorUseCaseFactory;

// TODO mit guice
public interface UseCaseAdapterInstantiationBuilder {

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
