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

package com.envimate.messageMate.messageBus.internal.exception;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionListener;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.List;

public interface ExceptionListenerHandler {

    SubscriptionId register(EventType eventType, MessageBusExceptionListener exceptionListener);

    SubscriptionId register(CorrelationId correlationId, MessageBusExceptionListener exceptionListener);

    List<MessageBusExceptionListener> listenerFor(ProcessingContext<?> processingContext);

    List<MessageBusExceptionListener> allListener();

    void unregister(SubscriptionId subscriptionId);
}
