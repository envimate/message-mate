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

package com.envimate.messageMate.useCaseConnecting.useCase;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.envimate.messageMate.useCaseConnecting.useCase.UseCaseInvocationException.useCaseInvocationException;
import static com.envimate.messageMate.useCaseConnecting.useCase.ZeroArgumentsConstructorUseCaseFactory.zeroArgumentsConstructorUseCaseFactory;
import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseInvoker implements UseCase {
    private static final Collection<String> EXCLUDED_METHODS = new HashSet<>(asList("equals", "hashCode", "toString", "clone",
            "finalize", "wait", "getClass", "notify", "notifyAll"));
    private final Class<?> useCaseClass;
    private final Method useCaseMethod;
    private final UseCaseFactory useCaseFactory;

    public static UseCaseInvoker useCase(final Class<?> useCaseClass) {
        final UseCaseFactory useCaseFactory = zeroArgumentsConstructorUseCaseFactory();
        return useCase(useCaseClass, useCaseFactory);
    }

    public static UseCaseInvoker useCase(final Class<?> useCaseClass, final UseCaseFactory useCaseFactory) {
        final Method useCaseMethod = locateUseCaseMethod(useCaseClass);
        return new UseCaseInvoker(useCaseClass, useCaseMethod, useCaseFactory);
    }

    private static Method locateUseCaseMethod(final Class<?> useCaseClass) {
        final Method[] methods = useCaseClass.getMethods();
        final List<Method> useCaseMethods = Arrays.stream(methods)
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> !Modifier.isAbstract(method.getModifiers()))
                .filter(method -> method.getDeclaringClass().equals(useCaseClass))
                .filter(method -> !EXCLUDED_METHODS.contains(method.getName()))
                .collect(Collectors.toList());
        if (useCaseMethods.size() != 1) {
            final String message = String.format("Use case classes must have 1 instance method. Found the methods %s " +
                            "for class %s",
                    useCaseMethods, useCaseClass);
            throw new IllegalArgumentException(message);
        } else {
            return useCaseMethods.get(0);
        }
    }

    @Override
    public Object invoke(final Object request) {
        final Object useCaseInstance = useCaseFactory.createInstance(useCaseClass);
        try {
            return useCaseMethod.invoke(useCaseInstance, request);
        } catch (final IllegalAccessException e) {
            throw useCaseInvocationException(useCaseClass, useCaseMethod, request, e);
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw useCaseInvocationException(useCaseClass, useCaseMethod, request, e);
            }
        }
    }

}
