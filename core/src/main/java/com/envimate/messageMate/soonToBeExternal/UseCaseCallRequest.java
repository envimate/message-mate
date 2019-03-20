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

import com.envimate.messageMate.messageFunction.correlation.CorrelationId;
import com.envimate.messageMate.soonToBeExternal.methodInvoking.UseCaseMethodInvoker;
import lombok.Getter;

import java.util.List;

public final class UseCaseCallRequest {
    @Getter
    private final List<Object> parameter;
    @Getter
    private final CorrelationId correlationId;
    @Getter
    private Object useCase;
    @Getter
    private UseCaseMethodInvoker methodInvoker;
    @Getter
    private Object event;

    private UseCaseCallRequest(final Object useCase, final UseCaseMethodInvoker methodInvoker, final Object event,
                               final List<Object> parameter, final CorrelationId correlationId) {
        this.parameter = parameter;
        this.correlationId = correlationId;
        this.useCase = useCase;
        this.methodInvoker = methodInvoker;
        this.event = event;
    }

    public static UseCaseCallRequest useCaseCallRequest(final Object useCase, final UseCaseMethodInvoker methodInvoker,
                                                        final Object event, final List<Object> parameter,
                                                        final CorrelationId correlationId) {
        return new UseCaseCallRequest(useCase, methodInvoker, event, parameter, correlationId);
    }
}
