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

package com.envimate.messageMate.useCaseAdapter.usecaseInvoking;

import com.envimate.messageMate.useCaseAdapter.methodInvoking.SinglePublicUseCaseMethodInvokerImpl;
import com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.UseCaseFactory;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.List;

import static com.envimate.messageMate.internal.reflections.ForbiddenUseCaseMethods.NOT_ALLOWED_USECASE_PUBLIC_METHODS;
import static com.envimate.messageMate.internal.reflections.ReflectionUtils.getAllPublicMethods;
import static com.envimate.messageMate.useCaseAdapter.methodInvoking.SinglePublicUseCaseMethodInvokerImpl.singlePublicUseCaseMethodInvoker;
import static com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.ZeroArgumentsConstructorUseCaseFactory.zeroArgumentsConstructorUseCaseFactory;
import static com.envimate.messageMate.useCaseAdapter.usecaseInvoking.UseCaseInvocationInformation.useCaseInvocationInformation;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ClassBasedUseCaseInvokerImpl implements UseCaseInvoker {
    private final UseCaseInvocationInformation useCaseInvocationInformation;

    public static ClassBasedUseCaseInvokerImpl classBasedUseCaseInvoker(final Class<?> useCaseClass) {
        final UseCaseFactory useCaseFactory = zeroArgumentsConstructorUseCaseFactory(useCaseClass);
        final Method method = locateUseCaseMethod(useCaseClass);
        final SinglePublicUseCaseMethodInvokerImpl methodInvoker = singlePublicUseCaseMethodInvoker(method);
        final UseCaseInvocationInformation invocationInformation = useCaseInvocationInformation(useCaseFactory, methodInvoker);
        return new ClassBasedUseCaseInvokerImpl(invocationInformation);
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
    public UseCaseInvocationInformation getInvocationInformation() {
        return useCaseInvocationInformation;
    }

}
