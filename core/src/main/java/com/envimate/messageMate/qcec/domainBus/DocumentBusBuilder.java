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

package com.envimate.messageMate.qcec.domainBus;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.constraintEnforcing.ConstraintEnforcer;
import com.envimate.messageMate.qcec.eventBus.EventBus;
import com.envimate.messageMate.qcec.queryresolving.QueryResolver;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.MessageBusType.SYNCHRONOUS;
import static com.envimate.messageMate.qcec.constraintEnforcing.ConstraintEnforcerFactory.aConstraintEnforcer;
import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static com.envimate.messageMate.qcec.eventBus.EventBusFactory.aEventBus;
import static com.envimate.messageMate.qcec.queryresolving.QueryResolverFactory.aQueryResolver;
import static lombok.AccessLevel.PRIVATE;

/**
 * Builder class to create a new {@code DocumentBus}.
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class DocumentBusBuilder {
    private QueryResolver queryResolver;
    private ConstraintEnforcer constraintEnforcer;
    private EventBus eventBus;

    /**
     * Creates a new {@code DocumentBus} based on a synchronous {@code MessageBus}.
     *
     * @return a new {@code DocumentBus}
     */
    public static DocumentBus aDefaultDocumentBus() {
        final MessageBus queryMessageBus = aMessageBus()
                .forType(SYNCHRONOUS)
                .build();
        final MessageBus constraintMessageBus = aMessageBus()
                .forType(SYNCHRONOUS)
                .build();
        final MessageBus eventMessageBus = aMessageBus()
                .forType(SYNCHRONOUS)
                .build();
        final QueryResolver queryResolver = aQueryResolver(queryMessageBus);
        final ConstraintEnforcer constraintEnforcer = aConstraintEnforcer(constraintMessageBus);
        final EventBus eventBus = aEventBus(eventMessageBus);
        return aDocumentBus()
                .using(queryResolver)
                .using(constraintEnforcer)
                .using(eventBus)
                .build();
    }

    /**
     * Factory method to create a new {@code DocumentBusBuilder}.
     *
     * @return newly created {@code DocumentBusBuilder}
     */
    public static DocumentBusBuilder aDocumentBus() {
        return new DocumentBusBuilder();
    }

    /**
     * Sets the {@code QueryResolver} to be used for the {@code DocumentBus}.
     *
     * @param queryResolver the {@code QueryResolver} to be used
     * @return the same {@code DocumentBusBuilder} instance the method was called one
     */
    public DocumentBusBuilder using(final QueryResolver queryResolver) {
        this.queryResolver = queryResolver;
        return this;
    }

    /**
     * Sets the {@code ConstraintEnforcer} to be used for the {@code DocumentBus}.
     *
     * @param constraintEnforcer the {@code ConstraintEnforcer} to be used
     * @return the same {@code DocumentBusBuilder} instance the method was called one
     */
    public DocumentBusBuilder using(final ConstraintEnforcer constraintEnforcer) {
        this.constraintEnforcer = constraintEnforcer;
        return this;
    }

    /**
     * Sets the {@code EventBus} to be used for the {@code DocumentBus}.
     *
     * @param eventBus the {@code EventBus} to be used
     * @return the same {@code DocumentBusBuilder} instance the method was called one
     */
    public DocumentBusBuilder using(final EventBus eventBus) {
        this.eventBus = eventBus;
        return this;
    }

    /**
     * Creates the configured {@code DocumentBus}.
     *
     * @return newly created {@code DocumentBus}
     */
    public DocumentBusImpl build() {
        ensureNotNull(queryResolver, "query resolver");
        ensureNotNull(constraintEnforcer, "constraint enforcer");
        ensureNotNull(eventBus, "event bus");
        return new DocumentBusImpl(queryResolver, constraintEnforcer, eventBus);
    }
}
