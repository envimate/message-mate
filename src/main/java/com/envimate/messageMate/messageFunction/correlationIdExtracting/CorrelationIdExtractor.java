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

package com.envimate.messageMate.messageFunction.correlationIdExtracting;

import com.envimate.messageMate.correlation.CorrelationId;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.envimate.messageMate.internal.reflections.ReflectionUtils.getAllSuperClassesAndInterfaces;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class CorrelationIdExtractor<T> {

    private final Map<Class<T>, CorrelationIdExtraction<T>> extractionMap = new HashMap<>();

    public static <T> CorrelationIdExtractor<T> correlationIdExtractor() {
        return new CorrelationIdExtractor<>();
    }

    public CorrelationId extract(final T message) {
        final Class<T> messageClass = (Class<T>) message.getClass();
        final CorrelationIdExtraction<T> correlationIdExtraction = extractionFor(messageClass);
        return correlationIdExtraction.extractCorrelationId(message);
    }

    public CorrelationIdExtraction<T> extractionFor(final Class<T> tClass) {
        final Set<Class<?>> allClasses = getAllSuperClassesAndInterfaces(tClass);
        for (final Class<?> aClass : allClasses) {
            if (extractionMap.containsKey(aClass)) {
                return extractionMap.get(aClass);
            }
        }
        throw new NoExtractionKnownException("No Extraction for class " + tClass + " known");
    }

    public void addExtraction(final Class<T> tClass, final CorrelationIdExtraction<T> extraction) {
        extractionMap.put(tClass, extraction);
    }
}
