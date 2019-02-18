package com.envimate.messageMate.channel;

import com.envimate.messageMate.channel.action.actionHandling.CallNotAllowedAsFinalChannelAction;
import com.envimate.messageMate.channel.action.actionHandling.NotHandlerForUnknownActionException;
import com.envimate.messageMate.channel.action.actionHandling.ReturnWithoutCallException;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.channel.ChannelActionBuilder.*;
import static com.envimate.messageMate.channel.ChannelSetupBuilder.*;
import static com.envimate.messageMate.channel.ChannelValidationBuilder.*;
import static com.envimate.messageMate.channel.Given.given;

class ChannelSpecs {

    @Test
    void testChannel_canConsumeMessage() {
        given(aConfiguredChannel()
                .withDefaultActionConsume())
                .when(aMessageIsSend())
                .then(expectTheMessageToBeConsumed());
    }

    @Test
    void testChannel_letMessagesJumpToDifferentChannel() {
        given(aConfiguredChannel()
                .withDefaultActionJumpToDifferentChannel())
                .when(aMessageIsSend())
                .then(expectTheMessageToBeConsumedByTheSecondChannel());
    }

    @Test
    void testChannel_continuesHistoryWhenChannelsAreChanged() {
        given(threeChannelsConnectedWithJumps())
                .when(aMessageIsSend())
                .then(expectAllChannelsToBeContainedInTheHistory());
    }

    @Test
    void testChannel_canReturnFromACall() {
        given(aChannelCallingASecondThatReturnsBack())
                .when(aCallToTheSecondChannelIsExecuted())
                .then(expectTheMessageToHaveReturnedSuccessfully());
    }

    @Test
    void testChannel_canExecuteNestedCalls() {
        given(ChannelSetupBuilder.aChannelSetupWithNestedCalls())
                .when(aMessageIsSend())
                .then(expectTheMessageToHaveReturnedFromAllCalls());
    }

    @Test
    void testChannel_failsForReturnWithoutACall() {
        given(aConfiguredChannel()
                .withDefaultActionReturn())
                .when(aMessageIsSend())
                .then(expectAExceptionOfType(ReturnWithoutCallException.class));
    }

    @Test
    void testChannel_failsForCallAsFinalAction() {
        given(aConfiguredChannel()
                .withDefaultActionCall())
                .when(aMessageIsSend())
                .then(expectAExceptionOfType(CallNotAllowedAsFinalChannelAction.class));
    }

    @Test
    void testChannel_failsForUnknownAction() {
        given(aConfiguredChannel()
                .withAnUnknownAction())
                .when(aMessageIsSend())
                .then(expectAExceptionOfType(NotHandlerForUnknownActionException.class));
    }

    //TODO: what about async and threads
    //filter
    @Test
    void testChannel_allowsFilterToChangeAction_forPreFilter() {
        given(aConfiguredChannel()
                .withAPreFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }

    @Test
    void testChannel_allowsFilterToReplaceMessage_forPreFilter() {
        given(aConfiguredChannel()
                .withAPreFilterThatReplacesTheMessage())
                .when(aMessageIsSend())
                .then(expectTheMessageToBeReplaced());
    }

    @Test
    void testChannel_allowsFilterToBlockMessage_forPreFilter() {
        given(aConfiguredChannel()
                .withAPreFilterThatBlocksMessages())
                .when(aMessageIsSend())
                .then(expectNoMessageToBeDelivered());
    }

    @Test
    void testChannel_dropsMessageWhenMessageIsForgotten_forPreFilter() {
        given(aConfiguredChannel()
                .withAPreFilterThatForgetsMessages())
                .when(aMessageIsSend())
                .then(expectNoMessageToBeDelivered());
    }

    @Test
    void testChannel_allowsAddingFilterWithPosition_forPreFilter() {
        given(aConfiguredChannel())
                .when(severalPreFilterOnDifferentPositionAreAdded())
                .then(expectAllFilterToBeInCorrectOrderInChannel());
    }

    @Test
    void testChannel_canQueryFilter_forPreFilter() {
        given(aConfiguredChannel()
                .withSeveralPreFilter())
                .when(theFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    void testChannel_canRemoveAFilter_forPreFilter() {
        given(aConfiguredChannel()
                .withSeveralPreFilter())
                .when(oneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    void testChannel_throwsExceptionForPositionBelowZero_forPreFilter() {
        given(aConfiguredChannel()
                .withAPreFilterAtAnInvalidPosition(-1))
                .when(aMessageIsSend())
                .then(expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    void testChannel_throwsExceptionForPositionGreaterThanAllowed_forPreFilter() {
        given(aConfiguredChannel()
                .withAPreFilterAtAnInvalidPosition(100))
                .when(aMessageIsSend())
                .then(expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    void testChannel_allowsFilterToChangeAction_forProcessFilter() {
        given(aConfiguredChannel()
                .withAProcessFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }


    @Test
    void testChannel_allowsFilterToReplaceMessage_forProcessFilter() {
        given(aConfiguredChannel()
                .withAProcessFilterThatReplacesTheMessage())
                .when(aMessageIsSend())
                .then(expectTheMessageToBeReplaced());
    }

    @Test
    void testChannel_allowsFilterToBlockMessage_forProcessFilter() {
        given(aConfiguredChannel()
                .withAProcessFilterThatBlocksMessages())
                .when(aMessageIsSend())
                .then(expectNoMessageToBeDelivered());
    }

    @Test
    void testChannel_dropsMessageWhenMessageIsForgotten_forProcessFilter() {
        given(aConfiguredChannel()
                .withAProcessFilterThatForgetsMessages())
                .when(aMessageIsSend())
                .then(expectNoMessageToBeDelivered());
    }

    @Test
    void testChannel_allowsAddingFilterWithPosition_forProcessFilter() {
        given(aConfiguredChannel())
                .when(severalProcessFilterOnDifferentPositionAreAdded())
                .then(expectAllFilterToBeInCorrectOrderInChannel());
    }

    @Test
    void testChannel_canQueryFilter_forProcessFilter() {
        given(aConfiguredChannel()
                .withSeveralProcessFilter())
                .when(theFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    void testChannel_canRemoveAFilter_forProcessFilter() {
        given(aConfiguredChannel()
                .withSeveralProcessFilter())
                .when(oneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    void testChannel_throwsExceptionForPositionBelowZero_forProcessFilter() {
        given(aConfiguredChannel()
                .withAProcessFilterAtAnInvalidPosition(-1))
                .when(aMessageIsSend())
                .then(expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    void testChannel_throwsExceptionForPositionGreaterThanAllowed_forProcessFilter() {
        given(aConfiguredChannel()
                .withAProcessFilterAtAnInvalidPosition(100))
                .when(aMessageIsSend())
                .then(expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    void testChannel_allowsFilterToChangeAction_forPostFilter() {
        given(aConfiguredChannel()
                .withAPostFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }

    @Test
    void testChannel_allowsFilterToReplaceMessage_forPostFilter() {
        given(aConfiguredChannel()
                .withAPostFilterThatReplacesTheMessage())
                .when(aMessageIsSend())
                .then(expectTheMessageToBeReplaced());
    }

    @Test
    void testChannel_allowsFilterToBlockMessage_forPostFilter() {
        given(aConfiguredChannel()
                .withAPostFilterThatBlocksMessages())
                .when(aMessageIsSend())
                .then(expectNoMessageToBeDelivered());
    }

    @Test
    void testChannel_dropsMessageWhenMessageIsForgotten_forPostFilter() {
        given(aConfiguredChannel()
                .withAPostFilterThatForgetsMessages())
                .when(aMessageIsSend())
                .then(expectNoMessageToBeDelivered());
    }

    @Test
    void testChannel_allowsAddingFilterWithPosition_forPostFilter() {
        given(aConfiguredChannel())
                .when(severalPostFilterOnDifferentPositionAreAdded())
                .then(expectAllFilterToBeInCorrectOrderInChannel());
    }

    @Test
    void testChannel_canQueryFilter_forPostFilter() {
        given(aConfiguredChannel()
                .withSeveralPostFilter())
                .when(theFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    void testChannel_canRemoveAFilter_forPostFilter() {
        given(aConfiguredChannel()
                .withSeveralPostFilter())
                .when(oneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    void testChannel_throwsExceptionForPositionBelowZero_forPostFilter() {
        given(aConfiguredChannel()
                .withAPostFilterAtAnInvalidPosition(-1))
                .when(aMessageIsSend())
                .then(expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    void testChannel_throwsExceptionForPositionGreaterThanAllowed_forPostFilter() {
        given(aConfiguredChannel()
                .withAPostFilterAtAnInvalidPosition(100))
                .when(aMessageIsSend())
                .then(expectTheException(IndexOutOfBoundsException.class));
    }

    //metadata
    @Test
    void testChannel_filterCanModifyMetaData() {
        given(aConfiguredChannel()
                .withDefaultActionConsume())
                .when(whenTheMetaDataIsModified())
                .then(expectTheMetaDataChangePersist());
    }

    /*
    //TODO: what about statistics

    @Test
    default void testPipe_returnsCorrectNumberOfDroppedMessages(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASingleSubscriber()
                .withAFilterThatDropsWrongMessages())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfDroppedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testPipe_returnsCorrectNumberOfReplacedMessages(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASingleSubscriber()
                .withAFilterThatReplacesWrongMessages())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfReplacedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testPipe_whenAFilterDoesNotUseAMethod_theMessageIsMarkedAsForgotten(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(3)
                .withAnInvalidFilterThatDoesNotUseAnyFilterMethods())
                .when(aSingleMessageIsSend()
                        .andThen(theNumberOfForgottenMessagesIsQueried()))
                .then(expectResultToBe(1));
    }
     */
}
