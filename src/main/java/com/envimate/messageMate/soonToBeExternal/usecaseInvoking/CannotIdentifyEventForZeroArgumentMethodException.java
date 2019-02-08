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

package com.envimate.messageMate.soonToBeExternal.usecaseInvoking;

import java.lang.reflect.Method;

import static java.lang.String.format;

public final class CannotIdentifyEventForZeroArgumentMethodException extends RuntimeException {

    private CannotIdentifyEventForZeroArgumentMethodException(final String message) {
        super(message);
    }

    public static CannotIdentifyEventForZeroArgumentMethodException exceptionThatNoEventCanBeIdentifiedForMethodWithoutParameter(
            final Method method) {
        final String message = format("Cannot use first argument as event type for method %s when method has no arguments.",
                method);
        return new CannotIdentifyEventForZeroArgumentMethodException(message);
    }
}
