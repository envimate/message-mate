package com.envimate.messageMate.chain;

import com.envimate.messageMate.chain.action.actionHandling.CallNotAllowedAsFinalChainAction;
import com.envimate.messageMate.chain.action.actionHandling.NotHandlerForUnknownActionException;
import com.envimate.messageMate.chain.action.actionHandling.ReturnWithoutCallException;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.chain.ChainActionBuilder.*;
import static com.envimate.messageMate.chain.ChainSetupBuilder.aConfiguredChain;
import static com.envimate.messageMate.chain.ChainValidationBuilder.*;

class ChainSpecs {

    @Test
    void testChain_canConsumeMessage() {
        Given.given(aConfiguredChain()
                .withDefaultActionConsume())
                .when(aMessageIsSend())
                .then(expectTheMessageToBeConsumed());
    }

    @Test
    void testChain_letMessagesJumpToDifferentChain() {
        Given.given(aConfiguredChain()
                .withDefaultActionJumpToDifferentChain())
                .when(aMessageIsSend())
                .then(expectTheMessageToBeConsumedByTheSecondChain());
    }

    @Test
    void testChains_continuesHistoryWhenChainsAreChanged() {
        Given.given(ChainSetupBuilder.threeChainsConnectedWithJumps())
                .when(aMessageIsSend())
                .then(expectAllChainsToBeContainedInTheHistory());
    }

    @Test
    void testChain_canReturnFromACall() {
        Given.given(ChainSetupBuilder.aChainCallingASecondThatReturnsBack())
                .when(aCallToTheSecondChainIsExecuted())
                .then(expectTheMessageToHaveReturnedSuccessfully());
    }

    @Test
    void testChain_canExecuteNestedCalls() {
        Given.given(ChainSetupBuilder.aChainSetupWithNestedCalls())
                .when(aMessageIsSend())
                .then(expectTheMessageToHaveReturnedFromAllCalls());
    }

    @Test
    void testChain_failsForReturnWithoutACall() {
        Given.given(aConfiguredChain()
                .withDefaultActionReturn())
                .when(aMessageIsSend())
                .then(expectAExceptionOfType(ReturnWithoutCallException.class));
    }

    @Test
    void testChain_failsForCallAsFinalChainAction() {
        Given.given(aConfiguredChain()
                .withDefaultActionCall())
                .when(aMessageIsSend())
                .then(expectAExceptionOfType(CallNotAllowedAsFinalChainAction.class));
    }

    @Test
    void testChain_failsForUnknownAction() {
        Given.given(aConfiguredChain()
                .withAnUnknownAction())
                .when(aMessageIsSend())
                .then(expectAExceptionOfType(NotHandlerForUnknownActionException.class));
    }

    @Test
    void testChain_allowsFilterInPreChannelToChangeAction() {
        Given.given(aConfiguredChain()
                .withAPreFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }

    @Test
    void testChain_allowsFilterInProcessChannelToChangeAction() {
        Given.given(aConfiguredChain()
                .withAProcessFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }

    @Test
    void testChain_allowsFilterInPostChannelToChangeAction() {
        Given.given(aConfiguredChain()
                .withAPostFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }

    @Test
    void testChain_allowsAddingFilterWithPositionInPreChannel() {
        Given.given(aConfiguredChain())
                .when(severalFilterOnDifferentPositionAreAddedInPreChannel())
                .then(expectAllFilterToBeInCorrectOrderInChain());
    }

    @Test
    void testChains_allowsAddingFilterWithPositionInProcessChannel() {
        Given.given(aConfiguredChain())
                .when(severalFilterOnDifferentPositionAreAddedInProcessChannel())
                .then(expectAllFilterToBeInCorrectOrderInChain());
    }

    @Test
    void testChain_allowsAddingFilterWithPositionInPostChannel() {
        Given.given(aConfiguredChain())
                .when(severalFilterOnDifferentPositionAreAddedInPostChannel())
                .then(expectAllFilterToBeInCorrectOrderInChain());
    }

    @Test
    void testChain_canQueryFilterInPreChannel() {
        Given.given(aConfiguredChain()
                .withSeveralFilterInThePreChannel())
                .when(whenTheFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    void testChain_canQueryFilterInProcessChannel() {
        Given.given(aConfiguredChain()
                .withSeveralFilterInTheProcessChannel())
                .when(whenTheFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    void testChain_canQueryFilterInPostChannel() {
        Given.given(aConfiguredChain()
                .withSeveralFilterInThePostChannel())
                .when(whenTheFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    void testChain_canRemoveAFilterFromPreChannel() {
        Given.given(aConfiguredChain()
                .withSeveralFilterInThePreChannel())
                .when(whenOneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    void testChain_canRemoveAFilterFromProcessChannel() {
        Given.given(aConfiguredChain()
                .withSeveralFilterInTheProcessChannel())
                .when(whenOneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    void testChain_canRemoveAFilterFromPostChannel() {
        Given.given(aConfiguredChain()
                .withSeveralFilterInThePostChannel())
                .when(whenOneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    void testChain_filterCanModifyMetaData() {
        Given.given(aConfiguredChain()
                .withDefaultActionConsume())
                .when(whenTheMetaDataIsModified())
                .then(expectTheMetaDataChangePersist());
    }
}
