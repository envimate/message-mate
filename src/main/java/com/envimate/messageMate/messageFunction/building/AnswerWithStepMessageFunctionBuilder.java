package com.envimate.messageMate.messageFunction.building;

public interface AnswerWithStepMessageFunctionBuilder<R,S> {

    <U extends R> Step4RequestAnswerStep1MessageFunctionBuilder<R, S> with(Class<U> requestClass);
}
