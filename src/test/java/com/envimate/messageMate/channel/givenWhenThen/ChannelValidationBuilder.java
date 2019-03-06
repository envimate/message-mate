package com.envimate.messageMate.channel.givenWhenThen;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.channel.action.*;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestEnvironmentProperty;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.validations.SharedTestValidations;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestProperties.*;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestValidations.*;
import static com.envimate.messageMate.channel.givenWhenThen.ProcessingFrameHistoryMatcher.aProcessingFrameHistory;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusTestValidations.assertExpectedReceiverReceivedAllMessages;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.*;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelValidationBuilder {
    private final TestValidation testValidation;

    private static ChannelValidationBuilder aValidation(final TestValidation testValidation) {
        return new ChannelValidationBuilder(testValidation);
    }

    public static ChannelValidationBuilder expectTheMessageToBeConsumed() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectTheMessageToBeConsumedByTheSecondChannel() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectAllChannelsToBeContainedInTheHistory() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final List<Channel<TestMessage>> expectedTraversedChannels = getTestPropertyAsListOfChannel(testEnvironment, ALL_CHANNELS);
            assertResultTraversedAllChannelBasedOnTheirDefaultActions(testEnvironment, expectedTraversedChannels);
        });
    }

    public static ChannelValidationBuilder expectTheMessageToHaveReturnedSuccessfully() {
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

    public static ChannelValidationBuilder expectTheMessageToHaveReturnedFromAllCalls() {
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

    public static ChannelValidationBuilder expectTheMessageToBeReplaced() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final Object expectedResult = testEnvironment.getProperty(REPLACED_MESSAGE);
            assertResultEqualsExpected(testEnvironment, expectedResult);
        });
    }

    public static ChannelValidationBuilder expectNoMessageToBeDelivered() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertNoResultSet(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectAExceptionOfType(final Class<?> expectedExceptionClass) {
        return aValidation(testEnvironment -> assertExceptionThrownOfType(testEnvironment, expectedExceptionClass));
    }

    public static ChannelValidationBuilder expectTheChangedActionToBeExecuted() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectAllFilterToBeInCorrectOrderInChannel() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = getTestPropertyAsListOfFilter(testEnvironment, EXPECTED_RESULT);
            assertFilterAsExpected(testEnvironment, expectedFilter);
        });
    }

    public static ChannelValidationBuilder expectTheFilterInOrderAsAdded() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectTheAllRemainingFilter() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = getTestPropertyAsListOfFilter(testEnvironment, EXPECTED_RESULT);
            assertFilterAsExpected(testEnvironment, expectedFilter);
        });
    }

    public static ChannelValidationBuilder expectTheMetaDataChangePersist() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertMetaDatumOfResultSetAsExpected(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectTheResult(final Object expectedResult) {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, expectedResult);
        });
    }

    public static ChannelValidationBuilder expectTheExceptionCatched(final Class<?> expectedResultClass) {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultOfClass(testEnvironment, expectedResultClass);
        });
    }

    public static ChannelValidationBuilder expectTheException(final Class<?> expectedExceptionClass) {
        return aValidation(testEnvironment -> assertExceptionThrownOfType(testEnvironment, expectedExceptionClass));
    }

    public static ChannelValidationBuilder expectTheMessageToBeReceivedByAllSubscriber() {
        return aValidation(testEnvironment -> {
            final ProcessingContext<?> processingContext = testEnvironment.getPropertyAsType(EXPECTED_RESULT, ProcessingContext.class);
            final Object expectedMessage = processingContext.getPayload();
            final List<?> expectedTestMessages = Collections.singletonList(expectedMessage);
            assertExpectedReceiverReceivedAllMessages(testEnvironment, expectedTestMessages);
        });
    }

    public static ChannelValidationBuilder expectRemainingSubscriber() {
        return aValidation(testEnvironment -> {
            final Channel<TestMessage> channel = getTestPropertyAsChannel(testEnvironment, SUT);
            final Subscription<TestMessage> subscription = (Subscription<TestMessage>) channel.getDefaultAction();
            final List<Subscriber<TestMessage>> subscribers = subscription.getSubscribers();
            final List<?> expectedSubscriber = (List<?>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
            assertThat(subscribers, containsInAnyOrder(expectedSubscriber.toArray()));
        });
    }

    public static ChannelValidationBuilder expectTheMessageToBeReceivedByAllRemainingSubscriber() {
        return expectTheMessageToBeReceivedByAllSubscriber();
    }

    public static ChannelValidationBuilder expectTheChannelToBeShutdown() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertIsShutdown(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectTheShutdownToBeSucceededInTime() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertIsShutdown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, true);
        });
    }

    public static ChannelValidationBuilder expectTheShutdownToBeFailed() {
        return aValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertIsShutdown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, false);
        });
    }

    public static ChannelValidationBuilder expectNoException() {
        return aValidation(SharedTestValidations::assertNoExceptionThrown);
    }

    private static void assertIsShutdown(final TestEnvironment testEnvironment) {
        final Channel<TestMessage> channel = getTestPropertyAsChannel(testEnvironment, SUT);
        final boolean isShutdown = channel.isClosed();
        assertTrue(isShutdown);
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

    public ChannelValidationBuilder and(final ChannelValidationBuilder other) {
        return new ChannelValidationBuilder(testEnvironment -> {
            this.testValidation.validate(testEnvironment);
            other.testValidation.validate(testEnvironment);
        });
    }

    public TestValidation build() {
        return testValidation;
    }
}
