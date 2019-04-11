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

package com.envimate.messageMate.qcec.domainBus.internal.answer;

import com.envimate.messageMate.qcec.domainBus.building.AnswerStep1Builder;
import com.envimate.messageMate.qcec.domainBus.building.AnswerStep2Builder;
import com.envimate.messageMate.qcec.queryresolving.Query;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.envimate.messageMate.qcec.domainBus.internal.answer.ConstraintAnswerImpl.constraintAnswer;
import static com.envimate.messageMate.qcec.domainBus.internal.answer.EventAnswerImpl.eventAnswer;
import static com.envimate.messageMate.qcec.domainBus.internal.answer.QueryAnswerImpl.queryAnswer;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class AnswerBuilder<T> implements AnswerStep1Builder<T>, AnswerStep2Builder<T> {
    private final List<Predicate<T>> onlyIfConditions = new ArrayList<>();
    private final List<TerminationCondition<?>> terminationConditions = new ArrayList<>();
    private final AnswerCreation<T> answerCreation;
    private final AnswerRegister answerRegister;

    public static <T extends Query<?>> AnswerStep1Builder<T> anQueryAnswerForClass(final Class<T> queryClass,
                                                                                   final AnswerRegister answerRegister) {
        final AnswerCreation<T> answerCreation = (responseCondition, responseConsumer, terminationConditions) -> {
            return queryAnswer(queryClass, responseCondition, responseConsumer, terminationConditions);
        };
        return new AnswerBuilder<>(answerCreation, answerRegister);
    }

    public static <T> AnswerStep1Builder<T> anConstraintAnswerForClass(final Class<T> constraintClass,
                                                                       final AnswerRegister answerRegister) {
        final AnswerCreation<T> answerCreation = (responseCondition, responseConsumer, terminationConditions) -> {
            return constraintAnswer(constraintClass, responseCondition, responseConsumer, terminationConditions);
        };
        return new AnswerBuilder<>(answerCreation, answerRegister);
    }

    public static <T> AnswerStep1Builder<T> anEventAnswerForClass(final Class<T> queryClass,
                                                                  final AnswerRegister answerRegister) {
        final AnswerCreation<T> answerCreation = (responseCondition, responseConsumer, terminationConditions) -> {
            return eventAnswer(queryClass, responseCondition, responseConsumer, terminationConditions);
        };
        return new AnswerBuilder<>(answerCreation, answerRegister);
    }

    @Override
    public AnswerStep1Builder<T> onlyIf(final Predicate<T> condition) {
        this.onlyIfConditions.add(condition);
        return this;
    }

    @Override
    public AnswerStep2Builder<T> until(final Class<?> eventClass) {
        return until(eventClass, o -> o.equals(eventClass));
    }

    @Override
    public <R> AnswerStep2Builder<T> until(final Class<R> eventClass, final Predicate<R> condition) {
        this.terminationConditions.add(TerminationCondition.terminationCondition(eventClass, condition));
        return this;
    }

    @Override
    public SubscriptionId using(final Consumer<T> consumer) {
        final Predicate<T> combinedResponseCondition = t -> {
            if (onlyIfConditions.isEmpty()) {
                return true;
            } else {
                for (final Predicate<T> condition : onlyIfConditions) {
                    if (condition.test(t)) {
                        return true;
                    }
                }
                return false;
            }
        };
        return answerRegister.submit(answerCreation.create(combinedResponseCondition, consumer, terminationConditions));
    }

    private interface AnswerCreation<T> {
        Answer create(Predicate<T> respCondition, Consumer<T> responseConsumer, List<TerminationCondition<?>> termConditions);
    }
}
