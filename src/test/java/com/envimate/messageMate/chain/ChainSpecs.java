package com.envimate.messageMate.chain;

import com.envimate.messageMate.chain.action.actionHandling.CallNotAllowedAsFinalChainAction;
import com.envimate.messageMate.chain.action.actionHandling.NotHandlerForUnknownActionException;
import com.envimate.messageMate.chain.action.actionHandling.ReturnWithoutCallException;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.chain.ChainActionBuilder.*;
import static com.envimate.messageMate.chain.ChainSetupBuilder.*;
import static com.envimate.messageMate.chain.ChainValidationBuilder.*;
import static com.envimate.messageMate.chain.Given.given;

class ChainSpecs {

    @Test
    void testChain_canConsumeMessage() {
        given(aConfiguredChain()
                .withDefaultActionConsume())
                .when(aMessageIsSend())
                .then(expectTheMessageToBeConsumed());
    }

    @Test
    void testChain_letMessagesJumpToDifferentChain() {
        given(aConfiguredChain()
                .withDefaultActionJumpToDifferentChain())
                .when(aMessageIsSend())
                .then(expectTheMessageToBeConsumedByTheSecondChain());
    }

    @Test
    void testChains_continuesHistoryWhenChainsAreChanged() {
        given(threeChainsConnectedWithJumps())
                .when(aMessageIsSend())
                .then(expectAllChainsToBeContainedInTheHistory());
    }

    @Test
    void testChain_canReturnFromACall() {
        given(aChainCallingASecondThatReturnsBack())
                .when(aCallToTheSecondChainIsExecuted())
                .then(expectTheMessageToHaveReturnedSuccessfully());
    }

    @Test
    void testChain_canExecuteNestedCalls() {
        given(ChainSetupBuilder.aChainSetupWithNestedCalls())
                .when(aMessageIsSend())
                .then(expectTheMessageToHaveReturnedFromAllCalls());
    }

    @Test
    void testChain_failsForReturnWithoutACall() {
        given(aConfiguredChain()
                .withDefaultActionReturn())
                .when(aMessageIsSend())
                .then(expectAExceptionOfType(ReturnWithoutCallException.class));
    }

    @Test
    void testChain_failsForCallAsFinalChainAction() {
        given(aConfiguredChain()
                .withDefaultActionCall())
                .when(aMessageIsSend())
                .then(expectAExceptionOfType(CallNotAllowedAsFinalChainAction.class));
    }

    @Test
    void testChain_failsForUnknownAction() {
        given(aConfiguredChain()
                .withAnUnknownAction())
                .when(aMessageIsSend())
                .then(expectAExceptionOfType(NotHandlerForUnknownActionException.class));
    }

    @Test
    void testChain_allowsFilterInPreChannelToChangeAction() {
        given(aConfiguredChain()
                .withAPreFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }

    @Test
    void testChain_allowsFilterInProcessChannelToChangeAction() {
        given(aConfiguredChain()
                .withAProcessFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }

    @Test
    void testChain_allowsFilterInPostChannelToChangeAction() {
        given(aConfiguredChain()
                .withAPostFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }

    @Test
    void testChain_allowsAddingFilterWithPositionInPreChannel() {
        given(aConfiguredChain())
                .when(severalFilterOnDifferentPositionAreAddedInPreChannel())
                .then(expectAllFilterToBeInCorrectOrderInChain());
    }

    @Test
    void testChains_allowsAddingFilterWithPositionInProcessChannel() {
        given(aConfiguredChain())
                .when(severalFilterOnDifferentPositionAreAddedInProcessChannel())
                .then(expectAllFilterToBeInCorrectOrderInChain());
    }

    @Test
    void testChain_allowsAddingFilterWithPositionInPostChannel() {
        given(aConfiguredChain())
                .when(severalFilterOnDifferentPositionAreAddedInPostChannel())
                .then(expectAllFilterToBeInCorrectOrderInChain());
    }

    @Test
    void testChain_canQueryFilterInPreChannel() {
        given(aConfiguredChain()
                .withSeveralFilterInThePreChannel())
                .when(whenTheFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    void testChain_canQueryFilterInProcessChannel() {
        given(aConfiguredChain()
                .withSeveralFilterInTheProcessChannel())
                .when(whenTheFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    void testChain_canQueryFilterInPostChannel() {
        given(aConfiguredChain()
                .withSeveralFilterInThePostChannel())
                .when(whenTheFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    void testChain_canRemoveAFilterFromPreChannel() {
        given(aConfiguredChain()
                .withSeveralFilterInThePreChannel())
                .when(whenOneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    void testChain_canRemoveAFilterFromProcessChannel() {
        given(aConfiguredChain()
                .withSeveralFilterInTheProcessChannel())
                .when(whenOneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    void testChain_canRemoveAFilterFromPostChannel() {
        given(aConfiguredChain()
                .withSeveralFilterInThePostChannel())
                .when(whenOneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    void testChain_filterCanModifyMetaData() {
        given(aConfiguredChain()
                .withDefaultActionConsume())
                .when(whenTheMetaDataIsModified())
                .then(expectTheMetaDataChangePersist());
    }
}
