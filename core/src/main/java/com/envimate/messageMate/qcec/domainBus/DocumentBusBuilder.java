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
import static com.envimate.messageMate.qcec.domainBus.enforcing.NotNullEnforcer.ensureNotNull;
import static com.envimate.messageMate.qcec.eventBus.EventBusFactory.aEventBus;
import static com.envimate.messageMate.qcec.queryresolving.QueryResolverFactory.aQueryResolver;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class DocumentBusBuilder {
    private QueryResolver queryResolver;
    private ConstraintEnforcer constraintEnforcer;
    private EventBus eventBus;

    public static DocumentBusBuilder aDocumentBus() {
        return new DocumentBusBuilder();
    }

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
        return new DocumentBusBuilder()
                .using(queryResolver)
                .using(constraintEnforcer)
                .using(eventBus)
                .build();
    }

    public DocumentBusBuilder using(final QueryResolver queryResolver) {
        this.queryResolver = queryResolver;
        return this;
    }

    public DocumentBusBuilder using(final ConstraintEnforcer constraintEnforcer) {
        this.constraintEnforcer = constraintEnforcer;
        return this;
    }

    public DocumentBusBuilder using(final EventBus eventBus) {
        this.eventBus = eventBus;
        return this;
    }

    public DocumentBusImpl build() {
        ensureNotNull(queryResolver, "DocumentBus needs a QueryResolver.");
        ensureNotNull(constraintEnforcer, "DocumentBus needs a ConstraintEnforcer.");
        ensureNotNull(eventBus, "DocumentBus needs a EventBus.");
        return new DocumentBusImpl(queryResolver, constraintEnforcer, eventBus);
    }
}
