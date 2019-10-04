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

package com.envimate.messageMate.useCases.building;

/**
 * An {@link Exception} indicating, that no serialization rule for a use case's response could be applied.
 *
 * @see <a href="https://github.com/envimate/message-mate#channel">Message Mate Documentation</a>
 */
public final class MissingResponseSerializationException extends RuntimeException {

    private MissingResponseSerializationException(final String message) {
        super(message);
    }

    /**
     * Creates a new {@link MissingResponseSerializationException} for the given message.

     * @param message the exceptions message
     * @return the newly created {@link MissingResponseSerializationException}
     */
    public static MissingResponseSerializationException missingResponseSerializationException(final String message) {
        return new MissingResponseSerializationException(message);
    }
}
