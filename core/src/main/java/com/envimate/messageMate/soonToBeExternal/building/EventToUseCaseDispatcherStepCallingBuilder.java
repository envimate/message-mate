package com.envimate.messageMate.soonToBeExternal.building;
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

import com.envimate.messageMate.useCaseAdapter.Caller;
import com.envimate.messageMate.soonToBeExternal.usecaseInvoking.UseCaseInvoker;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.envimate.messageMate.soonToBeExternal.usecaseInvoking.ClassBasedUseCaseInvokerImpl.classBasedUseCaseInvoker;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public interface EventToUseCaseDispatcherStepCallingBuilder<USECASE, EVENT> {

    default EventToUseCaseDispatcherStep3Builder calling(final BiFunction<USECASE, EVENT, Object> caller) {
        return callingBy((useCase, event) -> {
            final Object returnValue = caller.apply(useCase, event);
            return ofNullable(returnValue);
        });
    }

    default EventToUseCaseDispatcherStep3Builder callingVoid(final BiConsumer<USECASE, EVENT> caller) {
        return callingBy((usecase, event) -> {
            caller.accept(usecase, event);
            return empty();
        });
    }

    default EventToUseCaseDispatcherStep3Builder callingTheSingleUseCaseMethod() {
        return callingBy((usecase, event) -> {
            final UseCaseInvoker invoker = classBasedUseCaseInvoker(usecase.getClass());
            final Object returnValue = invoker.getInvocationInformation().getMethodInvoker().invoke(usecase, event, asList(event)); // TODO
            return ofNullable(returnValue);
        });
    }

    EventToUseCaseDispatcherStep3Builder callingBy(Caller<USECASE, EVENT> caller);
}
