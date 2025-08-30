package com.jasonlat.design.framework.tree;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 多线程策略路由抽象类
 * <p>
 * 该类提供了基于多线程的策略路由功能，支持异步数据加载和业务流程处理。
 * 继承了策略映射器和策略处理器的功能，可以根据请求参数和上下文动态选择合适的策略处理器。
 * </p>
 * 
 * <p>主要特性：</p>
 * <ul>
 *   <li>支持多线程异步数据加载</li>
 *   <li>提供默认策略处理器机制</li>
 *   <li>支持策略路由和动态选择</li>
 *   <li>泛型设计，支持不同类型的请求参数、上下文和返回值</li>
 * </ul>
 *
 * @param <T> 请求参数类型
 * @param <D> 动态上下文类型
 * @param <R> 返回结果类型
 * @author Jasonlat
 * @description 异步资源加载策略
 * @create 2025-08-30
 */
public abstract class AbstractMultiThreadStrategyRouter<T, D, R> implements StrategyMapper<T, D, R>, StrategyHandler<T, D, R> {

    /**
     * 默认策略处理器
     * <p>
     * 当无法找到合适的策略处理器时，将使用此默认处理器进行处理
     * </p>
     */
    @Getter
    @Setter
    @SuppressWarnings("unchecked")
    protected StrategyHandler<T, D, R> defaultStrategyHandler = StrategyHandler.DEFAULT;

    /**
     * 策略路由方法
     * <p>
     * 根据请求参数和动态上下文获取合适的策略处理器，如果找不到则使用默认处理器
     * </p>
     *
     * @param requestParameter 请求参数
     * @param dynamicContext 动态上下文
     * @return 处理结果
     * @throws Exception 处理过程中可能抛出的异常
     */
    public R router(T requestParameter, D dynamicContext) throws Exception {
        StrategyHandler<T, D, R> strategyHandler = get(requestParameter, dynamicContext);
        if(null != strategyHandler) return strategyHandler.apply(requestParameter, dynamicContext);
        return defaultStrategyHandler.apply(requestParameter, dynamicContext);
    }

    /**
     * 策略处理器的应用方法
     * <p>
     * 该方法首先执行异步数据加载，然后进行业务流程处理
     * </p>
     *
     * @param requestParameter 请求参数
     * @param dynamicContext 动态上下文
     * @return 处理结果
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public R apply(T requestParameter, D dynamicContext) throws Exception {
        // 异步加载数据
        multiThread(requestParameter, dynamicContext);
        // 业务流程受理
        return doApply(requestParameter, dynamicContext);
    }

    /**
     * 多线程异步数据加载方法
     * <p>
     * 子类需要实现此方法来定义具体的异步数据加载逻辑。
     * 该方法在业务流程处理之前执行，用于预加载必要的数据。
     * </p>
     *
     * @param requestParameter 请求参数
     * @param dynamicContext 动态上下文
     * @throws ExecutionException 执行异常
     * @throws InterruptedException 中断异常
     * @throws TimeoutException 超时异常
     */
    protected abstract void multiThread(T requestParameter, D dynamicContext) throws ExecutionException, InterruptedException, TimeoutException;

    /**
     * 业务流程处理方法
     * <p>
     * 子类需要实现此方法来定义具体的业务处理逻辑。
     * 该方法在异步数据加载完成后执行。
     * </p>
     *
     * @param requestParameter 请求参数
     * @param dynamicContext 动态上下文
     * @return 处理结果
     * @throws Exception 处理过程中可能抛出的异常
     */
    protected abstract R doApply(T requestParameter, D dynamicContext) throws Exception;

}
