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

package com.envimate.messageMate.shared.utils;

import com.envimate.messageMate.channel.givenWhenThen.FilterPosition;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.FilterTestActions;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.SimplifiedFilterTestActions;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.TestFilter.*;
import static com.envimate.messageMate.shared.properties.SharedTestProperties.*;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class FilterTestUtils {

    public static List<Filter<ProcessingContext<TestMessage>>> addSeveralNoopFilter(final FilterTestActions filterTestActions,
                                                                                    final int[] positions,
                                                                                    final FilterPosition filterPosition) {
        final List<Filter<ProcessingContext<TestMessage>>> filters = new LinkedList<>();
        for (final int position : positions) {
            final Filter<ProcessingContext<TestMessage>> filter = addANoopFilterAtPosition(filterTestActions, filterPosition,
                    position);
            filters.add(position, filter);
        }
        return filters;
    }

    public static Filter<ProcessingContext<TestMessage>> addANoopFilterAtPosition(final FilterTestActions filterTestActions,
                                                                                  final FilterPosition filterPosition,
                                                                                  final int position) {
        final Filter<ProcessingContext<TestMessage>> filter = (processingContext, filterActions) -> {
            filterActions.pass(processingContext);
        };
        filterTestActions.addFilter(filter, filterPosition, position);
        return filter;
    }

    public static List<?> queryFilter(final FilterTestActions filterTestActions, final TestEnvironment testEnvironment) {
        final FilterPosition filterPosition = getFilterPositionOrNull(testEnvironment);
        return filterTestActions.getFilter(filterPosition);
    }

    public static List<?> queryFilter(final FilterTestActions filterTestActions, final FilterPosition filterPosition) {
        return filterTestActions.getFilter(filterPosition);
    }

    @SuppressWarnings("unchecked")
    public static void addAFilterThatChangesTheContentOfEveryMessage(final SimplifiedFilterTestActions filterTestActions,
                                                                     final TestEnvironment testEnvironment) {

        testEnvironment.setProperty(EXPECTED_CHANGED_CONTENT, CHANGED_CONTENT);
        final Filter<?> filter = aContentChangingFilter();
        filterTestActions.addNotRawFilter((Filter<TestMessage>) filter);
    }

    public static void addFilterThatBlocksMessages(final FilterTestActions filterTestActions,
                                                   final FilterPosition filterPosition) {
        final Filter<ProcessingContext<TestMessage>> filter = aMessageDroppingFilter();
        filterTestActions.addFilter(filter, filterPosition);
    }

    public static void addFilterThatForgetsMessages(final FilterTestActions filterTestActions,
                                                    final FilterPosition filterPosition) {
        final Filter<ProcessingContext<TestMessage>> filter = aMessageFilterThatDoesNotCallAnyMethod();
        filterTestActions.addFilter(filter, filterPosition);
    }

    public static void addFilterThatThrowsException(final FilterTestActions filterTestActions,
                                                    final FilterPosition filterPosition) {
        final Filter<ProcessingContext<TestMessage>> filter = anErrorThrowingFilter();
        filterTestActions.addFilter(filter, filterPosition);
    }

    public static void removeAFilter(final FilterTestActions filterTestActions, final TestEnvironment testEnvironment) {
        final FilterPosition filterPosition = getFilterPositionOrNull(testEnvironment);
        final List<?> filters = filterTestActions.getFilter(filterPosition);
        final int indexToRemove = (int) (Math.random() * filters.size());
        final Filter<?> removedFilter = (Filter<?>) filters.get(indexToRemove);
        filterTestActions.removeFilter(removedFilter, filterPosition);
        final List<?> expectedFilter = testEnvironment.getPropertyAsType(EXPECTED_FILTER, List.class);
        expectedFilter.remove(removedFilter);
    }

    private static FilterPosition getFilterPositionOrNull(final TestEnvironment testEnvironment) {
        if (testEnvironment.has(FILTER_POSITION)) {
            return testEnvironment.getPropertyAsType(FILTER_POSITION, FilterPosition.class);
        } else {
            return null;
        }
    }
}
