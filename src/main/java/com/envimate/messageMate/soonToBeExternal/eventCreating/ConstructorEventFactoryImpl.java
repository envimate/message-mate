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

package com.envimate.messageMate.soonToBeExternal.eventCreating;

import com.envimate.messageMate.soonToBeExternal.EventFactory;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.envimate.messageMate.soonToBeExternal.eventCreating.ConstructorEventFactoryException.constructorEventFactoryException;
import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ConstructorEventFactoryImpl implements EventFactory {
    private final Class<?> eventClass;
    private final List<Class<?>> parameterTypes;

    public static ConstructorEventFactoryImpl constructorEventFactory(final Class<?> eventClass,
                                                                      final Constructor<?> constructor) {
        final Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
        final List<Class<?>> paramTypes = asList(constructorParameterTypes);
        return constructorEventFactory(eventClass, paramTypes);
    }

    public static ConstructorEventFactoryImpl constructorEventFactory(final Class<?> eventClass,
                                                                      final List<Class<?>> paramTypes) {
        return new ConstructorEventFactoryImpl(eventClass, paramTypes);
    }

    @Override
    public Class<?> eventType() {
        return eventClass;
    }

    @Override
    public List<Class<?>> parameterTypes() {
        return parameterTypes;
    }

    @Override
    public Object createEvent(final List<Object> parameters) {
        try {
            @SuppressWarnings("rawtypes")
            final Class<?>[] parameterClasses = parameterTypes.toArray(new Class[]{});
            return eventClass.getDeclaredConstructor(parameterClasses).newInstance(parameters.toArray());
        } catch (final NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw constructorEventFactoryException(eventClass, parameters, e);
        }
    }
}
