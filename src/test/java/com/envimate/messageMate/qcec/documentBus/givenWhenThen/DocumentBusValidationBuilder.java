package com.envimate.messageMate.qcec.documentBus.givenWhenThen;

import com.envimate.messageMate.qcec.shared.TestReceiver;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.qcec.shared.testEvents.EndingEvent;
import com.envimate.messageMate.qcec.shared.testQueries.SpecificQuery;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RequiredArgsConstructor(access = PRIVATE)
public final class DocumentBusValidationBuilder {
    private final TestValidation testValidation;

    public static DocumentBusValidationBuilder expectNoQueryAfterTheEventToHaveAResult() {
        return new DocumentBusValidationBuilder(testEnvironment -> {
            final List results = testEnvironment.getPropertyAsType(RESULT, List.class);
            final Object expectedQueryResult = testEnvironment.getProperty(EXPECTED_RESULT);
            boolean eventSeen = false;
            for (final Object result : results) {
                if (result instanceof EndingEvent) {
                    eventSeen = true;
                } else {
                    final int queryResult = (int) result;
                    if (eventSeen) {
                        assertThat(queryResult, equalTo(0));
                    } else {
                        assertThat(queryResult, equalTo(expectedQueryResult));
                    }
                }
            }
        });
    }


    public static DocumentBusValidationBuilder expectOnlyTheFirstConstraintToBeReceived() {
        return expectOnlyTheExpectedResultToBeReceived();
    }

    public static DocumentBusValidationBuilder expectOnlyTheFirstEventToBeReceived() {
        return expectOnlyTheExpectedResultToBeReceived();
    }

    private static DocumentBusValidationBuilder expectOnlyTheExpectedResultToBeReceived() {
        return new DocumentBusValidationBuilder(testEnvironment -> {
            final List<TestReceiver> receivers = (List<TestReceiver>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
            final Object expectedReceivedEvent = testEnvironment.getProperty(EXPECTED_RESULT);
            for (final TestReceiver receiver : receivers) {
                final List receivedObjects = receiver.getReceivedObjects();
                assertThat(receivedObjects.size(), equalTo(1));
                final Object receivedEvent = receivedObjects.get(0);
                assertThat(receivedEvent, equalTo(expectedReceivedEvent));
            }
        });
    }


    public static DocumentBusValidationBuilder expectOnlyEventsOfInterestToBeReceived() {
        return expectOnlyObjectsOfInterestToBeReceived();
    }

    public static DocumentBusValidationBuilder expectOnlyConstraintsOfInterestToBeReceived() {
        return expectOnlyObjectsOfInterestToBeReceived();
    }

    public static DocumentBusValidationBuilder expectOnlyQueriesOfInterestToBeReceived() {
        return expectOnlyObjectsOfInterestToBeReceived();
    }

    private static DocumentBusValidationBuilder expectOnlyObjectsOfInterestToBeReceived() {
        return new DocumentBusValidationBuilder(testEnvironment -> {
            final List<TestReceiver<SpecificQuery>> receivers = (List<TestReceiver<SpecificQuery>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
            final Object expectedReceivedObject = testEnvironment.getProperty(TEST_OBJECT);
            for (final TestReceiver<SpecificQuery> receiver : receivers) {
                final List<Object> receivedObjects = receiver.getReceivedObjects();
                assertThat(receivedObjects.size(), equalTo(1));
                final Object onlyReceivedQuery = receivedObjects.get(0);
                assertThat(onlyReceivedQuery, equalTo(expectedReceivedObject));
            }
        });
    }


    public TestValidation build() {
        return testValidation;
    }
}
