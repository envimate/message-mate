package com.envimate.messageMate.useCases.useCaseAdapter.usecaseCalling;

import com.envimate.messageMate.useCases.useCaseAdapter.mapping.RequestDeserializer;
import com.envimate.messageMate.useCases.useCaseAdapter.mapping.ResponseSerializer;
import com.envimate.messageMate.useCases.useCaseAdapter.methodInvoking.UseCaseMethodInvoker;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static com.envimate.messageMate.internal.reflections.ForbiddenUseCaseMethods.NOT_ALLOWED_USECASE_PUBLIC_METHODS;
import static com.envimate.messageMate.internal.reflections.ReflectionUtils.getAllPublicMethods;
import static com.envimate.messageMate.useCases.useCaseAdapter.methodInvoking.SerializingMethodInvoker.serializingMethodInvoker;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SinglePublicUseCaseMethodCaller<U> implements Caller<U> {
    private final UseCaseMethodInvoker methodInvoker;

    public static <U> SinglePublicUseCaseMethodCaller<U> singlePublicUseCaseMethodCaller(final Class<U> useCaseClass) {
        final Method method = locateUseCaseMethod(useCaseClass);
        final UseCaseMethodInvoker methodInvoker = serializingMethodInvoker(method);
        return new SinglePublicUseCaseMethodCaller<>(methodInvoker);
    }

    private static Method locateUseCaseMethod(final Class<?> useCaseClass) {
        final List<Method> useCaseMethods = getAllPublicMethods(useCaseClass, NOT_ALLOWED_USECASE_PUBLIC_METHODS);
        if (useCaseMethods.size() == 1) {
            return useCaseMethods.get(0);
        } else {
            final String message = String.format("Use case classes must have 1 instance method. Found the methods %s " +
                            "for class %s",
                    useCaseMethods, useCaseClass);
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public Map<String, Object> call(final U useCase,
                                    final Object event,
                                    final RequestDeserializer requestDeserializer,
                                    final ResponseSerializer responseSerializer) {
        final Map<String, Object> responseMap = methodInvoker.invoke(useCase, event, requestDeserializer, responseSerializer);
        return responseMap;
    }
}
