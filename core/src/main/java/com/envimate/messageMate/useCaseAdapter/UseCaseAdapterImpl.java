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

package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.useCaseAdapter.mapping.RequestDeserializer;
import com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.UseCaseCallingInformation;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static com.envimate.messageMate.useCaseAdapter.UseCaseRequestExecutingSubscriber.useCaseRequestExecutingSubscriber;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
@SuppressWarnings("rawtypes") //TODO: remove
public final class UseCaseAdapterImpl implements UseCaseAdapter {
    private final List<UseCaseCallingInformation> useCaseCallingInformations;
    private final UseCaseInstantiator useCaseInstantiator;
    private final RequestDeserializer requestDeserializer;

    public static UseCaseAdapter useCaseAdapterImpl(final List<UseCaseCallingInformation> useCaseCallingInformations,
                                                    final UseCaseInstantiator useCaseInstantiator,
                                                    final RequestDeserializer requestDeserializer) {
        ensureNotNull(useCaseCallingInformations, "useCaseCallingInformations");
        ensureNotNull(useCaseInstantiator, "useCaseInstantiator");
        ensureNotNull(requestDeserializer, "requestDeserializer");
        return new UseCaseAdapterImpl(useCaseCallingInformations, useCaseInstantiator, requestDeserializer);
    }

    @Override
    public void attachTo(final MessageBus messageBus) {
        useCaseCallingInformations.forEach(callingInformation -> {
            final Subscriber<ProcessingContext<Object>> useCaseRequestSubscriber =
                    useCaseRequestExecutingSubscriber(messageBus, callingInformation, useCaseInstantiator, requestDeserializer);
            messageBus.subscribeRaw(callingInformation.getEventType(), useCaseRequestSubscriber);
        });
    }
}
