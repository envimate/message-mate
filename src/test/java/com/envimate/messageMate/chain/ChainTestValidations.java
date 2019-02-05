package com.envimate.messageMate.chain;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestEnvironmentProperty;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static com.envimate.messageMate.chain.ChainTestActions.getFilterOf;
import static com.envimate.messageMate.chain.ChainTestProperties.MODIFIED_META_DATUM;
import static com.envimate.messageMate.chain.ProcessingFrameHistoryMatcher.aProcessingFrameHistory;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

@RequiredArgsConstructor(access = PRIVATE)
final class ChainTestValidations {

    static void assertResultAndExpectedResultAreEqual(final TestEnvironment testEnvironment) {
        final Object result = testEnvironment.getProperty(RESULT);
        final Object expectedResult = testEnvironment.getProperty(EXPECTED_RESULT);
        assertThat(result, equalTo(expectedResult));
    }

    static void assertNoExceptionThrown(final TestEnvironment testEnvironment) {
        if (testEnvironment.has(EXCEPTION)) {
            final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            fail("Unexpected exception", exception);
        }
    }

    static void assertExceptionThrownOfType(final TestEnvironment testEnvironment, final Class<?> expectedExceptionClass) {
        final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
        assertThat(exception.getClass(), equalTo(expectedExceptionClass));
    }

    static void assertResultTraversedAllChainsBasedOnTheirDefaultActions(final TestEnvironment testEnvironment,
                                                                         final List<Chain<TestMessage>> expectedTraversedChains) {
        final ProcessingContext<TestMessage> result = getTestPropertyAsProcessingContext(testEnvironment, RESULT);
        final ProcessingFrameHistoryMatcher processingFrameHistoryMatcher = aProcessingFrameHistory();
        expectedTraversedChains.forEach(chain -> processingFrameHistoryMatcher.withAFrameFor(chain, chain.getDefaultAction()));
        processingFrameHistoryMatcher.assertCorrect(result);
    }

    static void assertMessageFollowedChainsWithActions(final TestEnvironment testEnvironment,
                                                       final ProcessingFrameHistoryMatcher processingFrameHistoryMatcher) {
        final ProcessingContext<TestMessage> result = getTestPropertyAsProcessingContext(testEnvironment, RESULT);
        processingFrameHistoryMatcher.assertCorrect(result);
    }


    static void assertFilterAsExpected(final TestEnvironment testEnvironment, final List<Filter<ProcessingContext<TestMessage>>> expectedFilter) {
        final ChainChannel channel = testEnvironment.getPropertyAsType(ChainTestProperties.CHANNEL, ChainChannel.class);
        final Chain<TestMessage> chain = getTestPropertyAsChain(testEnvironment, SUT);
        final List<Filter<ProcessingContext<TestMessage>>> actualFilter = getFilterOf(chain, channel);
        assertThat(actualFilter, equalTo(expectedFilter));
    }

    static void assertMetaDatumOfResultSetAsExpected(final TestEnvironment testEnvironment) {
        final String expectedMetaDatum = testEnvironment.getPropertyAsType(EXPECTED_RESULT, String.class);
        final ProcessingContext<TestMessage> result = getTestPropertyAsProcessingContext(testEnvironment, RESULT);
        final Map<Object, Object> contextMetaData = result.getContextMetaData();
        final Object actualMetaDatum = contextMetaData.get(MODIFIED_META_DATUM);
        assertThat(actualMetaDatum, equalTo(expectedMetaDatum));
    }

    @SuppressWarnings("unchecked")
    private static Chain<TestMessage> getTestPropertyAsChain(final TestEnvironment testEnvironment, final TestEnvironmentProperty property) {
        return (Chain<TestMessage>) testEnvironment.getProperty(property);
    }

    @SuppressWarnings("unchecked")
    private static ProcessingContext<TestMessage> getTestPropertyAsProcessingContext(final TestEnvironment testEnvironment,
                                                                                     final TestEnvironmentProperty property) {
        return (ProcessingContext<TestMessage>) testEnvironment.getProperty(property);
    }

}
