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

package com.envimate.messageMate.soonToBeExternal.usecaseCreating;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;

import static com.envimate.messageMate.soonToBeExternal.usecaseCreating.ZeroArgumentsConstructorUseCaseFactoryException.zeroArgumentsConstructorUseCaseFactoryException;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ZeroArgumentsConstructorUseCaseFactory implements UseCaseFactory {
    private final Class<?> useCaseClass;

    public static UseCaseFactory zeroArgumentsConstructorUseCaseFactory(final Class<?> useCaseClass) {
        return new ZeroArgumentsConstructorUseCaseFactory(useCaseClass);
    }

    @Override
    public Object createInstance() {
        try {
            return useCaseClass.getDeclaredConstructor().newInstance();
        } catch (final NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw zeroArgumentsConstructorUseCaseFactoryException(useCaseClass, e);
        }
    }
}
