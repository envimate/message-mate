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

package com.envimate.messageMate.correlation;

import com.envimate.messageMate.internal.enforcing.InvalidInputException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.UUID;

import static com.envimate.messageMate.internal.enforcing.StringValidator.cleaned;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CorrelationId {
    private final UUID value;

    public static CorrelationId fromString(final String value) {
        final String cleaned = cleaned(value);
        try {
            return new CorrelationId(UUID.fromString(cleaned));
        } catch (final IllegalArgumentException e) {
            throw new InvalidInputException("Must be a valid uuid.");
        }
    }

    public static CorrelationId newUniqueId() {
        return new CorrelationId(UUID.randomUUID());
    }

    public String stringValue() {
        return this.value.toString();
    }
}
