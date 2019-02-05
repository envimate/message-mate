package com.envimate.messageMate.chain;

import com.envimate.messageMate.chain.action.Call;
import com.envimate.messageMate.chain.action.Consume;
import com.envimate.messageMate.chain.action.Jump;
import com.envimate.messageMate.chain.action.Return;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestEnvironmentProperty;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.envimate.messageMate.chain.ChainTestProperties.*;
import static com.envimate.messageMate.chain.ChainTestValidations.*;
import static com.envimate.messageMate.chain.ProcessingFrameHistoryMatcher.aProcessingFrameHistory;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChainValidationBuilder {
    private final TestValidation testValidation;

    private static ChainValidationBuilder aValidation(final TestValidation testValidation) {
        return new ChainValidationBuilder(testValidation);
    }

    static ChainValidationBuilder expectTheMessageToBeConsumed() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    static ChainValidationBuilder expectTheMessageToBeConsumedByTheSecondChain() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    static ChainValidationBuilder expectAllChainsToBeContainedInTheHistory() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final List<Chain<TestMessage>> expectedTraversedChains = getTestPropertyAsListOfChain(testEnvironment, ALL_CHAINS);
            assertResultTraversedAllChainsBasedOnTheirDefaultActions(testEnvironment, expectedTraversedChains);
        });
    }

    static ChainValidationBuilder expectTheMessageToHaveReturnedSuccessfully() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final Chain<TestMessage> firstChain = getTestPropertyAsChain(testEnvironment, SUT);
            final Chain<TestMessage> callTargetChain = getTestPropertyAsChain(testEnvironment, CALL_TARGET_CHAIN);
            final Chain<TestMessage> returningTargetChain = getTestPropertyAsChain(testEnvironment, RETURNING_CHAIN);
            assertMessageFollowedChainsWithActions(testEnvironment, aProcessingFrameHistory()
                    .withAFrameFor(firstChain, Call.class)
                    .withAFrameFor(callTargetChain, Jump.class)
                    .withAFrameFor(returningTargetChain, Return.class)
                    .withAFrameFor(firstChain, Consume.class));
        });
    }

    static ChainValidationBuilder expectTheMessageToHaveReturnedFromAllCalls() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final Chain<TestMessage> initialChain = getTestPropertyAsChain(testEnvironment, SUT);
            final List<Chain<TestMessage>> callTargetLists = getTestPropertyAsListOfChain(testEnvironment, CALL_TARGET_CHAIN);
            final Chain<TestMessage> firstCallTargetChain = callTargetLists.get(0);
            final Chain<TestMessage> secondCallTargetChain = callTargetLists.get(1);
            final Chain<TestMessage> returningTargetChain = getTestPropertyAsChain(testEnvironment, RETURNING_CHAIN);
            assertMessageFollowedChainsWithActions(testEnvironment, aProcessingFrameHistory()
                    .withAFrameFor(initialChain, Call.class)
                    .withAFrameFor(firstCallTargetChain, Call.class)
                    .withAFrameFor(secondCallTargetChain, Jump.class)
                    .withAFrameFor(returningTargetChain, Return.class)
                    .withAFrameFor(firstCallTargetChain, Return.class)
                    .withAFrameFor(initialChain, Consume.class));
        });
    }

    static ChainValidationBuilder expectAExceptionOfType(final Class<?> expectedExceptionClass) {
        return aValidation(testEnvironment -> assertExceptionThrownOfType(testEnvironment, expectedExceptionClass));
    }

    static ChainValidationBuilder expectTheChangedActionToBeExecuted() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    static ChainValidationBuilder expectAllFilterToBeInCorrectOrderInChain() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = getTestPropertyAsListOfFilter(testEnvironment, EXPECTED_RESULT);
            assertFilterAsExpected(testEnvironment, expectedFilter);
        });
    }

    static ChainValidationBuilder expectTheFilterInOrderAsAdded() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    static ChainValidationBuilder expectTheAllRemainingFilter() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = getTestPropertyAsListOfFilter(testEnvironment, EXPECTED_RESULT);
            assertFilterAsExpected(testEnvironment, expectedFilter);
        });
    }

    static ChainValidationBuilder expectTheMetaDataChangePersist() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertMetaDatumOfResultSetAsExpected(testEnvironment);
        });
    }

    private static Chain<TestMessage> getTestPropertyAsChain(final TestEnvironment testEnvironment, final TestEnvironmentProperty property) {
        return getTestPropertyAsChain(testEnvironment, property.name());
    }

    @SuppressWarnings("unchecked")
    private static Chain<TestMessage> getTestPropertyAsChain(final TestEnvironment testEnvironment, final String property) {
        return (Chain<TestMessage>) testEnvironment.getProperty(property);
    }

    @SuppressWarnings("unchecked")
    private static List<Chain<TestMessage>> getTestPropertyAsListOfChain(final TestEnvironment testEnvironment, final String property) {
        return (List<Chain<TestMessage>>) testEnvironment.getProperty(property);
    }

    @SuppressWarnings("unchecked")
    private static List<Filter<ProcessingContext<TestMessage>>> getTestPropertyAsListOfFilter(final TestEnvironment testEnvironment,
                                                                                              final TestEnvironmentProperty property) {
        return (List<Filter<ProcessingContext<TestMessage>>>) testEnvironment.getProperty(property);
    }

    public TestValidation build() {
        return testValidation;
    }
}
