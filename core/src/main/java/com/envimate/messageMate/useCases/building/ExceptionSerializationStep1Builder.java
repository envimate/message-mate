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

package com.envimate.messageMate.useCases.building;

import com.envimate.messageMate.mapping.Mapifier;

import java.util.function.Predicate;

import static com.envimate.messageMate.mapping.ExceptionMapifier.defaultExceptionMapifier;
import static com.envimate.messageMate.mapping.MissingSerializationException.responseMapperException;
import static com.envimate.messageMate.mapping.SerializationFilters.areOfType;

public interface ExceptionSerializationStep1Builder {

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize a use case return value
     * to a if the use case return value matches the provided {@link Predicate filter}.
     *
     * @param filter a {@link Predicate} that returns true if the {@link Mapifier} should be used on the
     *               respective use case return value
     * @return the next step in the fluent builder
     */
    ExceptionSerializationStep2Builder<Exception> serializingExceptionsThat(Predicate<Exception> filter);

    /**
     * Configures the default {@link Mapifier} that will be used to serialize a use case return value
     * to a  if no {@link Mapifier} configured under
     * {@link ExceptionSerializationStep1Builder#serializingResponseObjectsThat(Predicate)},
     * {@link ExceptionSerializationStep1Builder#serializingResponseObjectsOfType(Class)}, etc. matches the use case return value.
     *
     * @param mapper a {@link Mapifier}
     * @return the next step in the fluent builder
     */
    BuilderStepBuilder serializingExceptionsByDefaultUsing(Mapifier<Exception> mapper);

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize a use case return value
     * to a  if the use case return value is of the specified type.
     *
     * @param type the type of use case return values that will be serialized by the {@link Mapifier}
     * @return the next step in the fluent builder
     */
    @SuppressWarnings("unchecked")
    default <T> ExceptionSerializationStep2Builder<T> serializingExceptionsOfType(final Class<T> type) {
        return mapper ->
                serializingExceptionsThat(areOfType(type))
                        .using((Mapifier<Exception>) mapper);
    }

    /**
     * Configures  to throw an exception if no {@link Mapifier} configured under
     * {@link ExceptionSerializationStep1Builder#serializingResponseObjectsThat(Predicate)},
     * {@link ExceptionSerializationStep1Builder#serializingResponseObjectsOfType(Class)}, etc. matches the use case return value.
     *
     * @return the next step in the fluent builder
     */
    default BuilderStepBuilder throwingAnExceptionIfNoExceptionMappingCanBeFound() {
        return serializingExceptionsByDefaultUsing(object -> {
            throw responseMapperException("No response mapper found for exception of class %s.", object.getClass());
        });
    }

    default BuilderStepBuilder puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault() {
        return serializingExceptionsByDefaultUsing(defaultExceptionMapifier());
    }
}
