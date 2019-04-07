package com.envimate.messageMate.serializedMessageBus;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.PayloadAndErrorPayload;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.shared.testMessages.ErrorTestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.serializedMessageBus.SerializedMessageBusTestProperties.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.EXPECTED_CORRELATION_ID;
import static com.envimate.messageMate.shared.testMessages.ErrorTestMessage.errorTestMessage;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SerializedMessageBusActionBuilder {
    private final TestAction<SerializedMessageBus> testAction;

    public static SerializedMessageBusActionBuilder aMapDataIsSend() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final Map<String, Object> map = new HashMap<>();
            map.put("someValue", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_DATA, map);
            serializedMessageBus.send(eventType, map);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder aMapDataIsSendForTheGivenCorrelationId() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final CorrelationId correlationId = testEnvironment.getPropertyAsType(EXPECTED_CORRELATION_ID, CorrelationId.class);
            final Map<String, Object> map = new HashMap<>();
            map.put("someValue", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_DATA, map);
            serializedMessageBus.send(EVENT_TYPE_WITH_NO_SUBSCRIBERS, map, correlationId);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder aMapDataWithErrorDataIsSend() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final Map<String, Object> data = new HashMap<>();
            data.put("someValue", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_DATA, data);
            final Map<String, Object> errorData = new HashMap<>();
            errorData.put("exception", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_ERROR_DATA, errorData);
            serializedMessageBus.send(eventType, data, errorData);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder aMapDataWithErrorDataIsSendForTheGivenCorrelationId() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final CorrelationId correlationId = testEnvironment.getPropertyAsType(EXPECTED_CORRELATION_ID, CorrelationId.class);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final Map<String, Object> data = new HashMap<>();
            data.put("someValue", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_DATA, data);
            final Map<String, Object> errorData = new HashMap<>();
            errorData.put("exception", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_ERROR_DATA, errorData);
            serializedMessageBus.send(eventType, data, errorData, correlationId);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectIsSend() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final TestMessageOfInterest message = messageOfInterest();
            testEnvironment.setPropertyIfNotSet(SEND_DATA, message);
            serializedMessageBus.serializeAndSend(eventType, message);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectIsSendForACorrelationId() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final CorrelationId correlationId = testEnvironment.getPropertyAsType(EXPECTED_CORRELATION_ID, CorrelationId.class);
            final TestMessageOfInterest message = messageOfInterest();
            testEnvironment.setPropertyIfNotSet(SEND_DATA, message);
            serializedMessageBus.serializeAndSend(eventType, message, correlationId);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectDataWithErrorDataIsSend() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final TestMessageOfInterest message = messageOfInterest();
            testEnvironment.setPropertyIfNotSet(SEND_DATA, message);
            final ErrorTestMessage errorTestMessage = errorTestMessage();
            testEnvironment.setPropertyIfNotSet(SEND_ERROR_DATA, errorTestMessage);
            serializedMessageBus.serializeAndSend(eventType, message, errorTestMessage);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectDataWithErrorDataIsSendForAGivenCorrelationId() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final CorrelationId correlationId = testEnvironment.getPropertyAsType(EXPECTED_CORRELATION_ID, CorrelationId.class);
            final TestMessageOfInterest message = messageOfInterest();
            testEnvironment.setPropertyIfNotSet(SEND_DATA, message);
            final ErrorTestMessage errorTestMessage = errorTestMessage();
            testEnvironment.setPropertyIfNotSet(SEND_ERROR_DATA, errorTestMessage);
            serializedMessageBus.serializeAndSend(eventType, message, errorTestMessage, correlationId);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder aMapIsSendAndTheResultIsWaited() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final Map<String, Object> data = new HashMap<>();
            data.put("someValue", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_DATA, data);
            try {
                final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> result = serializedMessageBus.invokeAndWait(eventType, data);
                testEnvironment.setPropertyIfNotSet(RESULT, result);
            } catch (final InterruptedException | ExecutionException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder aMapIsSendAndTheResultIsWaitedWithTimeout() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final Map<String, Object> data = new HashMap<>();
            data.put("someValue", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_DATA, data);
            try {
                final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> result = serializedMessageBus.invokeAndWait(eventType, data, 10, MILLISECONDS);
                testEnvironment.setPropertyIfNotSet(RESULT, result);
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectIsSendAndTheResultIsWaited() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            testEnvironment.setPropertyIfNotSet(SEND_DATA, message);
            try {
                final PayloadAndErrorPayload<TestMessageOfInterest, ErrorTestMessage> result = serializedMessageBus.invokeAndWaitDeserialized(eventType, message, TestMessageOfInterest.class, ErrorTestMessage.class);
                testEnvironment.setPropertyIfNotSet(RESULT, result);
            } catch (final InterruptedException | ExecutionException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectIsSendAndTheResultIsWaitedWithTimeout() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            testEnvironment.setPropertyIfNotSet(SEND_DATA, message);
            try {
                final PayloadAndErrorPayload<TestMessageOfInterest, ErrorTestMessage> result = serializedMessageBus.invokeAndWaitDeserialized(eventType, message, TestMessageOfInterest.class, ErrorTestMessage.class, 10, MILLISECONDS);
                testEnvironment.setPropertyIfNotSet(RESULT, result);
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return null;
        });
    }


    public TestAction<SerializedMessageBus> build() {
        return testAction;
    }
}
