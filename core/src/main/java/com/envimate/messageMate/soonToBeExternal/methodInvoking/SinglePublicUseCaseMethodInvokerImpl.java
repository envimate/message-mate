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

package com.envimate.messageMate.soonToBeExternal.methodInvoking;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import static com.envimate.messageMate.soonToBeExternal.methodInvoking.MethodInvocationException.methodInvocationException;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SinglePublicUseCaseMethodInvokerImpl implements UseCaseMethodInvoker {
    private final Method useCaseMethod;

    public static SinglePublicUseCaseMethodInvokerImpl singlePublicUseCaseMethodInvoker(final Method method) {
        return new SinglePublicUseCaseMethodInvokerImpl(method);
    }

    @Override
    public Object invoke(final Object useCase, final Object event, final List<Object> parameter) {
        try {
            if (parameter.size() == 0) {
                return useCaseMethod.invoke(useCase, event);
            } else {
                final List<Object> arguments = new LinkedList<>(parameter);
                arguments.add(0, event);
                return useCaseMethod.invoke(useCase, arguments.toArray());
            }
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
