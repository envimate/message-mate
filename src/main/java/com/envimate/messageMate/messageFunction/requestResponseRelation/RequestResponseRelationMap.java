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

package com.envimate.messageMate.messageFunction.requestResponseRelation;

import com.envimate.messageMate.messageFunction.responseMatching.ResponseMatcher;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public interface RequestResponseRelationMap<R, S> {
    <T extends R> List<ResponseMatcher> responseMatchers(T request);

    void addSuccessResponse(Class<R> requestClass, Class<S> responseClass);

    void addErrorResponse(Class<R> requestClass, Class<S> responseClass);

    void addGeneralErrorResponse(Class<?> responseClass);

    <T> void addGeneralErrorResponse(Class<T> responseClass, BiFunction<T, R, Boolean> conditional);

    Set<Class<?>> getAllPossibleResponseClasses();
}
