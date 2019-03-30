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

package com.envimate.messageMate.soonToBeExternal;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.useCaseAdapter.Caller;
import lombok.Getter;

public final class UseCaseCallRequest {
    @Getter
    private final CorrelationId correlationId;
    @Getter
    private Object useCase;
    @Getter
    private Caller caller;
    @Getter
    private Object event;

    private UseCaseCallRequest(final Object useCase,
                               final Object event,
                               final Caller caller,
                               final CorrelationId correlationId) {
        this.useCase = useCase;
        this.event = event;
        this.caller = caller;
        this.correlationId = correlationId;
    }

    public static UseCaseCallRequest useCaseCallRequest(final Object useCase,
                                                        final Object event,
                                                        final Caller caller,
                                                        final CorrelationId correlationId) {
        return new UseCaseCallRequest(useCase, event, caller, correlationId);
    }
}
