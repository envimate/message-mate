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

package com.envimate.messageMate.messageFunction.building;

/**
 * Interface defining the step of building a {@code MessageFunction}, in which the response superclass is set.
 *
 * @param <R> the supertype of requests
 */
public interface Step2ResponseTypMessageFunctionBuilder<R> {

    /**
     * Defines the super class of all responses.
     *
     * @param responseClass the superclass of all responses
     * @param <S>           the supertype of all responses
     * @return a {@code MessageFunctionBuilder} instance expecting the next configuration step
     */
    <S> Step3MessageFunctionBuilder<R, S> forResponseType(Class<S> responseClass);
}
