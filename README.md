# message-mate
Message-Mate is a library for building messaging architectures.

It provides components to integrate parts of your business 
logic in a loosely coupled fashion. This allows for applications to be highly
extensible and easily tested.

## Motivation
Messaging is a form of communication, that exchanges encapsulated messages between 
parts of an application or different applications. In contrast to other types of 
integration it models both the carrier and the send messages as distinct
concepts of the application. This can provide several benefits if used correctly. But it 
can also generate overhead and complexity. This library focuses on a lightweight 
implementation of messaging patterns to be wire use cases and objects within one application. 
For integrating different applications (over network, shared memory, ...) other libraries exist.

This library provides implementations for typical forms of message carriers like 
Channels or MesssageBus. Explicitly modelling these transport mechanisms as distinct
objects have an beneficiary influence on the coupling of independent parts of the 
application. When parts of the application want to communicate with each other, they
send messages. The sender puts its message on the MessageBus. The MessageBus then 
ensures, that the message is delivered to all subscribers. The sender does not need to know
the number or type of the subscribers. The subscribers have no knowledge about the sender.
This leads to a very loosely coupled integration. As sender and subscriber can be added or 
removed dynamically during runtime, the application becomes very flexible. 

Both, MessageBus and Channel, can be configured to provide asynchronous aspects using 
Threads. This simplifies code using these objects, as a lot of asynchronous 
and synchronization problems are already solved. Dynamically scaling out Threads only 
required to change the configuration Channels and MessageBus. The rest of the application
can remain mostly agnostic to it. 

These messaging patterns ease the integration of Frameworks with the application's use cases.
But also the communication between use cases is greatly simplified with a MessageBus. Even
at the scope of Domain Objects messaging patterns can provide loosely coupling and dynamism.

## Installation
To use Message-Mate add the following Maven depedency to your 'pom.xml`:
```java
<dependency>
    <groupId>com.envimate.message-mate</groupId>
    <artifactId>core</artifactId>
    <version>1.0.2</version>
</dependency>
```

## Basic Concepts

### Channel
A common task in message driven architectures is sending messages from a bunch of producers to 
an arbitrary amount of consumers, handling errors and allowing to add Filters dynamically.
In MessageMate Channels provide these kind of properties:
 - add or remove sender and receiver dynamically
 - define the type of send messages, when sender and receiver have agreed on the format of the send messages
 - change the messages during the transportation via Filter: changing the contents of the message, blocking invalid messages,...
 - abstract configuration: once the Channel is created, the participants should be agnostic about the used configuration, e.g. whether the Channel is asynchronous
 - dynamic extension points: add Filter, add logging or even replace the subscriber during tests
 - monitoring: get information about the number of messages that got delivered successful, blocked or failed with exceptions
 
#### Creating a Channel
Channels can be created using the `ChannelBuilder` class:

```java
Channel<TestMessage> channel = ChannelBuilder.aChannel(TestMessage.class)
    .forType(ChannelType.SYNCHRONOUS)
    .withDefaultAction(Consume.consumeMessage(m -> {
        System.out.println(m);
    }))
    .build();

channel.send(new TestMessage());
```
Channels can be of type `SYNCHRONOUS` (which is the default) or of type `ASYNCHRONOUS`.
Synchronous Channels will execute the delivery on the Thread calling `send`. Asynchronous
Channels bring their own Threadpool with them. For more details on how to create and configure
asynchronous Channels see [Configuring the Channel](#Configuring-the-Channel)

At the end of a Channel an Action is executed for each message. Actions abstract
the consumer part of the messaging. The simplest Action is the `Consume` Action 
which executes the given logic for each message that reached the end of the Channel. 
Other Actions allow dynamic subscriptions or jumps to other Channels.

#### Actions
Each message reaching the end of the Channel will be consumed by an Action. This can
be the default Action defined during creation time or a dynamically changed by Filter 
(explained below). Several default Actions exist:

##### Consume
A `Consume` Action calls the given Consumer function for every message that reached
the end of the Channel:

```java
Action<T> action = Consume.consumeMessage(processingContext -> {
    T payload = processingContext.getPayload();
    System.out.println(payload);
});
```

A shortcut exists, if only the payload is needed: 
```java
Action<T> action = Consume.consumePayload(payload -> {
    System.out.println(payload);
});
```

##### Subscription
The `Subscription` Action extends the `Consume` Action with the ability of having 
several consumers, called Subscriber. The `Subscription` Action allows adding and
removing Subscriber dynamically:

```java
Subscription<Object> subscription = Subscription.subscription();

Consumer<Object> consumer = message -> System.out.println(message);

SubscriptionId subscriptionId = subscription.addSubscriber(consumer);

subscription.removeSubscriber(subscriptionId);

Subscriber<Object> subscriber = ConsumerSubscriber.consumerSubscriber(consumer);
SubscriptionId subscriptionId = subscription.addSubscriber(subscriber);
subscription.removeSubscriber(subscriber);

boolean notEmpty = subsription.hasSubscribers();
``` 
The `addSubscriber` method is overloaded to accept either a java Consumer or 
a `Subscriber` object. Classes implementing this interface get more control 
over the management of the SubscriptionId or the acceptance of messages, e.g. 
they can preempt the delivery, so that other subscriber do receive the message. 
See [Subscriber](#Subscriber) for more details. The `addSubscriber` methods returns a `SubscriptionId`, 
which is an unique UUID generated for each `Subscriber`. It can be used to uniquely 
identify a Subscriber. The `removeSubscriber` method makes use of this to remove
subscriptions.

##### Jump
In more complex messaging architectures larger processing logics are often split 
into smaller, logical pieces by chaining Channels. Each Channel is then responsible
for a smaller part in this flow. These Channels can be connected via `Jump`
Actions. A `Jump` Action takes a message and sends it on the next Channel:

```java
Channel<T> nextChannel = ...;
Jump<T> jump = Jump.jumpTo(nextChannel);
```

The reason to use `Jumps` and not a `Consume` calling `send` on the next Channel is 
the control structure used in Channels. Messages send over Channels are
enveloped in `ProcessingContext` objects. These context objects contain history information
over past Channels useful for debugging.
The `Jump` Action handles these context information during the change of Channels
(For more information about the `ProcessingContext` object see [Processing Context](#Processing-Context)).

##### Adding Filter to Channel
Channels provide an extensible mechanism for processing messages: Filter.
A send message traverses all Filter before being consumed by the final Action.
Each Filter has two options: It can allow the message to pass or it can block the message.
A blocked message will stop its propagation through the remaining Filter and will never
reach the final Action:

```java
channel.addProcessFilter((processingContext, filterActions) -> {
    TestMessage message = processingContext.getPayload();
    if (isValid()) {
        message.validated = true;
        filterActions.pass(processingContext);
    } else {
        filterActions.block(processingContext);
    }
});
```
Calling `filterActions.pass` will propagate the message to the next Filter. 
`filterActions.block` will stop the propagation. If none of these methods are called,
the message is also blocked. But not calling the `block` method should be avoided as 
Filter should be written as explicit as possible (Also the message is marked as 
`forgotten` and not as `blocked` in the `ChannelStatistics`).

As mentioned earlier, each message is always enveloped in a `ProcessingContext` control structure.
To get access to the original message use `getPayload`. But the `pass` and
`block` methods again expect the `ProcessingContext` object. Filter can freely access the
`ProcessingContext` object. The most common usage would be to replace the Action, that
is executed at the Channel's end:

```java
channel.addPostFilter((processingContext, filterActions) -> {
    if(!processingContext.actionWasChanged()) {
        processingContext.changeAction(Consume.consumeMessage(m -> {}));
    }
});
```

Filter can be added at three different stages: Pre, Process, Post. These three different
extension points serve as coarse-grained ordering. All Filter in the Pre Stage are always
executed before the Filter in the Process stage, which themselves execute before the Post Filter.
Within these stages the order of Filter follows the contract of java's concurrent list.

For each of three stages methods exists to query registered Filter and to remove 
Filter:
```java
List<Filter<ProcessingContext<T>>> preFilter = channel.getPreFilter();
List<Filter<ProcessingContext<T>>> processFilter = channel.getProcessFilter();
List<Filter<ProcessingContext<T>>> postFilter = channel.getPostFilter();
        
channel.removePreFilter(filter);
channel.removeProcessFilter(filter);
channel.removePostFilter(filter);        
```

##### Call and Return
A special Action that can only be used inside a Filter is the `Call` Action. It is used
to perform an immediate jump to a different Channel. The transport of the message is
resumed the moment the other Channel executes a `Return` as it's final Action. This `Call`/`Return`
combination allows Filter to add arbitrarily complex logic dynamically to a Channel.

```java
Channel<TestMessage> differentChannel = ChannelBuilder.aChannel(TestMessage.class)
    .withDefaultAction(Return.aReturn())
    .build();

channel.addPostFilter((processingContext, filterActions) -> {
    Call.callTo(differentChannel, processingContext);
    System.out.println("Returned from other Channel.");
    filterActions.pass(processingContext);
});
```
`Calls` can be nested arbitrarily and don't need to return. But executing a `Return` without
a previous `Call` will result in an error.

The factory method `Call.callTo` executes the `Call` directly. If access to the `Call`
object is needed, a two step alternative exists:

```java
channel.addPostFilter((processingContext, filterActions) -> {
    final Call<TestMessage> call = Call.prepareACall(differentChannel);
    doSomethingWith(call);
    call.execute(processingContext);
});
```

Once a `Call` and its matching `Return` object was executed, both objects are linked to each other:
```java
//suppose these are the two related actions
Return<Object> returnAction = Return.aReturn();
Call<Object> callAction = Call.prepareACall(otherChannel);


ChannelProcessingFrame<Object> returnFrame = callAction.getReturnFrame();
assertThat(returnFrame.getAction(), equalTo(returnAction));

ChannelProcessingFrame<Object> callFrame = returnAction.getRelatedCallFrame();
assertThat(callFrame.getAction(), equalTo(callAction));

Channel<Object> callActionTargetChannel = callAction.getTargetChannel();
```

#### Channel Statistics
Each Channel provides basic logging in form of statistics itself: It logs the number of 
messages, that were
 - accepted: message was received by Channel and transport was started. A message is always
 accepted or an exception is thrown
 - queued: asynchronous Channel can queue messages, if no Threads are available. This statistic
 resembles the number of currently waiting messages
 - blocked: number of messages that were blocked by Filter
 - forgotten: number of messages that were neither passed nor blocked by Filter
 - successful: number of messages that passed all Filter and executed the final Action without error
 - failed: if an exception is thrown during a Filter or the final Action, the 
 message is marked as failed

 ```java
ChannelStatusInformation statusInformation = channel.getStatusInformation();
ChannelStatistics statistics = statusInformation.getChannelStatistics();
BigInteger acceptedMessages = statistics.getAcceptedMessages();
BigInteger queuedMessages = statistics.getQueuedMessages();
BigInteger blockedMessages = statistics.getBlockedMessages();
BigInteger forgottenMessages = statistics.getForgottenMessages();
BigInteger successfulMessages = statistics.getSuccessfulMessages();
BigInteger failedMessages = statistics.getFailedMessages();
Date timestamp = statistics.getTimestamp();
 ```
Each statistic contains a timestamp indicating the date, when the given numbers
were approximately valid.

#### Closing the Channel
Each Channel can be closed to free resources in case the Channel was stateful 
(Asynchronous Channels are stateful). The following methods exists:

```java
boolean finishRemainingTasks = true;
channel.close(finishRemainingTasks);

boolean closed = channel.isClosed();

try {
    boolean terminationSucceeded = channel.awaitTermination(5, MILLISECONDS);
} catch (InterruptedException e) {
    
}
```

These methods follow the contract, that classes from the standard java library with
these sort of methods abide to. Channel (as all closable Classes in MessageMate) 
also implement the `AutoClosable` interface and can be used in a try-with-resources statement.

#### Configuring the Channel
Configuring a Channel is done using the respective `ChannelBuilder` class' methods.

```java
ChannelBuilder.aChannel()
.forType(ChannelType.ASYNCHRONOUS)
.withAsynchronousConfiguration(asyncConfig)
.withDefaultAction(Subscription.subscription())
.withChannelExceptionHandler(customExceptionHandler)
.withActionHandlerSet(customActionHandlerSet)
.build();
```

The available Actions were discussed in [Actions](#Actions)

##### Type
There exists two types of Channels: `ChannelType.SYNCHRONOUS`and `ChannelType.ASYNCHRONOUS`. Sending on synchronous
Channels is executed on the Thread calling `send`. Asynchronous Channels provide their
own Threads using a Threadpool. Asynchronous Channels require an additional 
`AsynchronousConfiguration`. 

There exists two convenience methods to ease the creation of a fitting asynchronous
configuration:

```java
int numberOfThreads = 5;
AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration(numberOfThreads);
int maximumBoundOfQueuedMessages = 100;
AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration(numberOfThreads, maximumBoundOfQueuedMessages);
```

In case a more fine-tuned configuration is needed, two constructor and getter are provided:
```java
AsynchronousConfiguration configuration = new AsynchronousConfiguration();
configuration.setCorePoolSize(5);

int corePoolSize = 5;
int maximumPoolSize = 10;
int maximumTimeout = 15;
TimeUnit timeUnit = MILLISECONDS;
LinkedBlockingQueue<Runnable> threadPoolWorkingQueue = new LinkedBlockingQueue<>();
new AsynchronousConfiguration(corePoolSize, maximumPoolSize, maximumTimeout, timeUnit, threadPoolWorkingQueue);
```
These configuration properties are identically to those available for the java ThreadPoolExecutor
class, as the asynchronous Channel uses such underneath. For a comprehensive documentation please consult 
the java doc of the ThreadPoolExecutor class.

##### ChannelExceptionHandler
The default exception behaviour is to throw each exception on the Thread it occurs on.
This might not be sufficient for a multi-threaded configuration. Therefore a custom 
exception handler can be set, that gets access to all internal exceptions.
```java
ChannelExceptionHandler<T> channelExceptionHandler = new ChannelExceptionHandler<T>() {
    @Override
    public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(ProcessingContext<T> message, Exception e) {
        boolean abortDeliveryAndHandleError = true;
        return abortDeliveryAndHandleError;
    }

    @Override
    public void handleSubscriberException(ProcessingContext<T> message, Exception e) {

    }

    @Override
    public void handleFilterException(ProcessingContext<T> message, Exception e) {

    }
};
```
When an exception occurs during the `accept` method of a subscriber, first the
`shouldSubscriberErrorBeHandledAndDeliveryAborted` method is called. This method
can decide whether the exception should count as such and the delivery should be aborted.
A `true` results in the message being marked as failed in the statistics.
No further subscriber gets the message delivered and the 
`handleSubscriberException` method is called in the end. Given a `false` the 
exception is ignored and the delivery continues normally.

In case of a Filter throwing an exception, the `handleFilterException` method is called.
An exception inside a Filter always counts as failed and aborts the propagation to 
subsequent Filter or any final Action.

##### ActionHandlerSet
Actions serve only as representative container for the information necessary to 
execute them. Any logic regarding their execution is handled by the `ActionHandlers`. 
This allows exchanging logic without changing Actions and makes debuging easier. The 
`ActionHandlerSet` contains one handler for each Action. 

When a message reaches the end of a Channel, the `ActionHandlerSet` serves as a 
lookup object for an `ActionHandler` matching the Channel's final Action. When a suitable 
handler is found, its `handle` method is called. When no handler is registered an exception is thrown.

```java
ActionHandlerSet<Object> defaultActionHandlerSet = DefaultActionHandlerSet.defaultActionHandlerSet();
defaultActionHandlerSet.registerActionHandler(CustomAction.class, new CustomActionHandler());
        
ChannelBuilder.aChannel()
.withActionHandlerSet(actionHandlerSet);
```

A more in depth explanation about writing custom Actions and `ActionHandlerSets` 
is given in [Custom Actions](#Custom-Actions).

#### Processing Context
Channels can be chained into arbitrary complex structures. The Channels are connected
via Actions (and Calls inside Filter). Filters within these Channels might share data or
the history might be of interest for debugging purpose. Since these type of information
should not be stored inside the payload itself, a wrapping context object is needed,
the `ProcessingContext`. 

Each message contains its own `ProcessingContext` object. The history of Channels
is represented as a linked list of `ChannelProcessingFrames`.
This list includes a frame for each traversed Channel. Each frame contains a reference
to its previous and next frame and to its respective Channel. When a Channel is traversed
to its end, the actual final Action is also stored in the frame.
The `ProcessingContext` object serves as root object referencing the first, initial frame
and the frame of the currently traversed Channel.

```java
ChannelProcessingFrame<T> initialProcessingFrame = processingContext.getInitialProcessingFrame();
Channel<T> channel = initialProcessingFrame.getChannel();
ChannelProcessingFrame<T> previousFrame = initialProcessingFrame.getPreviousFrame();
ChannelProcessingFrame<T> nextFrame = initialProcessingFrame.getNextFrame();
Action<T> executedAction = initialProcessingFrame.getAction();
```

Calls are also included in the linked `ChannelProcessingFrames` list, although stored
a little bit differently. Let's suppose we have 4 Channels:
 - Channel A contains a Filter executing a Call to Channel B. The default Action of
 Channel A is a Jump to Channel D
 - Channel B is the target of the Call within Channel A. As default Action a Jump to
 Channel C is executed.
 - Channel C just executes a Return as Action returning the control to Channel A
 - Channel D is the last Channel with Consume as Action.
 
 ![Channel Call example](documentation/images/channel_call_sample.png)
 
 
The linked list of `ChannelProcessingFrames` would consist of the following 5 entries:
1) a frame for Channel A with a Action `Call` as soon as the Call is executed
2) a frame for Channel B with the default Action `Jump` to Channel C
3) a frame for Channel C with the Action `Return` back to Channel A
4) a frame for Channel A with the default Action `Jump` to Channel D
5) a frame for Channel D with `Consume` as final Action 

So in general one frame is added per Channel, except for a Call. In this case an extra
`ChannelProcessingFrames` is added to indicate the branching of the flow.

Additionally the `ProcessingContext` object provides a MetaDataMap for sharing data between Channels 
or Filter.
```java
ProcessingContext<Object> processingContext = ProcessingContext.processingContext(message);

Map<Object, Object> metaData = processingContext.getContextMetaData();
```


### MessageBus
Channels are restricted to a specific type. This can be a benefit as the format of the 
communication between producer and consumer is defined by the Channel itself. But this solution 
comes short when several formats or communications are to be supported by the same
transport object.

The solution is a MessageBus. Any type of message can be send over a MessageBus. Subscribers
are then able to pick the type of messages they are interested in via class-based subscription.
This makes integrating distinct parts of an application possible.

A MessageBus is structured as follows:

![Channel Call example](documentation/images/MessageBus.png)


Every message is accepted by the AcceptingChannel. The AcceptingChannel is responsible
for the configuration (synchronous or asynchronous) and can also contain Filter that
need access to all messages.
Messages, that passed the AcceptingChannel, are distributed into subscriber-specific 
Channels. Every class, which has at least on subscriber, corresponds t a Channel,
that delivers all message of its class to its subscribers. On this Channel Filter 
can be added, that are specific for this class.


#### Using the MessageBus

```java
MessageBus messageBus = MessageBusBuilder.aMessageBus()
    .forType(MessageBusType.SYNCHRONOUS)
    .build();

SubscriptionId subscriptionId = messageBus.subscribe(TestMessage.class, testMessage -> {
    System.out.println(testMessage);
});

messageBus.send(new TestMessage());
        
messageBus.unsubcribe(subscriptionId);
```

The `MessageBusBuilder` is used to configure and create a MessageBus. 
The `subscribe` method is again overloaded to either add a Subscriber or a java 
consumer. The first parameter defines the type of class of the subscription. 
All messages of this class or a subclass are delivered to its subscriber. 
The returned subscriptionId is used in case the subscriber wants to remove its 
subscription.

#### Adding Filter to MessageBus
The MessageBus can add Filters, that get access to all messages:

```java
final Filter<Object> filter = new Filter<Object>() {
    @Override
    public void apply(Object message, FilterActions<Object> filterActions) {
        //filter logic
    }
};
messageBus.add(filter);

List<Filter<Object>> allFilter = messageBus.getFilter();

messageBus.remove(filter);
```

In case a more fine-grained filtering is needed, the MessageBus allows to query for
the specific Channel for a given class. On this Channel Filter can be added as already
described in [Filter](#Adding-Filter-to-Channel)
```java
MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
Channel<TestMessage> channel = statusInformation.getChannelFor(TestMessage.class);
channel.addPreFilter(filter);
channel.addProcessFilter(filter);
channel.addPostFilter(filter);
```

#### MessageBus Statistics
Similar to the Channel the MessageBus collects statistics about all messages:

```java
MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
MessageBusStatistics statistics = statusInformation.getCurrentMessageStatistics();
BigInteger acceptedMessages = statistics.getAcceptedMessages();
BigInteger queuedMessages = statistics.getQueuedMessages();
BigInteger blockedMessages = statistics.getBlockedMessages();
BigInteger forgottenMessages = statistics.getForgottenMessages();
BigInteger successfulMessages = statistics.getSuccessfulMessages();
BigInteger failedMessages = statistics.getFailedMessages();
Date timestamp = statistics.getTimestamp();
```

#### Querying subscriber
Mostly for debugging purpose the currently registered subscriber can be queried
from the MessageBus:

```java
MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
List<Subscriber<?>> allSubscribers = statusInformation.getAllSubscribers();
Map<Class<?>, List<Subscriber<?>>> subscribersPerType = statusInformation.getSubscribersPerType();
```

#### Closing the MessageBus
The methods to close the MessageBus are similar to those described for Channels in [Closing the Channel](#Closing-the-Channel):

```java
boolean finishRemainingTasks = true;
messageBus.close(finishRemainingTasks);

boolean closed = messageBus.isClosed();

try {
    boolean awaitSucceeded = messageBus.awaitTermination(5, SECONDS);
} catch (InterruptedException e) {

}
```

#### Configuring the MessageBus
All configuration is done using the `MessageBusBuilder` class. All configurable properties have default values. This creates
a synchronous MessageBus. The default `MessageBusExceptionHandler` throws all exceptions on the calling Thread. Whenever a
class specific Channel has to be created, a synchronous one is created by the default MessageBusChannelFactory. 

```java
MessageBusBuilder.aMessageBus()
.forType(MessageBusType.SYNCHRONOUS)
.withAsynchronousConfiguration(asynchronousConfiguration)
.withExceptionHandler(new MessageBusExceptionHandler() {
    @Override
    public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(ProcessingContext<?> message, Exception e, Channel<?> channel) {
        final boolean abortDeliveryAndHandleException = false;
        return abortDeliveryAndHandleException;
    }

    @Override
    public void handleDeliveryChannelException(ProcessingContext<?> message, Exception e, Channel<?> channel) {

    }

    @Override
    public void handleFilterException(ProcessingContext<?> message, Exception e, Channel<?> channel) {

    }
})
.withAChannelFactory(new MessageBusChannelFactory() {
    @Override
    public <T> Channel<?> createChannel(Class<T> tClass, Subscriber<T> subscriber, MessageBusExceptionHandler messageBusExceptionHandler) {
        ChannelExceptionHandler<T> channelExceptionHandler = delegatingExceptionHandlerTo(messageBusExceptionHandler);
        return ChannelBuilder.aChannel()
            .withDefaultAction(Subscription.subscription())
            .withChannelExceptionHandler(channelExceptionHandler)
            .build();
    }
})
.build();
```

The type and the `AsynchronousConfiguration` are similar to those used for Channels 
described in [Configuring the Channel](#Configuring-the-Channel).

The default MessageBusExceptionHandler throws all exceptions. It can be replaced using
`withExceptionHandler` method. When an exception is thrown in one of the subscriber
the `shouldDeliveryChannelErrorBeHandledAndDeliveryAborted` is called to decide,
whether the exception should be handled and the delivery is aborted or whether the
exception should be ignored. In case the exception should be handled, the message
is marked as failed in the statistics and the `handleDeliveryChannelException`
method is called. When an exception is raised in any Filter (general or class
specific Channel), the delivery is aborted, the message is marked as failed and
the control is given to `handleFilterException`. After each of the `handle_Exception`
methods all suitable exception listener are called.

The `MessageBusChannelFactory` is used to create the class-specific Channel, that delivers the
messages to the Subscribers for this class. The default implementation creates a synchronous Channel,
that redirect errors to the `MessageBusErrorHandler`. But in case more control over the
configuration of these Channels is needed, a custom implementation can be given here. The 
creation of a new happen Channel can be requested in two cases: First a subscriber is added 
for a not yet known class. Second, an unknown message was sent. Then for the class of the message 
and all newly discovered parent classes, a new Channel is created.                                                                                        * discovered parent classes, a new {@code Channel} is created.
Care has to be taken to handle or redirect the errors correctly. Also important to note is,
that the `close` call to the MessageBus will not be propagated to the `MessageBusChannelFactory`.
If a custom `MessageBusChannelFactory` contains state that requires a teardown, the 
synchronisation with the `close` call has to be enforced manually.

#### Dynamically adding exception listener
Once the MessageBus is created, the given `MessageBusExceptionHandler` can not be changed.
But since subscribers are added or removed to or from a MessageBus in a highly dynamical 
way, a static exception handler becomes a problem. Therefore the MessageBus provides a
way to register exception listener for specific classes of messages on the fly. These 
listener will always be called after the `MessageBusExceptionHandler`.

```java
SubscriptionId subscriptionId = messageBus.onException(TestMessage.class, new MessageBusExceptionListener<TestMessage>() {
    @Override
    public void accept(TestMessage testMessage, Exception e) {
        System.out.println(e);
    }
});

List<Class<?>> list = new ArrayList<>();
SubscriptionId subscriptionId = messageBus.onException(list, customExceptionListener);
        
messageBus.unregisterExceptionListener(subscriptionId);
```

The `onException` method takes either a single class or a list of classes and a 
`MessageBusExceptionListener<T>` as listener. Whenever an exception occurs for one of the
given classes, the error listener is invoked. The `unregisterExceptionListener` is used to 
remove all the listener matching the given subscriptionId.

## Advance Concepts

### QCEC
A MessageBus allows for a loosely coupled form of communication, where Sender and 
Subscriber do not need to know from each other. They don't even know the number of the
others as members of both sides can join or leave dynamically. Configuring the MessageBus
in an asynchronous way allows for independently integrated parts of the application.

The integration points between the different use cases and Frameworks are a fitting example
for the beneficiary use of an asynchronous MessageBus. But aspects like loose coupling
and dynamic extensibility are of great benefit even in more coupled parts of the 
application, like within a use cases. Use cases execute their logic by assembling different parts
of the application. A MessageBus can be of great help here. `QCEC` defines concepts and
practices how to use a MessageBus inside the context of use cases or components with 
similar requirements of loosely coupling, extensibility and testability while having
more shared context than integration between use cases and Frameworks.

QCEC (qcc) stands for Query, Constraint, Event and Command. These four concepts when
combined with a synchronous MessageBus ease the assembling of logic into a use case.
Queries are responsible to retrieve information out of other objects.
Constraints inform others about a requirement, that, if violated, should rise an 
exception. The purpose of Events is to inform others or to share information with them.
Commands perform updates on Domain Objects or Repositories.

#### Queries
Use cases need to retrieve information from the objects they interact with. Having a list
of all objects of interest results in high coupling. By using a MessageBus, a message
can be distributed to these objects without the use case having too much knowledge
about them. The message is written as `Query`. This means, that subscriber 
upon receiving the `Query` object can use `Query` specific methods to store 
their data into the object. Let's take an example, in which we want to query all of our
apple trees about the number of apples they currently hold. We define a custom Query, 
that is responsible to query how many apples all of our apple trees have:

```java
class NumberOfApplesQuery implements Query<Integer>{
    private int sumOfApples;
        
    public void reportPartialResult(int numberOfApples){
        this.sumOfApples+=numberOfApples;
    }
        
    @Override
    public Integer result() {
        return sumOfApples;
    }
}
```
The `AppleTree` class can subscribe itself on the `NumberOfApplesQuery`. 
Each AppleTree reports its number of apples, whenever someone wants to know how many
apples there are:

```java
class AppleTree {
    public AppleTree(int numberOfApples, QueryResolver queryResolver) {
        queryResolver.answer(NumberOfApplesQuery.class, numberOfApplesQuery -> {
            numberOfApplesQuery.reportPartialResult(numberOfApples);
        });
    }
}
```

Now the use case does not not how to interact with `AppleTree` objects. He just
needs to send the query:

```java
MessageBus messageBus = MessageBusBuilder.aMessageBus()
    .forType(MessageBusType.SYNCHRONOUS)
    .build();

QueryResolver queryResolver = QueryResolverFactory.aQueryResolver(messageBus);

new AppleTree(1, queryResolver);
new AppleTree(3, queryResolver);

int numberOfApples = queryResolver.queryRequired(new NumberOfApplesQuery());
assertEquals(4, numberOfApples);
```
Executing a Query on the QueryResolver allows querying all AppleTrees about their stock.
Although in this example the querying code already knows how many apples there are,
it should be obvious, that the AppleTrees could be created somewhere else without
compromising the validity of the code. The querying code does not even know about
the existence of the AppleTrees. There could be different kinds of AppleTrees and the 
querying code would still be the same.

There exists two different methods for querying `query` and `queryRequired`:
```java
Optional<Integer> optional = queryResolver.query(new NumberOfApplesQuery());
int numberOfApples = optional.orElseThrow(() -> new UnsupportedOperationException("Expected a query result."));
        
int numberOfApples = queryResolver.queryRequired(new NumberOfApplesQuery());
```

The `query` method allows queries not having a result and therefore returning an
optional. The `queryRequired` method throws an `UnsupportedOperationException` when
there is no result.

The `answer` method returns an `SubscriptionId` object. This can be used for the
`unsubscribe` method to stop answering methods. The `answer` can also be used
on super classes or interfaces. In this case all subclasses will result in the 
`answer` method to be called with the respective instance.

```java
SubscriptionId subscriptionId = queryResolver.answer(Query.class, q -> handle(q));
queryResolver.unsubscribe(subscriptionId);
```

Per default queries are delivered to all Subscribers and the result is returned
afterwards. But queries can be stopped early, when it's apparent, that further
Subscribers won't add value to the result. To stop a query early, override the 
`finished` method. Once it returns `true`, the query is stopped and the result
is returned immediately.

```java
class PreemptiveQuery implements Query<Object> {
     private Object result;

     public void setResult(Object result) {
        this.result = result; 
     }

     @Override
     public Object result() {
        return result; 
     }

     @Override
     public boolean finished() {
         return result != null; 
     }
}
```

When subscribing for queries, superclasses can be used. The underlying MessageBus 
ensures, that all subclasses of the class used in `answer` will also call the
consumer. 

#### Constraints
Queries are used to retrieve data from others. They should not throw an exception, 
because it would mix up the partially retrieved data with the exception. But it's often
necessary to ensure, that a specific constraint holds and if it does not, to raise an
exception. This differs from queries in that way as a Constraint either holds or an 
exception is thrown. But a constraint will never return data.

Let's suppose we want to ensure, that the usernames of users are unique. We use a 
Constraint:
```java
class UniqueUsernameConstraint {
     public String usernameToCheck;

     public UniqueUsernameConstraint(String usernameToCheck) {
        this.usernameToCheck = usernameToCheck;
     }
}
```

The User class is responsible to protect the uniqueness of its username:
```java
class User {
    private String username;

    public User(String username, ConstraintEnforcer constraintEnforcer) {
        this.username = username;
        constraintEnforcer.respondTo(UniqueUsernameConstraint.class, uniqueUsernameConstraint -> {
        if(uniqueUsernameConstraint.usernameToCheck.equals(username)){
            throw new UsernameAlreadyInUseException(username);
            }
        });
    }
}
```
Now any code can send Constraints on the `ConstraintEnforcer` object
to ensure, that the unique username constraint holds.

```java
MessageBus messageBus = MessageBusBuilder.aMessageBus()
    .forType(MessageBusType.SYNCHRONOUS)
    .build();

ConstraintEnforcer constraintEnforcer = ConstraintEnforcerFactory.aConstraintEnforcer(messageBus);
        
new User("Tim", constraintEnforcer);
        
constraintEnforcer.enforce(new UniqueUsernameConstraint("Tim"));
```

Similar to the QueryResolver, the `respondTo` method allows for inheritance
and interfaces. It also returns a `SubscriptionId` that
can be used as parameter for the `unsubscribe` method to stop responding to constraints.

When subscribing for constraints, superclasses can be used. The underlying MessageBus 
ensures, that all subclasses of the class used in `respondTo` will also call the
consumer. 

#### Events
Queries retrieve information, constraints enforce rules and events are used to 
forward information. Events never return information
and should not throw an exception. They are just used to indicate, that something happened.

Let's suppose a very basic login use case: Given a username and password, a login is tried.
If it succeeded, an event is published to inform others, that the user went online. 
```java
MessageBus messageBus = MessageBusBuilder.aMessageBus()
    .forType(MessageBusType.SYNCHRONOUS)
    .build();

EventBus eventBus = EventBusFactory.aEventBus(messageBus);
        
boolean loginSuccessful = login(this.username, this.password);
if(loginSuccessful){
    eventBus.publish(new UserOnlineEvent(this.username));
}else{
    goBackToLoginForm();
}

class UserOnlineEvent {
    public String username;

    public UserOnlineEvent(String username) {
        this.username = username;
    }
}
```
The code publishing the event does not care, what others do with the information or even 
if there are others. It is of no concern for its functionality that other receive the event.

But other components might be interested, when a user goes online:
```java
class UserOnlineView {
    private final List<String> userOnline = new ArrayList<>();

    public UserOnlineView(EventBus eventBus) {
        eventBus.reactTo(UserOnlineEvent.class, userOnlineEvent -> {
            final String username = userOnlineEvent.username;
            userOnline.add(username);
        });
    }
}
```
The `UserOnlineView` is dependent on the event and its information. But it doesn't 
care, who sent it. It just needs the information.

The `EventBus` has three methods: `reactTo` to add a subscriber for a class and all
its subclasses. `publish` sends the Event on the underlying synchronous MessageBus.
And `unsubscribe` removes the subscription for the given `SubscriptionId`.

When subscribing for events, superclasses can be used. The underlying MessageBus 
ensures, that all subclasses of the class used in `reactTo` will also call the
consumer. 

#### Commands
Aside from querying and aggregating data, use cases are responsible for a safe and secure
update to the applications data. A common pattern is to model the update in form of
Commands. A Command is a reusable abstraction over the the update. It gets the
required parameter during its creation by the use case. During its invocation by the consuming
counterpart it gets all information to execute its task. By moving the update logic
out of the use case into a distinct object, the update process becomes decoupled 
from the use case and therefore reusable and testable.

#### DocumentBus
It's rarely the case that an application uses only Queries, Constraints or 
Events. Most of the time it's a mixture of these three. Therefore it 
becomes a burden to drag along a `QueryResolver`, a 
`ConstraintEnforcer` and an `EventBus`. It also becomes difficult to remember which
SubscriptionId was used for which of these objects. Therefore the `DocumentBus`
was created to combine these concepts and provide an easier to use interface.

It provides three entry methods: `answer` for Queries, `ensure` for Constraints and 
`reactTo` for Events, which represent the three respective methods of the QueryResolver,
ConstraintEnforcer and EventBus. But the DocumentBus allows to enhance the subscription
with conditionals and an automatic unsubscription.

Let's extend the AppleTree example with an DocumentBus. An AppleTree still reports his
stock of apples to the `NumberOfApplesQuery`. But only if the query is from the owner
of the tree. And the tree can only report as long as it is not cut down. Then it should
stop its reporting and unsubscribe from the `NumberOfApplesQuery`.


```java
DocumentBus documentBus = DocumentBusBuilder.aDefaultDocumentBus();
 
SubscriptionId subscriptionId = documentBus.answer(NumberOfAppleQuery.class)
    .onlyIf(numberOfAppleQuery -> numberOfAppleQuery.getOwner().equals(this.owner))
    .until(AppleTreeCutDownEvent.class, appleTreeCutDownEvent -> appleTreeCutDownEvent.getTree().equals(this))   
    .using(numberOfAppleQuery -> numberOfAppleQuery.reportPartial(this.numberOfApples))
```
The `answer` method takes the Query, for which itself or its subclasses the Consumer
given in `using` should be called. The `onlyIf` method can add arbitrary many conditions.
Only if all of them return `true` the Consumer given in `using` is called. 
The `until` method allows for one or several automatic unsubscriptions. Whenever one of
these conditions return true, the subscription is removed and the AppleTree stops
responding to the `NumberOfAppleQuery`. The returned SubscriptionId identified 
subscription for the query. It can be used to manually unsubscribe as long
as an the `until` condition has not been met yet.

The same convenience interface exists for the Constraint's `ensure` and the 
Event's `reactTo` method:


```java
documentBus.reactTo(AppleTreeCutDownEvent.class)
    .until(AppleTreeCutDownEvent.class, appleTreeCutDownEvent -> appleTreeCutDownEvent.getTree().equals(this))
    .using(appleTreeCutDownEvent -> releaseResources());
        
documentBus.ensure(TreeSpotFreeConstraint.class)
    .until(AppleTreeCutDownEvent.class, appleTreeCutDownEvent -> appleTreeCutDownEvent.getTree().equals(this))
    .using(treeSpotFreeConstraint -> {
        if(treeSpotFreeConstraint.getSpot().equals(this.spot)){
            throw new TreeSpotAlreadyOccupiedException(this.spot);
        }
    });
```

The `onlyIf` methods also exists for `reactTo` and `ensure`.

Sending objects is similar to the distinct single objects:

```java
Optional<Integer> optional = documentBus.query(new NumberOfAppleQuery());
int numberOfApples = documentBus.queryRequired(new NumberOfAppleQuery());

documentBus.enforce(new TreeSpotFreeConstraint());
        
documentBus.publish(new AppleTreeCutDownEvent());
```
### Message Function
Implementing a Request-Reply communication over an asynchronous MessageBus can be
error-prone. Once the request is send, numerous ways exist, how to respond to it.
It could be answered by different types for replies. Some model success responses, 
others error responses. But also exceptions can occur and no regular response is ever
sent. In case several requests are simultaneously active, responses and exceptions 
have to be checked, if they correspond to the correct request.

The `MessageFunction` class simplifies this Request-Reply communication. During its creation
you can define, which potential success and error responses could occur. Sending the request 
returns a future, that is fulfilled once the request was answered: may it be a successful reply,
a reply indicating an error or an exception, that was thrown during the process.

The future provides the methods `get`and `await`, which block the caller until a result
is received or the optional timeout expired. The future also allows for a non blocking
processing. Via the `then` method follow up actions can be defined, that are executed
once a the future is fulfilled.

The following example modells the process of buying a number of apples from a farmer.
The `BuyAppleRequest` starts the negotiation. The farmer can accept the offer with 
an `AcceptOfferReply` or decline with a `DeclineOfferReply` based on his stock:

```java
class Farmer {
    private int stock;

    public Farmer(MessageBus messageBus, int stock) {
    this.stock = stock;
    messageBus.subscribe(BuyAppleRequest.class, buyAppleRequest -> {
        CorrelationId correlationId = buyAppleRequest.correlationId;
        if (stock >= buyAppleRequest.numberOfApples) {
            AcceptOfferReply reply = new AcceptOfferReply(correlationId);
            messageBus.send(reply);
        }else{
            DeclineOfferReply reply = new DeclineOfferReply(correlationId);
            messageBus.send(reply);
        }
    });
    }
}

class BuyAppleRequest {
    public int numberOfApples;
    public CorrelationId correlationId = CorrelationId.newUniqueId();

    public BuyAppleRequest(int numberOfApples) {
        this.numberOfApples = numberOfApples;
    }
}

interface OfferReply {
    CorrelationId getCorrelationId();
}
    
class AcceptOfferReply implements OfferReply {
    public CorrelationId correlationId;

    public AcceptOfferReply(CorrelationId correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public CorrelationId getCorrelationId() {
        return correlationId;
    }
}

class DeclineOfferReply implements OfferReply {
    public CorrelationId correlationId;

    public DeclineOfferReply(CorrelationId correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public CorrelationId getCorrelationId() {
        return correlationId;
    }
}
```
The `CorrelationId` is necessary to match a reply to its corresponding request. Instead
of implementing a lot of subscriber and error logic itself the client code makes use
of a `MessageFunction`:

```java
MessageBus messageBus = MessageBusBuilder.aMessageBus()
    .forType(MessageBusType.ASYNCHRONOUS)
    .withAsynchronousConfiguration(asyncConfig)
    .build();

MessageFunction<BuyAppleRequest, OfferReply> messageFunction = MessageFunctionBuilder.aMessageFunction()
    .forRequestType(BuyAppleRequest.class)
    .forResponseType(OfferReply.class)
    .with(BuyAppleRequest.class)
    .answeredBy(AcceptOfferReply.class)
    .orByError(DeclineOfferReply.class)
    .obtainingCorrelationIdsOfRequestsWith(buyAppleRequest -> buyAppleRequest.correlationId)
    .obtainingCorrelationIdsOfResponsesWith(OfferReply::getCorrelationId)
    .usingMessageBus(messageBus)
    .build();

new Farmer(messageBus, 11);

ResponseFuture<OfferReply> responseFuture = messageFunction.request(new BuyAppleRequest(5));
responseFuture.then((response, wasSuccessful, exception) -> {
    if (exception != null) {
        System.out.println("Exception occured: " + exception);
    } else {
        if (wasSuccessful) {
            System.out.println("AcceptOfferReply received: " + response);
        } else {
            System.out.println("DeclineOfferReply received: " + response);
        }
    }
});
```
The builder of a MessageFunction contains several Steps:
- `forRequestType` defines the class (or superclass) for potential requests
- `forResponseType` defines the class (or often superclass) for potential replies
- The mapping of a request class to  its potential replies is started using the 
`with` method. The `with` method takes the class of the request (which has to 
extend the class given in `forRequestType`). Afterwards potential replies are
defined.
- The `answeredBy` method takes a reply class that counts as successful response when received
- The `orByError` method takes a class that when received results as not successful
- Distinguishing replies from different replies is done based on `CorrelationIds`.
Each request creates a new, unique one. All responses should contain the same 
`CorrelationIds`. The two `obtainingCorrelationIdsOf...` methods are used to extract 
 these identifiers out of the request and its reply.
- Finally the MessageBus to use is set and the MessageFunction is build 

The difference of `answeredBy` (and later `or`) and `orByError` is the marking the 
message as successful in the `ResponseFuture`. Responses defined in `answeredBy` and
`or` will fulfill the future successful. The future's method `wasSuccessful` or 
the property `wasSuccessful` inside the `FollowUpActions` will be set to `true`.
Responses defined in `orByError` will result `false` being the result.

#### ResponseFuture
A request can be send with the `request` function. It returns a `ResponseFuture` object 
specific for that request. This `ResponseFuture` instance is fulfilled, once a response
defined by `answeredBy`, `or` and `orByError` with a matching `CorrelationId` was 
received. Being a java `Future`, it provides methods to query or wait on the result:

```java
ResponseFuture<T> future = messageFunction.request(message);
try {
    T result = future.get();
} catch (InterruptedException | ExecutionException e) {

}

try {
    T result = future.get(3, TimeUnit.SECONDS);
} catch (InterruptedException | ExecutionException | TimeoutException e) {

}

boolean isDone = future.isDone();

boolean mayInterruptIfRunning = true;
boolean cancelSucceeded = future.cancel(mayInterruptIfRunning);
boolean cancelled = future.isCancelled();
```
These standard future methods follow the contract described in the `Future`javadoc.

The `get` methods suspend the caller until a result was receied or the optional 
timeout expired. Handling the result in a nonblocking way is provides with
`FollowUpAction`. The `then` method allows to one `FollowUpAction`, that will be executed
once the future fulfills. The handler logic is called on the Thread, that fulfilled
the future. The `FollowUpAction` accepts three parameter:
1) The response. This can be a success or failure response. Note that response is only
available, when no exception occurred. Therefore always check for `exception == null`
first
2) A boolean indicating whether it was a success response (as defined by `answeredBy`
and `or`) or an error response (as defined by `orByError`). It is also only correctly set, when
exception is null.
3) In case a exception occurred during any of the involved messages, the exception parameter
is not null.

A future fulfills only once. So an exception during the sending of a request will fulfill
the future. Subsequent replies to the request will be ignored and won't trigger any 
`FollowUpActions` twice. At any time only one `FollowUpAction` is allowed. When the future
is cancelled, no `FollowUpActions` will be executed. 

```java
responseFuture.then((response, wasSuccessful, exception) -> {
    if (exception != null) {
        System.out.println("Exception occured: " + exception);
    } else {
        if (wasSuccessful) {
            System.out.println("AcceptOfferReply received: " + response);
        } else {
            System.out.println("DeclineOfferReply received: " + response);
        }
    }
});
```

#### Several Request-Response Pairs
The `with`method can be called several times to allow for more than one pair of request
and responses. Inside each `with` flow, one `answeredBy` is also expected as next call.
But the subsequent `or` and `orByError` calls can be called several times:
```java
MessageFunctionBuilder.aMessageFunction()
    .forRequestType(RequestSuperClass.class)
    .forResponseType(ResponseSuperClass.class)
    .with(RequestType1.class)
    .answeredBy(Reply1.class).or(Reply2.class)
    .orByError(Error1.class).orByError(Error2.class)
    .with(RequestType2.class)
    .answeredBy(Reply1.class).or(Reply2.class).or(Reply3.class)
    .orByError(Error1.class)
    .obtainingCorrelationIdsOfRequestsWith(request -> {})
    .obtainingCorrelationIdsOfResponsesWith(response -> {})
    .usingMessageBus(messageBus)
    .build();
```

#### GeneralErrorResponse
There happen to occur cross-cutting errors, that are not part of the request-response 
class hierarchy. Nonetheless the MessageFunction should react to these and abort the
corresponding future. The `withGeneralErrorResponse` takes as arguments the class of 
the error response and an optional conditional. Whenever a matching response is received,
the future is fulfilled and marked as unsuccessful:
```java
MessageFunctionBuilder.aMessageFunction()
    .forRequestType(RequestClass.class)
    .forResponseType(ResponseClass.class)
    .with(RequestClass.class).answeredBy(ReplyClass.class)
    .withGeneralErrorResponse(GeneralErrorResponse.class)
    .obtainingCorrelationIdsOfRequestsWith(request -> {})
    .obtainingCorrelationIdsOfResponsesWith(response -> {})
    .usingMessageBus(messageBus)
    .build();

MessageFunctionBuilder.aMessageFunction()
    .forRequestType(RequestClass.class)
    .forResponseType(ResponseClass.class)
    .with(RequestClass.class).answeredBy(ReplyClass.class)
    .withGeneralErrorResponse(GeneralErrorResponse.class, (generalErrorResponse, request) -> {
        return generalErrorResponse.getCorrelationId().equals(request.getCorrelationId())
    })
    .obtainingCorrelationIdsOfRequestsWith(request -> {})
    .obtainingCorrelationIdsOfResponsesWith(response -> {})
    .usingMessageBus(messageBus)
    .build();
```

### Subscriber
Most of the functions, that take an Subscriber object, are overloaded to take also
a Consumer object. Internally the Consumer object is mapped to a Subscriber, but the 
user does not have to burden itself with the handling of SubscriptionIds. But implementing
your own Subscriber allows for greater control over the accepting and subscription mechanisms.
The `Subscriber` interface defines two methods:
```java
public interface Subscriber<T> {

    AcceptingBehavior accept(T message);

    SubscriptionId getSubscriptionId();
}
```
The `accept` method is called, whenever an object of the type `T` is received. The message
should return an `AcceptingBehavior` object. This object can control, if the delivery of the
message should be continued:
```java
public AcceptingBehavior accept(Object message) {
    boolean continueDelivery = handle(message);
    return AcceptingBehavior.acceptingBehavior(continueDelivery);
}
```
A `false` will stop the delivery of the message to subsequent subscriber. If the result
is known statically, two convenience constants can be used:

```java
AcceptingBehavior.MESSAGE_ACCEPTED;
AcceptingBehavior.MESSAGE_ACCEPTED_AND_STOP_DELIVERY;
```

The second method `getSubscriptionId` should return a SubscriptionId, that is constant
and unique for the Subscriber. The identification of an subscriber should be dependent
on the equality of the SubscriberId returned by this method. `equals` and `hashCode` 
should behave accordingly.

Two convenience implementations of the `Subscriber` interface exist: The `ConsumerSubscriber`
which creates a `Subscriber` from java `consume` and the `PreemptiveSubscriber`, which takes
a java `predicate`. The return value of the `predicate` is used to decide, if the
delivery is continued (return `true`) or if it is preempted (return `false`):

```java
ConsumerSubscriber<Object> consumerSubscriber = ConsumerSubscriber.consumerSubscriber(m -> {
    System.out.println(m);
});

PreemptiveSubscriber<Object> preemptiveSubscriber = PreemptiveSubscriber.preemptiveSubscriber(m -> {
    if (shouldDeliveryContinue(m)) {
        return true;
    } else {
        return false;
    }
});
```
 
### Custom Actions
The built-in Actions for Channels should cover most use cases. In case customization 
is needed, the `Action` interface can be implemented:

```java
public interface Action<T> {}
```
It does not define any methods. An Action is only a container for necessary data. All
the logic about executing the Action is done by the respective `ActionHandler`. For 
every custom Action, there must be an `ActionHandler` specifically written for this Action:

```java
public interface ActionHandler<T extends Action<R>, R> { 
    void handle(T action, ProcessingContext<R> processingContext);
}
```
The `ActionHandler` interface defines two generic parameter: `R` is the generic given by the
Action. Normally it is inherited by the type of the Channel. `T` corresponds the Action
for which the ActionHandler is written. The `handle` method is called whenever a message
with the Action has reached the end of the Channel. The following example implements a logging Action.
This should clarify the generic parameter:

We define a custom `Log` Action, which contains a PrintStream as target.

```java
class Log<T> implements Action<T> {
    private final PrintStream stream = System.out;

    public PrintStream getStream() {
        return stream;
    }
}
```

Additionally an `ActionHandler` is needed, so that Channel can execute the Log Action:

```java
class LogActionHandler<T> implements ActionHandler<Log<T>, T> {
     @Override
     public void handle(Log<T> action, ProcessingContext<T> processingContext) {
        final PrintStream stream = action.getStream();
        stream.println(processingContext);
     }
}
```

When we want to use our Log Action, we have to make it known to the Channel. Each 
Channel has an `ActionHandlerSet` set during creation. Only those code Actions 
can be used as final code Action of the code Channel, that have a matching 
ActionHandler registered in the set. If an unknown Action is encountered, an 
`NoHandlerForUnknownActionException` is thrown.

To add your custom Action, register it the your custom `ActionHandlerSet`:
```java
ActionHandlerSet<Object> actionHandlerSet = DefaultActionHandlerSet.defaultActionHandlerSet();
actionHandlerSet.registerActionHandler(Log.class, new LogActionHandler<>());
Channel<Object> channel = ChannelBuilder.aChannel(Object.class)
    .withDefaultAction(new Log<>())
    .withActionHandlerSet(actionHandlerSet)
    .build();
```
We used the default `ActionHandlerSet`, so that we do not have to register the built-in
Actions and their ActionHandlers ourselves. But a completely different set can be built from scratch
anytime with:

```java
ActionHandlerSet<T> actionHandlerSet = ActionHandlerSet.emptyActionHandlerSet();

//manually registering all built-in actions
actionHandlerSet.registerActionHandler(Consume.class, ConsumerActionHandler.consumerActionHandler());
actionHandlerSet.registerActionHandler(Subscription.class, SubscriptionActionHandler.subscriptionActionHandler());
actionHandlerSet.registerActionHandler(Jump.class, JumpActionHandler.jumpActionHandler());
actionHandlerSet.registerActionHandler(Return.class, ReturnActionHandler.returnActionHandler());
actionHandlerSet.registerActionHandler(Call.class, CallActionHandler.callActionHandler());
```
