package com.envimate.messageMate.messageFunction.building;

public interface AnswerAnsweredByStepMessageFunctionBuilder<R,S> {

    <U extends S> Step4RequestAnswerStep2MessageFunctionBuilder<R, S> answeredBy(Class<U> responseClass);
}
