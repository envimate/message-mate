package com.envimate.messageMate.qcec.querying.givenWhenThen;

import com.envimate.messageMate.qcec.queryresolving.Query;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.testQueries.TestQuery;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_EXCEPTION_MESSAGE;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.testQueries.TestQuery.aTestQuery;
import static com.envimate.messageMate.qcec.shared.testQueries.TestQuery.aTestQueryWithoutResult;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class QueryActionBuilder {
    private final TestAction<TestQueryResolver> testAction;

    public static QueryActionBuilder aQueryIsExecuted() {
        return new QueryActionBuilder((testQueryResolver, testEnvironment) -> {
            final int expectedResult = 5;
            testEnvironment.setProperty(EXPECTED_RESULT, expectedResult);
            testQueryResolver.subscribing(TestQuery.class, testQuery -> testQuery.setResult(expectedResult));
            final Optional<Integer> optionalResult = testQueryResolver.executeQuery(aTestQuery());
            return optionalResult.orElseThrow(() -> new RuntimeException("Query did not produce a result."));
        });
    }

    public static QueryActionBuilder theQueryIsExecuted(final Query<Integer> query) {
        return new QueryActionBuilder((testQueryResolver, testEnvironment) -> testQueryResolver.executeRequiredQuery(query));
    }

    public static QueryActionBuilder aQueryIsExecutedThatRequiresAResult() {
        return new QueryActionBuilder((testQueryResolver, testEnvironment) -> {
            final int expectedResult = 5;
            testEnvironment.setProperty(EXPECTED_RESULT, expectedResult);
            testQueryResolver.subscribing(TestQuery.class, testQuery -> testQuery.setResult(expectedResult));
            return testQueryResolver.executeRequiredQuery(aTestQuery());
        });
    }

    public static QueryActionBuilder aQueryIsExecutedThatCollectsPartialResults() {
        return new QueryActionBuilder((testQueryResolver, testEnvironment) -> {
            final int partialResult1 = 1;
            final int partialResult2 = 2;
            final int partialResult3 = 3;
            final int expectedResult = partialResult1 + partialResult2 + partialResult3;
            testEnvironment.setProperty(EXPECTED_RESULT, expectedResult);
            testQueryResolver.subscribing(TestQuery.class, testQuery -> testQuery.addPartialResult(partialResult1));
            testQueryResolver.subscribing(TestQuery.class, testQuery -> testQuery.addPartialResult(partialResult2));
            testQueryResolver.subscribing(TestQuery.class, testQuery -> testQuery.addPartialResult(partialResult3));
            return testQueryResolver.executeRequiredQuery(aTestQuery());
        });
    }

    public static QueryActionBuilder anRecipientIsUnsubscribedBeforeAQueryIsExecuted() {
        return new QueryActionBuilder((testQueryResolver, testEnvironment) -> {
            final int partialResult1 = 1;
            final int partialResult2 = 2;
            final int expectedResult = 2;
            testEnvironment.setProperty(EXPECTED_RESULT, expectedResult);
            final SubscriptionId subscriptionId = testQueryResolver.subscribing(TestQuery.class, testQuery -> testQuery.addPartialResult(partialResult1));
            testQueryResolver.subscribing(TestQuery.class, testQuery -> testQuery.addPartialResult(partialResult2));
            testQueryResolver.unsubscribe(subscriptionId);
            return testQueryResolver.executeRequiredQuery(aTestQuery());
        });
    }


    public static QueryActionBuilder aQueryIsExecutedThatThrowsAnException() {
        return aQueryWithExceptionThrown(false);
    }

    public static QueryActionBuilder aQueryIsExecutedThatExpectsAResultButDidThrowAnException() {
        return aQueryWithExceptionThrown(true);
    }

    private static QueryActionBuilder aQueryWithExceptionThrown(final boolean resultExpected) {
        return new QueryActionBuilder((testQueryResolver, testEnvironment) -> {
            testQueryResolver.subscribing(TestQuery.class, q -> q.addPartialResult(1));

            final String expectedExceptionMessage = "Expected exception message.";
            testQueryResolver.subscribing(TestQuery.class, testQuery -> {
                throw new RuntimeException(expectedExceptionMessage);
            });
            testEnvironment.setProperty(EXPECTED_EXCEPTION_MESSAGE, expectedExceptionMessage);

            testQueryResolver.subscribing(TestQuery.class, q -> q.addPartialResult(10));
            final TestQuery testQuery = aTestQuery();
            if (resultExpected) {
                return testQueryResolver.executeRequiredQuery(testQuery);
            } else {
                return testQueryResolver.executeQuery(testQuery);
            }
        });
    }

    public static QueryActionBuilder aQueryIsExecutedThatRequiresAResultButDoesntProvideOne() {
        return new QueryActionBuilder((testQueryResolver, testEnvironment) -> testQueryResolver.executeRequiredQuery(aTestQueryWithoutResult()));
    }

    public TestAction<TestQueryResolver> build() {
        return testAction;
    }
}
