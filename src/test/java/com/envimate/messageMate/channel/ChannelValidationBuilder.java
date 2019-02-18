package com.envimate.messageMate.channel;

import com.envimate.messageMate.channel.action.Call;
import com.envimate.messageMate.channel.action.Consume;
import com.envimate.messageMate.channel.action.Jump;
import com.envimate.messageMate.channel.action.Return;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestEnvironmentProperty;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.envimate.messageMate.channel.ChannelTestProperties.*;
import static com.envimate.messageMate.channel.ChannelTestValidations.*;
import static com.envimate.messageMate.channel.ProcessingFrameHistoryMatcher.aProcessingFrameHistory;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.*;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelValidationBuilder {
    private final TestValidation testValidation;

    private static ChannelValidationBuilder aValidation(final TestValidation testValidation) {
        return new ChannelValidationBuilder(testValidation);
    }

    static ChannelValidationBuilder expectTheMessageToBeConsumed() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    static ChannelValidationBuilder expectTheMessageToBeConsumedByTheSecondChannel() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    static ChannelValidationBuilder expectAllChannelsToBeContainedInTheHistory() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final List<Channel<TestMessage>> expectedTraversedChannels = getTestPropertyAsListOfChannel(testEnvironment, ALL_CHANNELS);
            assertResultTraversedAllChannelBasedOnTheirDefaultActions(testEnvironment, expectedTraversedChannels);
        });
    }

    static ChannelValidationBuilder expectTheMessageToHaveReturnedSuccessfully() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final Channel<TestMessage> firstChannel = getTestPropertyAsChannel(testEnvironment, SUT);
            final Channel<TestMessage> callTargetChannel = getTestPropertyAsChannel(testEnvironment, CALL_TARGET_CHANNEL);
            final Channel<TestMessage> returningTargetChannel = getTestPropertyAsChannel(testEnvironment, RETURNING_CHANNEL);
            assertMessageFollowedChannelWithActions(testEnvironment, aProcessingFrameHistory()
                    .withAFrameFor(firstChannel, Call.class)
                    .withAFrameFor(callTargetChannel, Jump.class)
                    .withAFrameFor(returningTargetChannel, Return.class)
                    .withAFrameFor(firstChannel, Consume.class));
        });
    }

    static ChannelValidationBuilder expectTheMessageToHaveReturnedFromAllCalls() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final Channel<TestMessage> initialChannel = getTestPropertyAsChannel(testEnvironment, SUT);
            final List<Channel<TestMessage>> callTargetLists = getTestPropertyAsListOfChannel(testEnvironment, CALL_TARGET_CHANNEL);
            final Channel<TestMessage> firstCallTargetChannel = callTargetLists.get(0);
            final Channel<TestMessage> secondCallTargetChannel = callTargetLists.get(1);
            final Channel<TestMessage> returningTargetChannel = getTestPropertyAsChannel(testEnvironment, RETURNING_CHANNEL);
            assertMessageFollowedChannelWithActions(testEnvironment, aProcessingFrameHistory()
                    .withAFrameFor(initialChannel, Call.class)
                    .withAFrameFor(firstCallTargetChannel, Call.class)
                    .withAFrameFor(secondCallTargetChannel, Jump.class)
                    .withAFrameFor(returningTargetChannel, Return.class)
                    .withAFrameFor(firstCallTargetChannel, Return.class)
                    .withAFrameFor(initialChannel, Consume.class));
        });
    }

    static ChannelValidationBuilder expectAExceptionOfType(final Class<?> expectedExceptionClass) {
        return aValidation(testEnvironment -> assertExceptionThrownOfType(testEnvironment, expectedExceptionClass));
    }

    static ChannelValidationBuilder expectTheChangedActionToBeExecuted() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    static ChannelValidationBuilder expectAllFilterToBeInCorrectOrderInChannel() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = getTestPropertyAsListOfFilter(testEnvironment, EXPECTED_RESULT);
            assertFilterAsExpected(testEnvironment, expectedFilter);
        });
    }

    static ChannelValidationBuilder expectTheFilterInOrderAsAdded() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    static ChannelValidationBuilder expectTheAllRemainingFilter() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = getTestPropertyAsListOfFilter(testEnvironment, EXPECTED_RESULT);
            assertFilterAsExpected(testEnvironment, expectedFilter);
        });
    }

    static ChannelValidationBuilder expectTheMetaDataChangePersist() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertMetaDatumOfResultSetAsExpected(testEnvironment);
        });
    }

    private static Channel<TestMessage> getTestPropertyAsChannel(final TestEnvironment testEnvironment, final TestEnvironmentProperty property) {
        return getTestPropertyAsChannel(testEnvironment, property.name());
    }

    @SuppressWarnings("unchecked")
    private static Channel<TestMessage> getTestPropertyAsChannel(final TestEnvironment testEnvironment, final String property) {
        return (Channel<TestMessage>) testEnvironment.getProperty(property);
    }

    @SuppressWarnings("unchecked")
    private static List<Channel<TestMessage>> getTestPropertyAsListOfChannel(final TestEnvironment testEnvironment, final String property) {
        return (List<Channel<TestMessage>>) testEnvironment.getProperty(property);
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
