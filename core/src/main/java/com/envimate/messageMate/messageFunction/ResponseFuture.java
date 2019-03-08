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

package com.envimate.messageMate.messageFunction;

import com.envimate.messageMate.messageFunction.followup.FollowUpAction;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

/**
 * For each request, the related {@code ResponseFuture} provides methods, to query or wait on the result.
 *
 * @param <T> the type of responses
 * @see <a href="https://github.com/envimate/message-mate#responsefuture">Message Mate Documentation</a>
 */

//Check, that cancel fulfills contract and that isCancelled only true, if cancelled before fulfilled.
//Check, that cancel + response does not call FollowUpAction
//Check then on cancelled future -> CancellationException
public interface ResponseFuture<T> extends Future<T> {

    /**
     * Returns {@code true} if the future was fulfilled with an success response, {@code false} otherwise.
     *
     * @return {@code true} if success, {@code false} for an error response or an exception
     */
    boolean wasSuccessful();

    /**
     * Adds a {@code FollowUpAction}, that gets executed, once the Future is fulfilled.
     *
     * @param followUpAction the {@code FollowUpAction} to execute
     * @throws UnsupportedOperationException if one {@code FollowUpAction} has already been set
     * @throws CancellationException         if the future has already been cancelled
     */
    void then(FollowUpAction<T> followUpAction);
}
