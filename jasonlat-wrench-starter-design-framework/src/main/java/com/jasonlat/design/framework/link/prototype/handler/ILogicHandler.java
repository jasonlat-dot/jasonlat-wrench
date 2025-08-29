package com.jasonlat.design.framework.link.prototype.handler;


import com.jasonlat.design.framework.link.prototype.DynamicContext;

/**
 * 逻辑处理器接口
 * 定义了责任链模式中每个处理器的基本行为
 * 提供了继续执行、停止执行和具体处理逻辑的方法
 * 
 * @param <T> 请求参数类型
 * @param <D> 动态上下文类型，必须继承自DynamicContext
 * @param <R> 返回结果类型
 * @author Jasonlat
 * @description 逻辑处理器
 * @create 2025-08-29
 */
public interface ILogicHandler<T, D extends DynamicContext, R> {

    /**
     * 设置继续执行下一个处理器
     * 默认实现，将动态上下文的proceed标识设置为true，表示继续执行链路
     * 
     * @param requestParameter 请求参数
     * @param dynamicContext 动态上下文
     * @return 返回null，表示继续执行
     */
    default R next(T requestParameter, D dynamicContext) {
        dynamicContext.setProceed(true);
        return null;
    }

    /**
     * 停止执行链路并返回结果
     * 将动态上下文的proceed标识设置为false，中断链路执行
     * 
     * @param requestParameter 请求参数
     * @param dynamicContext 动态上下文
     * @param result 要返回的结果
     * @return 返回指定的结果
     */
    default R stop(T requestParameter, D dynamicContext, R result){
        dynamicContext.setProceed(false);
        return result;
    }

    /**
     * 执行具体的业务逻辑处理
     * 每个实现类必须重写此方法，定义具体的处理逻辑
     * 
     * @param requestParameter 请求参数，包含处理所需的输入数据
     * @param dynamicContext 动态上下文，用于控制执行流程和传递数据
     * @return 处理结果，可以为null
     * @throws Exception 处理过程中可能抛出的异常
     */
    R apply(T requestParameter, D dynamicContext) throws Exception;

}
