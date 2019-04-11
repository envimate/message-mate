/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
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

package com.envimate.messageMate.internal.reflections;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ReflectionUtils {

    public static Set<Class<?>> getAllSuperClassesAndInterfacesIncludingClassItself(final Object object) {
        final Class<?> objectClass = object.getClass();
        final Set<Class<?>> classes = new HashSet<>();
        collectInheritedClasses(objectClass, classes);
        return classes;
    }

    public static Set<Class<?>> getAllSuperClassesAndInterfacesIncludingClassItself(final Class<?> aClass) {
        final Set<Class<?>> classes = getAllSuperClassesAndInterfaces(aClass);
        classes.add(aClass);
        return classes;
    }

    public static Set<Class<?>> getAllSuperClassesAndInterfaces(final Class<?> aClass) {
        final Set<Class<?>> classes = new HashSet<>();
        collectInheritedClasses(aClass, classes);
        return classes;
    }

    private static void collectInheritedClasses(final Class<?> aClass, final Set<Class<?>> classes) {
        final Class<?> superclass = aClass.getSuperclass();
        if (superclass != null) {
            classes.add(superclass);
            collectInheritedClasses(superclass, classes);
        }
        final Class<?>[] interfaces = aClass.getInterfaces();
        for (final Class<?> anInterface : interfaces) {
            classes.add(anInterface);
            collectInheritedClasses(anInterface, classes);
        }
    }

    public static List<Method> getAllPublicMethods(final Class<?> useCaseClass, final Collection<String> excludedMethods) {
        final Method[] methods = useCaseClass.getMethods();
        return Arrays.stream(methods)
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> !Modifier.isAbstract(method.getModifiers()))
                .filter(method -> method.getDeclaringClass().equals(useCaseClass))
                .filter(method -> !excludedMethods.contains(method.getName()))
                .collect(toList());
    }

    public static Constructor<?> getConstructorWithFewestArguments(final Class<?> eventClass) {
        final Constructor<?>[] declaredConstructors = eventClass.getDeclaredConstructors();
        if (declaredConstructors.length == 1) {
            return declaredConstructors[0];
        } else if (declaredConstructors.length > 1) {
            return stream(declaredConstructors)
                    .min(comparingInt(Constructor::getParameterCount))
                    .get();
        } else {
            final String message = "Cannot use constructor of event " + eventClass + " when no constructor exists.";
            throw new UnsupportedOperationException(message);
        }
    }

}
