package com.envimate.messageMate.useCaseConnecting;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusStatusInformation;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseConnectorValidationBuilder {
    private final TestValidation validation;

    public static UseCaseConnectorValidationBuilder theCallbackToBeCalledWithTheResponse() {
        return new UseCaseConnectorValidationBuilder(testEnvironment -> {
            ensureNoExceptionWasThrown(testEnvironment);
            final Object result = testEnvironment.getProperty(RESULT);
            final Object expectedResult = testEnvironment.getProperty(EXPECTED_RESULT);
            assertThat(result, equalTo(expectedResult));
        });
    }

    public static UseCaseConnectorValidationBuilder anExceptionOfType(final Class<?> expectedExceptionClass) {
        return new UseCaseConnectorValidationBuilder(testEnvironment -> {
            final Object exception = testEnvironment.getProperty(EXCEPTION);
            assertThat(exception.getClass(), equalTo(expectedExceptionClass));
        });
    }


    public static UseCaseConnectorValidationBuilder noExceptionToBeOccurred() {
        return new UseCaseConnectorValidationBuilder(UseCaseConnectorValidationBuilder::ensureNoExceptionWasThrown);
    }


    public static UseCaseConnectorValidationBuilder expectTheSubscriberToBeRemovedFromTheMessageBus() {
        return new UseCaseConnectorValidationBuilder(testEnvironment -> {
            ensureNoExceptionWasThrown(testEnvironment);
            final MessageBus messageBus = testEnvironment.getPropertyAsType(CONTROLLABLE_ENV_OBJECT, MessageBus.class);
            final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
            final Map<Class<?>, List<Subscriber<?>>> subscribersPerType = statusInformation.getSubscribersPerType();
            final List<Subscriber<?>> subscribers = subscribersPerType.get(UseCaseRequest.class);
            assertThat(subscribers.size(), equalTo(0));
        });
    }

    private static void ensureNoExceptionWasThrown(final TestEnvironment testEnvironment) {
        if (testEnvironment.has(EXCEPTION)) {
            final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            fail("Unexpected exception was thrown: ", exception);
        }
    }

    public TestValidation build() {
        return validation;
    }
}
