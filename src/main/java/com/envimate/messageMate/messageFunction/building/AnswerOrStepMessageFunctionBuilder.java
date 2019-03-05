package com.envimate.messageMate.messageFunction.building;

public interface AnswerOrStepMessageFunctionBuilder<R,S> {

    <U extends S> Step4RequestAnswerStep3MessageFunctionBuilder<R, S> or(Class<U> responseClass);
}
