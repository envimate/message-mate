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

package com.envimate.messageMate.chain.action.actionHandling;

import com.envimate.messageMate.chain.action.Action;

import java.util.HashMap;
import java.util.Map;

public final class ActionHandlerSet<T> {

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends Action>, ActionHandler<? extends Action<T>, T>> actionHandlerMap;

    @SuppressWarnings("rawtypes")
    private ActionHandlerSet(final Map<Class<? extends Action>, ActionHandler<? extends Action<T>, T>> actionHandlerMap) {
        this.actionHandlerMap = actionHandlerMap;
    }

    @SuppressWarnings("rawtypes")
    public static <T> ActionHandlerSet<T> actionHandlerSet(
            final Map<Class<? extends Action>, ActionHandler<? extends Action<T>, T>> handlerMap) {
        return new ActionHandlerSet<>(handlerMap);
    }

    @SuppressWarnings("rawtypes")
    public static <T> ActionHandlerSet<T> emptyActionHandlerSet() {
        final Map<Class<? extends Action>, ActionHandler<? extends Action<T>, T>> map = new HashMap<>();
        return new ActionHandlerSet<>(map);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ActionHandler<Action<T>, T> getActionHandlerFor(final Action<T> action) {
        final Class<? extends Action> actionClass = action.getClass();
        final ActionHandler<?, T> actionHandler = actionHandlerMap.get(actionClass);
        if (actionHandler != null) {
            return (ActionHandler<Action<T>, T>) actionHandler;
        } else {
            throw new NotHandlerForUnknownActionException(action);
        }
    }

    @SuppressWarnings("rawtypes")
    public void registerActionHandler(final Class<? extends Action> actionClass,
                                      final ActionHandler<? extends Action<T>, T> actionHandler) {
        actionHandlerMap.put(actionClass, actionHandler);
    }
}
