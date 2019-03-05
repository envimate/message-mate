package com.envimate.messageMate.messageFunction.building;

public interface AnswerOrByErrorStepMessageFunctionBuilder<R,S> {

    <U extends S> Step4RequestAnswerStep3MessageFunctionBuilder<R, S> orByError(Class<U> responseClass);

}
