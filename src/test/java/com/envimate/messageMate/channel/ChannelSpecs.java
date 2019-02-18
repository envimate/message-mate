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

    @Test
    void testChannel_allowsFilterInPrePipeToChangeAction() {
        given(aConfiguredChannel()
                .withAPreFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }

    @Test
    void testChannel_allowsFilterInProcessPipeToChangeAction() {
        given(aConfiguredChannel()
                .withAProcessFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }

    @Test
    void testChannel_allowsFilterInPostPipeToChangeAction() {
        given(aConfiguredChannel()
                .withAPostFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }

    @Test
    void testChannel_allowsAddingFilterWithPositionInPrePipe() {
        given(aConfiguredChannel())
                .when(severalFilterOnDifferentPositionAreAddedInPrePipe())
                .then(expectAllFilterToBeInCorrectOrderInChannel());
    }

    @Test
    void testChannel_allowsAddingFilterWithPositionInProcessPipe() {
        given(aConfiguredChannel())
                .when(severalFilterOnDifferentPositionAreAddedInProcessPipe())
                .then(expectAllFilterToBeInCorrectOrderInChannel());
    }

    @Test
    void testChannel_allowsAddingFilterWithPositionInPostPipe() {
        given(aConfiguredChannel())
                .when(severalFilterOnDifferentPositionAreAddedInPostPipe())
                .then(expectAllFilterToBeInCorrectOrderInChannel());
    }

    @Test
    void testChannel_canQueryFilterInPrePipe() {
        given(aConfiguredChannel()
                .withSeveralFilterInThePrePipe())
                .when(whenTheFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    void testChannel_canQueryFilterInProcessPipe() {
        given(aConfiguredChannel()
                .withSeveralFilterInTheProcessPipe())
                .when(whenTheFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    void testChannel_canQueryFilterInPostPipe() {
        given(aConfiguredChannel()
                .withSeveralFilterInThePostPipe())
                .when(whenTheFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    void testChannel_canRemoveAFilterFromPrePipe() {
        given(aConfiguredChannel()
                .withSeveralFilterInThePrePipe())
                .when(whenOneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    void testChannel_canRemoveAFilterFromProcessPipe() {
        given(aConfiguredChannel()
                .withSeveralFilterInTheProcessPipe())
                .when(whenOneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    void testChannel_canRemoveAFilterFromPostPipe() {
        given(aConfiguredChannel()
                .withSeveralFilterInThePostPipe())
                .when(whenOneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    void testChannel_filterCanModifyMetaData() {
        given(aConfiguredChannel()
                .withDefaultActionConsume())
                .when(whenTheMetaDataIsModified())
                .then(expectTheMetaDataChangePersist());
    }
}
