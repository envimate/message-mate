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

import java.util.Objects;
import java.util.function.Predicate;

import static com.envimate.messageMate.mapping.MissingSerializationException.missingSerializationException;
import static com.envimate.messageMate.mapping.SerializationFilters.areOfType;
import static java.lang.String.format;

public interface ResponseSerializationStep1Builder {

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize a use case return value
     * to an object if the use case return value matches the provided {@link Predicate filter}.
     *
     * @param filter a {@link Predicate} that returns true if the {@link Mapifier} should be used on the
     *               respective use case response
     * @return the next step in the fluent builder
     */
    ResponseSerializationStep2Builder<Object> serializingResponseObjectsThat(Predicate<Object> filter);

    /**
     * Configures the default {@link Mapifier} that will be used to serialize a use case return value
     * to a  if no {@link Mapifier} configured under
     * {@link ResponseSerializationStep1Builder#serializingResponseObjectsThat(Predicate)},
     * {@link ResponseSerializationStep1Builder#serializingResponseObjectsOfType(Class)}, etc. matches the use case return value.
     *
     * @param mapper a {@link Mapifier}
     * @return the next step in the fluent builder
     */
    ExceptionSerializationStep1Builder serializingObjectsByDefaultUsing(Mapifier<Object> mapper);

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize a use case return value
     * to an object if the use case return value is of the specified type.
     *
     * @param type the class of use case response that will be serialized by the {@link Mapifier}
     * @param <T>  the type of the response
     * @return the next step in the fluent builder
     */
    @SuppressWarnings("unchecked")
    default <T> ResponseSerializationStep2Builder<T> serializingResponseObjectsOfType(final Class<T> type) {
        return mapper ->
                serializingResponseObjectsThat(areOfType(type))
                        .using((Mapifier<Object>) mapper);
    }

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize a return value of type
     * {@code null}.
     *
     * @param <T> the type of the follow up builder
     * @return the next step in the fluent builder
     */
    @SuppressWarnings("unchecked")
    default <T> ResponseSerializationStep2Builder<T> serializingResponseObjectsOfTypeVoid() {
        return mapper ->
                serializingResponseObjectsThat(Objects::isNull)
                        .using((Mapifier<Object>) mapper);
    }

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize a use case request
     * to an object if the request matches the provided {@link Predicate filter}.
     *
     * @param filter a {@link Predicate} that returns true if the {@link Mapifier} should be used on the
     *               respective use case request
     * @return the next step in the fluent builder
     */
    default ResponseSerializationStep2Builder<Object> serializingUseCaseRequestThat(final Predicate<Object> filter) {
        return serializingResponseObjectsThat(filter);
    }

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize a use case request
     * if the request is {@code null}.
     *
     * @return the next step in the fluent builder
     */
    default ResponseSerializationStep2Builder<Object> serializinguseCaseRequestsOfTypeVoid() {
        return serializingResponseObjectsOfTypeVoid();
    }

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize a use case request
     * to an object if the use case return value is of the specified type.
     *
     * @param type the type of use case requests that will be serialized by the {@link Mapifier}
     * @param <T> the type of the use case request
     * @return the next step in the fluent builder
     */
    default <T> ResponseSerializationStep2Builder<T> serializingUseCaseRequestOfType(final Class<T> type) {
        return serializingResponseObjectsOfType(type);
    }

    /**
     * Configures  to throw an exception if no {@link Mapifier} configured under
     * {@link ResponseSerializationStep1Builder#serializingResponseObjectsThat(Predicate)},
     * {@link ResponseSerializationStep1Builder#serializingResponseObjectsOfType(Class)}, etc. matches the use case return value.
     *
     * @return the next step in the fluent builder
     */
    default ExceptionSerializationStep1Builder throwingAnExceptionByDefaultIfNoResponseMappingCanBeApplied() {
        return serializingObjectsByDefaultUsing(object -> {
            final String message = format("No serialization found for object %s.", object);
            throw missingSerializationException(message);
        });
    }
}
