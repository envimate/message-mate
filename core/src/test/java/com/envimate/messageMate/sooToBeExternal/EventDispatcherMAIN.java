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

package com.envimate.messageMate.sooToBeExternal;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.soonToBeExternal.*;

import java.util.ArrayList;
import java.util.List;

public class EventDispatcherMAIN {

    public static void main(String[] args) {
        final MessageBus messageBus = MessageBusBuilder.aMessageBus().build();
        final EventToUseCaseDispatcher useCaseDispatcher = EventToUseCaseDispatcherBuilder.anEventToUseCaseDispatcher()
                .invokingUseCase(TestUseCase.class)
                //.invokingUseCase(new TestUseCase())
                .usingMessageBus(messageBus)
                .build();

        messageBus.add((message, filterActions) -> {
            if (message instanceof UseCaseCallRequest) {
                final List<Object> parameter = ((UseCaseCallRequest) message).getParameter();
                parameter.add("hello");
            }
            filterActions.pass(message);
        });

        final EventFactory eventFactory = useCaseDispatcher.eventFactoryFor(TestEvent.class);
        final Object event = eventFactory.createEvent(new ArrayList<>());
        final UseCaseResponseFuture useCaseResponseFuture = useCaseDispatcher.dispatch(event);
        useCaseResponseFuture.then((response, wasSuccessful, exception) -> {
            if (exception != null) {
                throw new RuntimeException(exception);
            } else {
                System.out.println(response);
            }
        });
    }

}