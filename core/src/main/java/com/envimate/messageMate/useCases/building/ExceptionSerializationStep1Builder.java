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

package com.envimate.messageMate.useCases.building;

import com.envimate.messageMate.mapping.Mapifier;

import java.util.Map;
import java.util.function.Predicate;

import static com.envimate.messageMate.mapping.ExceptionMapifier.defaultExceptionMapifier;
import static com.envimate.messageMate.mapping.MissingSerializationException.missingSerializationException;
import static com.envimate.messageMate.mapping.SerializationFilters.areOfType;
import static java.lang.String.format;

public interface ExceptionSerializationStep1Builder {

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize an exception thrown by a use
     * case to a {@link Map} if the exception matches the provided {@link Predicate filter}.
     *
     * @param filter a {@link Predicate} that returns true if the {@link Mapifier} should be used on the
     *               respective exception
     * @return the next step in the fluent builder
     */
    ExceptionSerializationStep2Builder<Exception> serializingExceptionsThat(Predicate<Exception> filter);

    /**
     * Configures the default {@link Mapifier} that will be used to serialize an exception to a {@link Map} if no
     * {@link Mapifier} configured under {@link ExceptionSerializationStep1Builder#serializingExceptionsThat(Predicate)},
     * {@link ExceptionSerializationStep1Builder#serializingExceptionsOfType(Class)}, etc. matches the exception.
     *
     * @param mapper a {@link Mapifier}
     * @return the next step in the fluent builder
     */
    FinalStepBuilder serializingExceptionsByDefaultUsing(Mapifier<Exception> mapper);

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize an exception to a {@link Map}
     * if the exception is of the specified type.
     *
     * @param type the class of exception that will be serialized by the {@link Mapifier}
     * @param <T> the type of exception
     * @return the next step in the fluent builder
     */
    @SuppressWarnings("unchecked")
    default <T> ExceptionSerializationStep2Builder<T> serializingExceptionsOfType(final Class<T> type) {
        return mapper ->
                serializingExceptionsThat(areOfType(type))
                        .using((Mapifier<Exception>) mapper);
    }

    /**
     * Configures to throw an exception if no {@link Mapifier} configured under
     * {@link ExceptionSerializationStep1Builder#serializingExceptionsThat(Predicate)},
     * {@link ExceptionSerializationStep1Builder#serializingExceptionsOfType(Class)}, etc. matches exception.
     *
     * @return the next step in the fluent builder
     */
    default FinalStepBuilder throwingAnExceptionIfNoExceptionMappingCanBeFound() {
        return serializingExceptionsByDefaultUsing(object -> {
            final Class<? extends Exception> objectClass = object.getClass();
            final String message = format("No response mapper found for exception of class %s.", objectClass);
            throw missingSerializationException(message);
        });
    }

    /**
     * Configures the default {@link Mapifier} to take all exceptions, that have not been matched by a previous rule and
     * serialize them into a {@link Map} by taking the {@link Exception} object and storing it under
     * {@value com.envimate.messageMate.mapping.ExceptionMapifier#DEFAULT_EXCEPTION_MAPIFIER_KEY} key.
     *
     * @return the next step in the fluent builder interface
     */
    default FinalStepBuilder puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault() {
        return serializingExceptionsByDefaultUsing(defaultExceptionMapifier());
    }
}
