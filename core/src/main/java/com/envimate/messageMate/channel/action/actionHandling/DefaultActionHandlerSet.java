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

package com.envimate.messageMate.channel.action.actionHandling;

import com.envimate.messageMate.channel.action.*;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.channel.action.actionHandling.ActionHandlerSet.emptyActionHandlerSet;
import static com.envimate.messageMate.channel.action.actionHandling.CallActionHandler.callActionHandler;
import static com.envimate.messageMate.channel.action.actionHandling.ConsumerActionHandler.consumerActionHandler;
import static com.envimate.messageMate.channel.action.actionHandling.JumpActionHandler.jumpActionHandler;
import static com.envimate.messageMate.channel.action.actionHandling.ReturnActionHandler.returnActionHandler;
import static com.envimate.messageMate.channel.action.actionHandling.SubscriptionActionHandler.subscriptionActionHandler;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class DefaultActionHandlerSet {

    public static <T> ActionHandlerSet<T> defaultActionHandlerSet() {
        final ActionHandlerSet<T> actionHandlerSet = emptyActionHandlerSet();
        actionHandlerSet.registerActionHandler(Consume.class, consumerActionHandler());
        actionHandlerSet.registerActionHandler(Jump.class, jumpActionHandler());
        actionHandlerSet.registerActionHandler(Return.class, returnActionHandler());
        actionHandlerSet.registerActionHandler(Call.class, callActionHandler());
        actionHandlerSet.registerActionHandler(Call.class, callActionHandler());
        actionHandlerSet.registerActionHandler(Subscription.class, subscriptionActionHandler());
        return actionHandlerSet;
    }
}
