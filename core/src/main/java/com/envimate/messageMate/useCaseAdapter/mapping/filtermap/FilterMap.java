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

package com.envimate.messageMate.useCaseAdapter.mapping.filtermap;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilterMap<F1, F2, T> {
    private final List<FilterMapEntry<F1, F2, T>> entries;
    private final T defaultValue;

    static <F1, F2, T> FilterMap<F1, F2, T> filterMap(final List<FilterMapEntry<F1, F2, T>> entries,
                                                      final T defaultValue) {
        ensureNotNull(entries, "entries");
        ensureNotNull(defaultValue, "defaultValue");
        return new FilterMap<>(entries, defaultValue);
    }

    public T get(final F1 condition1,
                 final F2 condition2) {
        return entries.stream()
                .filter(entry -> entry.test(condition1, condition2))
                .map(FilterMapEntry::value)
                .findFirst()
                .orElse(defaultValue);
    }
}
