/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.envimate.messageMate.useCaseAdapter.methodInvoking;

import com.envimate.messageMate.useCaseAdapter.mapping.RequestDeserializer;
import com.envimate.messageMate.useCaseAdapter.mapping.ResponseSerializer;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static com.envimate.messageMate.useCaseAdapter.methodInvoking.MethodInvocationException.methodInvocationException;
import static java.util.Arrays.stream;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SinglePublicUseCaseMethodInvokerImpl implements UseCaseMethodInvoker {
    private final Method useCaseMethod;

    public static SinglePublicUseCaseMethodInvokerImpl singlePublicUseCaseMethodInvoker(final Method method) {
        return new SinglePublicUseCaseMethodInvokerImpl(method);
    }

    @Override
    public Map<String, Object> invoke(final Object useCase,
                         final Object event,
                         final RequestDeserializer requestDeserializer,
                         final ResponseSerializer responseSerializer) {
        try {
            final Class<?>[] parameterTypes = useCaseMethod.getParameterTypes();

            final Map<String, Object> map = (Map<String, Object>) event; //TODO: use good exception for ClassCastException + test
            final Object[] parameters = stream(parameterTypes)
                    .map(parameterType -> requestDeserializer.deserializeRequest(parameterType, map))
                    .toArray();
            final Object returnValue = useCaseMethod.invoke(useCase, parameters);
            return responseSerializer.serializeReturnValue(returnValue);
        } catch (final IllegalAccessException e) {
            final Class<?> useCaseClass = useCase.getClass();
            throw methodInvocationException(useCaseClass, useCase, useCaseMethod, event, e);
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                final Class<?> useCaseClass = useCase.getClass();
                throw methodInvocationException(useCaseClass, useCase, useCaseMethod, event, e);
            }
        }
    }

}
