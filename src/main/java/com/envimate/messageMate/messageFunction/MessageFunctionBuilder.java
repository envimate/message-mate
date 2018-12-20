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

import com.envimate.messageMate.messageFunction.building.Step2ResponseTypMessageFunctionBuilder;
import com.envimate.messageMate.messageFunction.building.Step4MessageFunctionBuilder;

public class MessageFunctionBuilder {

    public static MessageFunctionBuilder aMessageFunction() {
        return new MessageFunctionBuilder();
    }

    public <R> Step2ResponseTypMessageFunctionBuilder<R> forRequestType(final Class<R> requestClass) {
        return new Step2ResponseTypMessageFunctionBuilderImpl<>(requestClass);
    }

    private static class Step2ResponseTypMessageFunctionBuilderImpl<R> implements Step2ResponseTypMessageFunctionBuilder<R> {
        private final Class<R> requestClass;

        Step2ResponseTypMessageFunctionBuilderImpl(final Class<R> requestClass) {
            this.requestClass = requestClass;
        }

        @Override
        public <S> Step4MessageFunctionBuilder<R, S> forResponseType(final Class<S> responseClass) {
            return new GenerifiedMessageFunctionMessageFunctionBuilder<>(requestClass, responseClass);
        }
    }

}
