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

package com.envimate.messageMate.messageFunction.correlationIdExtracting;

import com.envimate.messageMate.messageFunction.correlation.CorrelationId;

/**
 * Interface to define the logic how to extract the {@code CorrelationId} out of a message.
 *
 * @param <T> the type of the message
 */
public interface CorrelationIdExtraction<T> {

    /**
     * Extracts the {@code CorrelationId} out of the message.
     *
     * @param message the given message
     * @return the extracted {@code CorrelationId}
     */
    CorrelationId extractCorrelationId(T message);
}
