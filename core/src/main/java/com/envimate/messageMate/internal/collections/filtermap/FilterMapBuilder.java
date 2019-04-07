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

package com.envimate.messageMate.internal.collections.filtermap;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;

import static com.envimate.messageMate.internal.collections.filtermap.FilterMap.filterMap;
import static com.envimate.messageMate.internal.collections.filtermap.FilterMapEntry.filterMapEntry;
import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilterMapBuilder<F1, F2, T> {
    private final List<FilterMapEntry<F1, F2, T>> entries;
    private T defaultValue;

    public static <F1, F2, T> FilterMapBuilder<F1, F2, T> filterMapBuilder() {
        return new FilterMapBuilder<>(new CopyOnWriteArrayList<>());
    }

    public FilterMapBuilder<F1, F2, T> put(final BiPredicate<F1, F2> filter, final T value) {
        final FilterMapEntry<F1, F2, T> entry = filterMapEntry(filter, value);
        entries.add(entry);
        return this;
    }

    public void setDefaultValue(final T defaultValue) {
        ensureNotNull(defaultValue, "defaultValue");
        this.defaultValue = defaultValue;
    }

    public FilterMap<F1, F2, T> build() {
        return filterMap(entries, defaultValue);
    }
}
